package com.example.vedrt.render;

import com.example.vedrt.module.BlockEsp;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/** Draws translucent boxes with depth testing disabled so they show through walls. */
public final class BlockEspRenderer {
    private BlockEspRenderer() {}

    private static final RenderPipeline PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("vedrt", "pipeline/esp_filled_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build());

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    public static void init() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(BlockEspRenderer::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        BlockEsp esp = BlockEsp.INSTANCE;
        if (esp == null || !esp.isEnabled()) return;
        long[] hits = BlockEsp.results();
        int limit = Math.min(hits.length, esp.maxBoxes());
        if (limit == 0) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cam = context.worldState().cameraRenderState.pos;
        float[] c = esp.color();
        float a = esp.opacity();

        PoseStack matrices = context.matrices();
        matrices.pushPose();
        buffer = new BufferBuilder(ALLOCATOR, PIPELINE.getVertexFormatMode(), PIPELINE.getVertexFormat());
        Matrix4fc m = matrices.last().pose();
        for (int i = 0; i < limit; i++) {
            long p = hits[i];
            float x = (float) (BlockPos.getX(p) - cam.x);
            float y = (float) (BlockPos.getY(p) - cam.y);
            float z = (float) (BlockPos.getZ(p) - cam.z);
            box(m, buffer, x, y, z, x + 1, y + 1, z + 1, c[0], c[1], c[2], a);
        }
        matrices.popPose();

        draw(mc, PIPELINE);
    }

    private static void box(Matrix4fc pm, BufferBuilder b, float minX, float minY, float minZ,
                            float maxX, float maxY, float maxZ, float r, float g, float bl, float a) {
        // Front
        b.addVertex(pm, minX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, maxY, maxZ).setColor(r, g, bl, a);
        // Back
        b.addVertex(pm, maxX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, maxY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, minZ).setColor(r, g, bl, a);
        // Left
        b.addVertex(pm, minX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, maxY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, maxY, minZ).setColor(r, g, bl, a);
        // Right
        b.addVertex(pm, maxX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, maxZ).setColor(r, g, bl, a);
        // Top
        b.addVertex(pm, minX, maxY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, maxY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, maxY, minZ).setColor(r, g, bl, a);
        // Bottom
        b.addVertex(pm, minX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, minY, minZ).setColor(r, g, bl, a);
        b.addVertex(pm, maxX, minY, maxZ).setColor(r, g, bl, a);
        b.addVertex(pm, minX, minY, maxZ).setColor(r, g, bl, a);
    }

    private static void draw(Minecraft client, RenderPipeline pipeline) {
        MeshData built = buffer.buildOrThrow();
        MeshData.DrawState params = built.drawState();
        VertexFormat format = params.format();

        GpuBuffer vertices = upload(params, format, built);
        drawBuffer(client, pipeline, built, params, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(MeshData.DrawState params, VertexFormat format, MeshData built) {
        int size = params.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < size) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "vedrt block esp", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, size);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, built.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(built.vertexBuffer(), view.data());
        }
        return vertexBuffer.currentBuffer();
    }

    private static void drawBuffer(Minecraft client, RenderPipeline pipeline, MeshData built,
                                   MeshData.DrawState params, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            built.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(built.indexBuffer());
            indexType = built.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shape = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shape.getBuffer(params.indexCount());
            indexType = shape.type();
        }

        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            () -> "vedrt block esp rendering",
            client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
            client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, params.indexCount(), 1);
        }

        built.close();
    }

    public static void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
