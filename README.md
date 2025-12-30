# TaskMaster（任务大师）

TaskMaster 是一个基于 **Spigot / Bukkit API** 的多人团队任务竞赛插件。  
插件通过 **动态任务生成 + 队伍积分系统**，在生存模式下为玩家提供高节奏、可扩展、可平衡的任务对抗玩法。

本项目重点关注 **可配置性、游戏平衡性与可维护性**，所有任务池与难度修正均外置为 YML 文件，支持无需重新编译即可调参。

---

## ✨ 核心特性

- 🧩 **六大任务类型**
  - 找到指定方块（Blocks）
  - 背包中拥有指定物品（Items）
  - 击杀指定生物（Mobs）
  - 以指定方式死亡（Death Types）
  - 完成指定成就（Advancements）
  - 聊天输入指定内容（Chat）

- ⚖ **可配置 Bonus 加分系统**
  - 每个任务类型有基础分值（config.yml）
  - 每个具体任务可额外配置难度修正（bonuses/*.yml）
  - 最终得分 = 基础分 + Bonus

- 🏆 **团队竞赛机制**
  - 多队伍同时进行
  - 实时积分板（Scoreboard）
  - 达到目标分数的队伍获胜

- 📢 **实时任务展示**
  - ActionBar 显示当前任务摘要
  - 任务完成后即时刷新

---

## ⚙ 配置说明

### 基础分值（config.yml）
```yml
block-weight: 1
entity-weight: 1
have-item-weight: 1
death-type-weight: 1
complete-advancement-weight: 1
player-chat-weight: 1
```

### Bonus 示例（items.yml）
```yml
DIAMOND: 2
NETHERITE_INGOT: 4
ENDER_PEARL: 1
BREAD: 0
```

---

## 🎮 游戏流程

1. 玩家选择队伍
2. 管理员启动游戏
3. 每队获得 3 个随机任务
4. 完成任务获得积分并刷新新任务
5. 达到目标分数后游戏结束

---

## 🧪 版本兼容性(未测试)

| 版本 | 状态 |
|------|------|
| 1.16 | ⚠️ 需要精简配置 |
| 1.17–1.18 | ✅ |
| 1.19–1.20 | ✅ 推荐 |
| 1.21 | ⚠️ 需校验配置 |

---

## 🚀 安装

1. 将插件 jar 放入 plugins 目录
2. 启动服务器生成配置
3. 修改 config.yml 与 bonuses/*.yml
4. 重启服务器

---

## 📜 License

仅用于学习与非商业用途。
