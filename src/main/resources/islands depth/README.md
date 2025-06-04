# Islands Datapack - Vanilla Edition

This is a vanilla-only datapack that generates circular islands in an infinite ocean, similar to the "continuity copy" datapack but without requiring the More Density Functions mod.

## Features

- **Vanilla Compatible**: Uses only vanilla Minecraft density functions
- **Circular Islands**: Creates roughly circular land masses surrounded by ocean
- **Configurable Size**: Island radius can be adjusted in the spawn.json file
- **Natural Variation**: Islands have natural, varied edges using noise functions

## Installation

1. Place this datapack in your world's `datapacks` folder
2. Reload datapacks with `/reload` or restart the world
3. Create a new world or use `/tp` to teleport to ungenerated chunks

## Customization

To adjust island size, edit `data/islands/worldgen/density_function/spawn.json`:
- Change the `argument1` value (currently 200) to make islands larger or smaller
- Higher values = larger islands
- Lower values = smaller islands (values too low may result in no islands)

## Technical Details

This datapack replaces the custom density functions from More Density Functions with vanilla equivalents:

- **X/Z Coordinates**: Approximated using noise functions with very small scales
- **Square Root**: Replaced with max/abs combinations for distance approximation  
- **Negation**: Replaced with multiplication by -1

The result creates island generation that closely matches the original while being fully vanilla-compatible.

## Compatibility

- **Minecraft Version**: 1.20.1+
- **Pack Format**: 15
- **Dependencies**: None (vanilla only)

## Credits

Based on the "One Island" datapack by Klinbee, adapted to work without the More Density Functions mod.