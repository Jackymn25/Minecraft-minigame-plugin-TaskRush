# TaskMaster

[View English version](./readme_en.md)

[小编的Spigot资源链接](https://www.spigotmc.org/resources/taskmaster.131364/)

TaskMaster 是一个面向 **Spigot/Paper** 服务端的 **团队任务竞赛** 插件。玩家加入队伍后，通过完成随机任务获得积分，最先达到目标分数的队伍获胜。插件内置多语言（默认中文），并支持通过配置文件自定义任务概率与得分规则。

---

## 功能特性
- **团队竞赛**：支持多队伍加入与计分。
- **随机任务池**：任务类型包含：
  - 方块相关（破坏 / 获取）
  - 生物猎杀
  - 物品收集
  - 特定死亡类型
  - 进度/成就（Advancement）
- **积分系统**：完成任务为队伍加分；支持**基础分**与**额外奖励分**。
- **可配置**：可调整任务类别出现概率、目标分数、不同任务类别的得分权重。
- **多语言**：内置 `zh_cn / en_us / fr_fr / ja_jp / ko_kr` 语言文件。
- **友好交互**：队伍选择 GUI、计分板展示等（以插件实现为准）。

---

## 安装方法
1. 将 **TaskMaster.jar** 放入服务器 `plugins/` 目录。
2. 重启服务器，插件会自动生成默认配置与资源文件。
3. 修改配置后：
   - **推荐**重启服务器使更改生效；
   - 若仅切换语言，可使用 `/reload_tm <language>`（不推荐使用服务端的 `/reload`）。

> 建议使用 **1.20 或更高版本** 的 Spigot/Paper 服务端（插件基于 1.20 API 开发）。

---

## 指令说明

| 指令 | 说明 |
| --- | --- |
| `/jointeam` | 打开队伍选择 GUI |
| `/taskmaster` | 开始一局 TaskMaster 游戏 |
| `/canceltaskmaster` | 取消正在进行的游戏并结算 |
| `/reload_tm [lang]` | 重载配置与语言；可选切换语言（如 `en_us`）（支持 Tab 自动提示） |
| `/ping [player]` | 查看自己或指定玩家延迟（ms） |

---

## 可配置说明
你可以在配置中调整：
- **语言**：支持中文简体、英文、法文、日文、韩文
- **游戏参数**：目标分数、任务概率权重、得分权重等
- **自定义任务内容与额外加分**：例如在死亡类任务配置中新增“死于龙息”等（需填写对应的唯一标识字符串）

---

## 配置（config.yml）示例
```yml
language: zh_cn
game-target-points: 30

# 任务类别出现概率权重（越大越常出现）
block-probability-weight: 19
entity-hunt-probability-weight: 19
have-item-probability-weight: 19
death-type-probability-weight: 19
complete-advancement-probability-weight: 19
type-chat-probability-weight: 5

# 各类别任务完成后的基础得分
block-weight: 1
entity-weight: 1
have-item-weight: 1
death-type-weight: 1
complete-advancement-weight: 1
player-chat-weight: 1
```

参数说明：
- `*-probability-weight`：控制任务类别出现概率（数值越大越常见）
- `*-weight`：该类别任务完成后的基础得分
- `game-target-points`：获胜目标分数
- `language`：插件语言（如 `zh_cn`, `en_us`）

---

## 配置（advancements.yml）示例
```yml
# 钻石！（获得钻石）额外加分 2（基础分默认 1），总得分 3
story/mine_diamond: 2

# 获得烈焰棒
nether/obtain_blaze_rod: 5

# 本地酿造（酿造药水）
nether/brew_potion: 5

# 可怕的堡垒（找到下界要塞）
nether/find_fortress: 4
```

- 以上 `story/mine_diamond` 等为 **Resource location**（唯一标识字符串）。
- 获取方式：在 Minecraft Wiki 的 Advancement 页面中 `Ctrl + F` 搜索 **Resource location** 即可找到对应值。
- 其他配置（如 `deaths.yml`, `blocks.yml` 等）同理。

---

## 任务奖励与任务列表（resources/bonuses/）
插件在 `resources/bonuses/` 下按类别提供任务目标与**额外奖励分**（常见文件名）：
- `blocks.yml`
- `mobs.yml`
- `items.yml`
- `deaths.yml`
- `advancements.yml`

未列出的目标**不会**被选为任务；你可以在这些 YAML 中增删条目或调整奖励分。

---

## 多语言（resources/lang/）
语言文件位于 `resources/lang/`（如 `zh_cn.yml`, `en_us.yml`）。  
可自行修改提示文本并用 `/reload_tm <language>` 让更改生效（建议 UTF-8）。

---

## 作者 / Credits
- Author: **Jacky**
- 感谢 SpigotMC 社区与所有测试玩家的反馈与支持。
