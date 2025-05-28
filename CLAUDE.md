# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- **Build plugin JAR**: `mvn package`
- **Clean build**: `mvn clean package`
- **Skip tests**: `mvn package -DskipTests`

The build automatically copies the JAR to the Feather server plugins directory via the maven-antrun-plugin.

## Architecture Overview

This is a comprehensive Minecraft Spigot plugin (MinecraftNew) that adds extensive RPG mechanics to vanilla Minecraft. The plugin follows a modular subsystem architecture:

### Core Structure

- **Main Class**: `MinecraftNew.java` - Central plugin orchestrator that initializes all subsystems
- **Package Structure**: `goat.minecraft.minecraftnew.*`
- **Dependencies**: Spigot 1.20.1, WorldEdit, MobChipLite
- **Java Version**: 21

### Key Subsystems

**Combat System** (`subsystems/combat/`)
- Enhanced combat mechanics with buffs, damage handling, hostility management
- Epic boss fights (Ender Dragon, Knight mobs)
- Rare combat drops and damage notifications

**Pet System** (`subsystems/pets/`)
- Comprehensive pet management with 40+ unique perks
- Pet leveling, collection, and interaction mechanics
- Pet-specific abilities (flight, mining bonuses, combat effects)

**Enchanting System** (`subsystems/enchanting/`)
- Custom enchantments with unique effects (Feed, Merit, Cleaver, etc.)
- Ultimate enchanting system with custom listeners
- Enchantment registration and effect handling

**Skill Systems**
- Mining (`subsystems/mining/`) - Gem collection, oxygen management
- Fishing (`subsystems/fishing/`) - Sea creatures, rarity system
- Farming (`subsystems/farming/`) - Verdant relics, seeder mechanics
- Forestry (`subsystems/forestry/`) - Forest spirits, pet management

**Crafting & Economy**
- Culinary system (`subsystems/culinary/`) - Advanced cooking mechanics
- Brewing (`subsystems/brewing/`) - Custom potions with unique effects
- Smithing (`subsystems/smithing/`) - Equipment reforging and talismans
- Villager professions (Bartender, Engineer) with custom trades

**World Management**
- Realms system (`subsystems/realms/`) - Tropic dimension
- Better End (`utils/dimensions/end/`) - Enhanced End dimension
- Music system with disc-based arena mechanics

### Merit System

Central progression system managed by `PlayerMeritManager` with perks like:
- Combat enhancements (Berserker's Rage, Vampiric Strike)
- Utility abilities (Ender Mind, Tactical Retreat)
- Training bonuses and quick swap mechanics

### Development Patterns

**Event-Driven Architecture**: Most functionality implemented as Bukkit event listeners
**Singleton Patterns**: Manager classes use getInstance() for shared state
**Modular Registration**: Each subsystem registers its own events and commands in onEnable()
**Resource Management**: Proper cleanup in onDisable() with save operations

### Data Persistence

- Player data stored in plugin data folder
- Pet collections, merit progress, and skill levels persisted
- Inventory states saved for various subsystems

### Command Structure

100+ commands registered in plugin.yml covering all subsystems. Key admin commands require `continuity.admin` permission.

## Working with Subsystems

When modifying subsystems:
1. Check existing manager classes for initialization patterns
2. Register new event listeners in MinecraftNew.onEnable()
3. Add command executors and register in plugin.yml
4. Ensure proper cleanup in onDisable() if needed
5. Follow the package structure: `subsystems/<category>/<feature>`