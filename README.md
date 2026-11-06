# VEDRT

Client-side Fabric mod for **Minecraft 1.21.11** (Mojang mappings, Java 21) with a clickable module menu.
Built for single player and your own LAN world. **It disables itself on remote servers.**

## Using it
- **Right Shift** opens the menu.
- **Left-click** a module to toggle it, **right-click** it to open its settings.
- Settings are saved to `config/vedrt.json`.
- Optional per-module keybinds in Options > Controls > VEDRT (unbound by default).

## Modules
| Module | Settings |
|---|---|
| GlowEsp | range, players / hostile / animals / other / items toggles, "Only these" entity list (e.g. `zombie, creeper`) |
| BlockEsp | range, max boxes, opacity, color, block list with presets (Chests, Ores, Spawners, Dirt) or type your own ids (e.g. `dirt`, `diamond_ore`) |
| Fullbright | - |
| Freecam | speed |
| `/vedrt find <structure>` | runs vanilla `/locate structure` (needs cheats on) |

## Build
```
gradle wrapper --gradle-version 9.2.1   # once, generates gradlew
./gradlew build                          # jar in build/libs
./gradlew runClient
```
Check versions in `gradle.properties` against https://fabricmc.net/develop.

## Notes
- BlockEsp rendering follows the Fabric docs "Rendering in the World" for 1.21.11 (custom no-depth pipeline).
- Rename the package `com.example.vedrt` before publishing.
