# Autism-Client-ElytraMace-Addon
# Elytra Mace Addon

Elytra Mace addon for Autism Client. Auto rockets with elytra, track nearest player, swap to chestplate mid-air, and attack with mace.

## Features

- Auto elytra takeoff and rocket usage
- Lock onto and track nearest player
- Swap elytra to chestplate mid-air (10 block range)
- Mace attack (6 block range)
- Re-equip elytra and repeat cycle after attack

## Settings

| Setting | Default | Range | Description |
|---------|---------|-------|-------------|
| Rocket Delay | 12 | 5-30 | Delay between rockets (ticks) |
| Search Chunk | 16 | 1-32 | Target search range (chunks) |

## State Machine

```
TAKEOFF → ROCKETS → TRACK → SWAP_WAIT → ATTACK → RE_ELYTRA → (repeat)
```

1. **TAKEOFF** - Looks up and activates elytra
2. **ROCKETS** - Uses 2 or 3 rockets (based on distance)
3. **TRACK** - Locks onto nearest player and flies toward them
4. **SWAP_WAIT** - Swaps elytra with chestplate from inventory
5. **ATTACK** - Attacks with mace (5 ticks or until landing)
6. **RE_ELYTRA** - Re-equips elytra and loops back to start

## Installation

1. Install Autism Client for Minecraft 1.26.2
2. Copy `build/libs/elytra-mace-addon-1.0.0.jar` to your mods folder
3. Launch the game

## Requirements

- Minecraft 1.26.2
- Fabric Loader 0.19.3
- Autism Client 4.4-26.2
- Java 25

## Development

```bash
# Run in project directory
./gradlew clean build
```

Output: `build/libs/elytra-mace-addon-1.0.0.jar`
Çıktı: `build/libs/elytra-mace-addon-1.0.0.jar`
