package me.jacky.taskMaster.uscase;

import org.bukkit.entity.Player;

public class JoinTeamInputData {
    private Player player;
    private int res;
    public JoinTeamInputData(Player player, int res) {
        this.player = player;
        this.res = res;
    }

    public Player getPlayer() {
        return player;
    }

    public int getRes() {
        return res;
    }
}
