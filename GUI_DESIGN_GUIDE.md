# GUI Design Guide

This document summarizes the most polished GUI implementations in the project and explains how to structure future GUIs using the same patterns.

## Showcase of Existing GUIs

### Merit GUI
Implemented in `MeritCommand.java`【F:src/main/java/goat/minecraft/minecraftnew/utils/commands/MeritCommand.java†L250-L331】, this interface uses a 54–slot inventory with page selectors, decorative borders and dynamic item lore. Player merit points are shown with a diamond item and the interface refreshes itself when perks are purchased.

### Backpack GUI
The persistent backpack is handled by `CustomBundleGUI`【F:src/main/java/goat/minecraft/minecraftnew/other/additionalfunctionality/CustomBundleGUI.java†L61-L106】. It loads and saves inventory contents to a YAML file and blocks moving the backpack item inside its own GUI.

### Particle Selection GUI
`ItemDisplayManager` provides a particle‑selection interface【F:src/main/java/goat/minecraft/minecraftnew/other/qol/ItemDisplayManager.java†L468-L503】. It presents available particles and a mode‑toggle lever in slot 53. Clicking items updates the display in real time.

### Skills GUI
Implemented in `SkillsCommand.java`【F:src/main/java/goat/minecraft/minecraftnew/utils/commands/SkillsCommand.java†L52-L104】, this view presents player skill levels with progress lore and decorative panes filling empty slots.

## Creating New GUIs

1. **Inventory Setup**
   - Create the inventory with `Bukkit.createInventory(null, size, title)`.
   - Fill border slots with decorative panes to maintain a clean layout (see Merit GUI for an example).

2. **Item Preparation**
   - Use meaningful icons for each action or feature.
   - Set display names and lore with color codes to convey status.
   - Avoid leaving null slots; fill unused spaces with glass panes.

3. **Opening the GUI**
   - Call `player.openInventory(inv)` when activating the interface.
   - Store any needed state (such as the player’s current page or editing target) in a map keyed by UUID.

4. **Handling Clicks**
   - Register a listener for `InventoryClickEvent`.
   - Check `event.getView().getTitle()` to ensure the click belongs to your GUI.
   - Use `event.setCancelled(true)` to prevent unwanted item movement.
   - React to specific slots or item types to perform actions, then update or reopen the GUI if needed.

5. **Persistence**
   - For GUIs that store items, load and save contents using `YamlConfiguration` similar to `CustomBundleGUI`.
   - Save changes on `InventoryCloseEvent` and when the plugin disables.

6. **Reusability**
   - Encapsulate GUI logic in a dedicated manager class following the singleton pattern if multiple systems access it.
   - Keep text strings in one place to ease localization and future updates.

Following the patterns above will ensure new GUIs match the style of the existing ones and remain maintainable.
