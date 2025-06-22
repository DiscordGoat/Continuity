# Generator Subsystem Roadmap

This document divides the Generator feature into small pieces. After implementing each piece, compile
and test the plugin before moving to the next one. Debug logger messages are labelled with the
corresponding piece number.

## Piece 1 – Block Setup and GUI Opening
- Create `GeneratorSubsystem` with basic `onEnable` and `onDisable` methods.
- Register the skulk shrieker + bedrock block structure as a Generator.
- Open a placeholder GUI on right click.
- **Debug**: `Piece_1` message when a player opens the GUI.

**Task:** Build and load the plugin. Place a generator and verify the GUI opens and the `Piece_1` log appears.

## Piece 2 – GUI Layout
- Implement the Generator GUI showing ore buttons and nine Redstone Gem slots.
- Add a disabled "Begin Fabrication" button.
- **Debug**: `Piece_2` message when the GUI is constructed.

**Task:** Open the GUI in game and confirm the layout plus the `Piece_2` log output.

## Piece 3 – Mining Level Gating
- Enable ore buttons only when the player's mining level meets requirements.
- Show locked options with gray names if the level is too low.
- **Debug**: `Piece_3` message when a player selects an ore option.

**Task:** Test with characters at different mining levels to ensure gating works and logs print correctly.

## Piece 4 – Redstone Gem Item
- Implement the `Redstone Gem` item with unique UUID lore and default power 1,000.
- Prevent stacking by storing a random ID in the lore.
- **Debug**: `Piece_4` message whenever a gem is created or inserted into the GUI.

**Task:** Obtain a gem from a toolsmith, insert it into a generator, and confirm the `Piece_4` log appears.

## Piece 5 – Begin Fabrication and Session Creation
- Add click handling for the "Begin Fabrication" button.
- Create a `GeneratorTaskSession` storing location, ore type, progress and active gems.
- **Debug**: `Piece_5` message when a session starts.

**Task:** Start a session and watch the console for the `Piece_5` log entry.

## Piece 6 – Persistence to YAML
- Save active sessions to `generators.yml` on disable and load them on enable.
- Rebuild progress bars and tasks from the file during startup.
- **Debug**: `Piece_6` messages when sessions save and load.

**Task:** Restart the server and verify sessions persist along with the `Piece_6` logs.

## Piece 7 – Progress Display
- Spawn three armor stands showing percentage and remaining time above the generator.
- Update a progress bar every second.
- **Debug**: `Piece_7` message each time the progress updates.

**Task:** Observe the armor stands while the session runs and confirm the `Piece_7` messages.

## Piece 8 – Block Stages and Particles
- Change the bedrock/ore/block state and spawn particles according to the progress thresholds.
- **Debug**: `Piece_8` message whenever the stage changes.

**Task:** Run a session through several stages and ensure the visual changes and logs occur.

## Piece 9 – Power Consumption
- Drain Redstone Gems over time according to the selected ore type.
- Remove depleted gems and stall the session when no power remains.
- **Debug**: `Piece_9` message showing remaining power after each tick.

**Task:** Let a session run until a gem depletes and confirm the `Piece_9` log output.

## Piece 10 – Stall and Resume
- Allow players to resume a stalled session by right-clicking the generator.
- Keep progress intact when power returns.
- **Debug**: `Piece_10` message on stall and another on resume.

**Task:** Cause a stall (remove power) then resume it. Check for `Piece_10` logs in both cases.

## Piece 11 – Completion and Claiming
- When progress reaches 100%, stop sounds and enable a "Claim Perfect Mineral" button.
- Dropping the output resets the block to bedrock and ends the session.
- **Debug**: `Piece_11` message upon successful completion and item drop.

**Task:** Finish a full generation cycle and verify completion behavior and logs.

## Piece 12 – Pickup and Cancel
- Add GUI buttons to cancel an active session or pick up the generator item.
- Ensure sessions are removed and any unused gem power is returned.
- **Debug**: `Piece_12` message whenever a generator is picked up or a session is cancelled.

**Task:** Test both cancel and pickup actions to confirm final cleanup and logging.

---

Follow these pieces sequentially. After each implementation, run the plugin, test the new functionality, and confirm the corresponding `Piece_X` debug messages appear before proceeding to the next step.
