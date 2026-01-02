# TaskMaster

[查看中文版本](./README.md)

TaskMaster is a **team-based task race** plugin for **Spigot/Paper** servers. Players join teams, complete randomly generated tasks to earn points, and the first team to reach the target score wins. The plugin includes multi-language support (Chinese by default) and allows customization of task probabilities and scoring rules via configuration files.

---

## Features
- **Team competition** with multiple teams and live scoring.
- **Random task pool**, including:
  - Block tasks (break / obtain)
  - Mob hunting
  - Item collection
  - Specific death causes
  - Advancements
- **Point system**: teams gain points on completion; supports **base points** + **bonus points**.
- **Configurable**: tune category probabilities, target score, and per-category point weights.
- **Multi-language**: built-in `zh_cn / en_us / fr_fr / ja_jp / ko_kr`.
- **Player-friendly UX**: team selection GUI, scoreboard display, etc. (depending on the implementation).

---

## Installation
1. Put **TaskMaster.jar** into your server `plugins/` folder.
2. Restart the server; default configs/resources will be generated.
3. After editing configs:
   - **Recommended**: restart the server to apply changes;
   - For language switching only, use `/reload_tm <language>` (do **not** use the server-wide `/reload`).

> Recommended: **Spigot/Paper 1.20+** (built against the 1.20 API).

---

## Commands

| Command | Description |
| --- | --- |
| `/jointeam` | Open the team selection GUI |
| `/taskmaster` | Start a TaskMaster match |
| `/canceltaskmaster` | Cancel the ongoing match and finalize scoring |
| `/reload_tm [lang]` | Reload config + language; optionally switch language (e.g., `en_us`) (Tab-completion supported) |
| `/ping [player]` | Show ping (ms) for yourself or a player |

---

## What You Can Configure
- **Language**: Simplified Chinese, English, French, Japanese, Korean
- **Gameplay settings**: target score, task probability weights, scoring weights
- **Custom tasks & bonus points**: e.g., add new death causes like “dragon breath” (requires the correct unique identifier string)

---

## `config.yml` Example
```yml
language: zh_cn
game-target-points: 30

# Category probability weights (higher = more frequent)
block-probability-weight: 19
entity-hunt-probability-weight: 19
have-item-probability-weight: 19
death-type-probability-weight: 19
complete-advancement-probability-weight: 19
type-chat-probability-weight: 5

# Base points per category
block-weight: 1
entity-weight: 1
have-item-weight: 1
death-type-weight: 1
complete-advancement-weight: 1
player-chat-weight: 1
```

Notes:
- `*-probability-weight`: relative chance of each task category (higher = more frequent)
- `*-weight`: base points awarded for completing a task in that category
- `game-target-points`: winning score threshold
- `language`: plugin language (e.g., `zh_cn`, `en_us`)

---

## `advancements.yml` Example
```yml
# Diamonds! (obtain a diamond) bonus +2 (base is 1), total = 3
story/mine_diamond: 2

# Obtain Blaze Rod
nether/obtain_blaze_rod: 5

# Local Brewery (brew a potion)
nether/brew_potion: 5

# A Terrible Fortress (find a Nether Fortress)
nether/find_fortress: 4
```

- Keys like `story/mine_diamond` are **Resource locations** (unique identifiers).
- How to find them: on the Minecraft Wiki Advancement page, `Ctrl + F` for **Resource location**.
- The same idea applies to other configs (e.g., `deaths.yml`, `blocks.yml`, etc.).

---

## Task Lists & Bonus Points (`resources/bonuses/`)
Task targets and **bonus scores** are defined in YAML files under `resources/bonuses/` (common filenames):
- `blocks.yml`
- `mobs.yml`
- `items.yml`
- `deaths.yml`
- `advancements.yml`

Targets not listed will **not** be selected as tasks. You can add/remove entries and tweak bonus points in these files.

---

## Languages (`resources/lang/`)
Language packs live in `resources/lang/` (e.g., `zh_cn.yml`, `en_us.yml`).  
You can customize messages and run `/reload_tm <language>` to apply updates (UTF-8 recommended).

---

## Author & Credits
- Author: **Jacky**
- Thanks to the SpigotMC community and all testers for feedback and support.
