# Color‑Coding Guide for Perk Descriptions

This document explains the conventions and nuances behind our use of `ChatColor` codes in descriptions. Coding agents can refer to this guide when adding or updating descriptions to ensure consistent styling and semantic clarity.

---

## 1. Overview

- **Base text color**: `GRAY`  
  — Used for neutral or descriptive text that frames the descriptions.
- **Highlight colors**: convey the “flavor” or semantic category of each piece of information:
    - **`DARK_BLUE` / `BLUE` / `DARK_AQUA`**: Rare or arcane effects, action prompts
    - **`AQUA` / `DARK_PURPLE`**: Magical/elemental abilities or UI hints
    - **`YELLOW`**: Quantitative bonuses, chances, durations
    - **`GREEN`**: Positive resource or growth effects (food, health, drops)
    - **`RED`**: Damage, risk, negative side‑effects
    - **`DARK_GRAY`**: Debuffs, deterioration, darker themes
    - **`WHITE`**: Pure values, precise timings

---

## 2. Color Legend

| Color Constant     | Hex Example | Semantic Use                                    | Example Snippet                                                       |
|--------------------|-------------|-------------------------------------------------|------------------------------------------------------------------------|
| `GRAY`             | `#AAAAAA`   | Neutral framing and connective words            | `ChatColor.GRAY + "Compacts …"`                                       |
| `DARK_BLUE`        | `#0000AA`   | Item names, rare materials                      | `ChatColor.DARK_BLUE + "Compact Stone"`                               |
| `BLUE`             | `#5555FF`   | Action verbs/prompts (e.g. “Right Click:”)      | `ChatColor.BLUE + "Right Click: "`                                     |
| `DARK_PURPLE`      | `#AA00AA`   | Magical warp or spectral accents                | `ChatColor.DARK_PURPLE + "Warp forward."`                             |
| `AQUA`             | `#00AAAA`   | Elemental buffs (water, night vision, etc.)     | `ChatColor.AQUA + "Water Breathing"`                                   |
| `DARK_AQUA`        | `#008B8B`   | Special ranged assists or advanced mechanics    | `ChatColor.DARK_AQUA + "assist ranged attacks."`                      |
| `YELLOW`           | `#FFFF55`   | Percentages, chances, range/distance values     | `ChatColor.YELLOW + (level * 1) + "% chance"`                         |
| `GREEN`            | `#55FF55`   | Resource gains, health bonuses, sustainable effects | `ChatColor.GREEN + "double crops."`                                |
| `RED`              | `#FF5555`   | Damage bonuses, immunities, risk notifications  | `ChatColor.RED + "immunity to Fire Damage."`                          |
| `DARK_GRAY`        | `#555555`   | Negative effects, decay, slowdown               | `ChatColor.DARK_GRAY + "succumb faster."`                              |
| `WHITE`            | `#FFFFFF`   | Precise times/durations or neutral numeric values | `ChatColor.WHITE + "0.5s"`                                           |
| `GOLD`             | `#FFAA00`   | Treasure‑related highlights, rare drop chances  | `ChatColor.GOLD + (level * 0.1) + "%"`                                 |

---
the 2 best examples of dynamic descriptions are found in PetManager and SkillTreeManager, where dynamic descriptions are used to great effect.