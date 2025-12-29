package me.jacky.taskMaster.uscase;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinTeamUseCase {

    private final TeamConfigManager teamConfigManager;

    public JoinTeamUseCase(TeamConfigManager teamConfigManager) {
        this.teamConfigManager = teamConfigManager;
    }

    public void join(JoinTeamInputData joinTeamInputData) {
        Player player = joinTeamInputData.getPlayer();
        int teamId = joinTeamInputData.getRes();

        // 如果teamId是8，说明是关闭按钮，不处理
        if (teamId == 8) {
            return;
        }

        // 检查teamId是否有效
        String teamName = teamConfigManager.getTeamNameById(teamId);
        if (teamName == null) {
            player.sendMessage(ChatColor.RED + "无效的队伍选择！");
            return;
        }

        // 检查队伍是否已满
        if (teamConfigManager.isTeamFull(teamName)) {
            player.sendMessage(ChatColor.RED + "该队伍已满员，请选择其他队伍！");
            return;
        }

        // 获取玩家原来的队伍
        UUID playerId = player.getUniqueId();
        String previousTeam = teamConfigManager.getPlayerTeam(playerId);

        // 加入新队伍
        boolean success = teamConfigManager.joinTeam(playerId, player.getName(), teamId);

        if (success) {
            // 获取队伍显示名称
            String displayName = teamConfigManager.getTeamDisplayName(teamId);

            // 发送消息给玩家
            if (previousTeam != null && previousTeam.equals(teamName)) {
                player.sendMessage(ChatColor.YELLOW + "你已经在" + displayName + ChatColor.YELLOW + "中了！");
            } else {
                player.sendMessage(ChatColor.GREEN + "你已成功加入" + displayName + ChatColor.GREEN + "！");

                // 广播给其他玩家（可选）
                String broadcastMessage = ChatColor.GRAY + player.getName() +
                        ChatColor.WHITE + " 加入了 " + displayName;
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.sendMessage(broadcastMessage)
                );

                // 显示队伍信息
                displayTeamInfo(player, teamName);
            }
        } else {
            player.sendMessage(ChatColor.RED + "加入队伍失败，请稍后再试！");
        }
    }

    /**
     * 显示队伍信息
     */
    private void displayTeamInfo(Player player, String teamName) {
        var teamInfo = teamConfigManager.getTeamInfo(teamName);

        player.sendMessage(ChatColor.GOLD + "========== 队伍信息 ==========");
        player.sendMessage(ChatColor.WHITE + "队伍: " +
                teamInfo.get("color") + teamInfo.get("display-name"));
        player.sendMessage(ChatColor.WHITE + "当前人数: " +
                ChatColor.YELLOW + teamInfo.get("player-count") +
                ChatColor.WHITE + "/" + teamInfo.get("max-players"));
        player.sendMessage(ChatColor.WHITE + "当前分数: " +
                ChatColor.GREEN + teamInfo.get("score"));
        player.sendMessage(ChatColor.GOLD + "============================");
    }

    /**
     * 获取所有队伍信息（用于GUI显示或其他用途）
     */
    public void displayAllTeamsInfo(Player player) {
        var allTeams = teamConfigManager.getAllTeamsInfo();

        player.sendMessage(ChatColor.GOLD + "========== 所有队伍信息 ==========");
        allTeams.forEach((teamName, info) -> {
            String status = (boolean)info.get("has-players") ?
                    ChatColor.GREEN + "有玩家" : ChatColor.RED + "空队伍";
            player.sendMessage(String.format(
                    "%s%s %s- 人数: %s%d/%d %s- 分数: %s%d",
                    info.get("color"), info.get("display-name"),
                    ChatColor.WHITE,
                    ChatColor.YELLOW, info.get("player-count"), info.get("max-players"),
                    ChatColor.WHITE,
                    ChatColor.GREEN, info.get("score")
            ));
        });
        player.sendMessage(ChatColor.GOLD + "=================================");
    }
}