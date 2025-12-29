package me.jacky.taskMaster;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Game {

    private final TeamConfigManager teamConfigManager;

    public Game(TeamConfigManager teamConfigManager) {
        this.teamConfigManager = teamConfigManager;
    }

    public void StartGame() {
        // 获取每个队伍里的玩家 整理血量 饱食度 血量上限 游戏模式（生存）开启死亡不掉落
        // 将每个队伍玩家随机传送到主世界的一个随机坐标 y = 200 给予缓降效果（足够到达地面的时间）
        // 给每个玩家速度1效果 跳跃2效果 持续60秒
        return;

    }

}
