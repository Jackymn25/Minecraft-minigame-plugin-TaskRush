package me.jacky.taskMaster.uscase;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Use case for joining a team.
 * Handles validating team selection, joining logic, and informational messages.
 */
public class JoinTeamUseCase {

    /** Team configuration manager. */
    private final TeamConfigManager teamConfigManager;

    /** Team id representing the "close" button in GUI. */
    private static final int CLOSE_BUTTON_TEAM_ID = 8;

    /**
     * Creates a new JoinTeamUseCase.
     *
     * @param inputTeamConfigManager team configuration manager
     */
    public JoinTeamUseCase(final TeamConfigManager inputTeamConfigManager) {
        this.teamConfigManager = inputTeamConfigManager;
    }

    /**
     * Handles a player's attempt to join a team based on GUI input.
     *
     * @param joinTeamInputData input data containing player and selected team id
     */
    public void join(final JoinTeamInputData joinTeamInputData) {
        Player player = joinTeamInputData.getPlayer();
        int teamId = joinTeamInputData.getRes();

        // If teamId is the close button, do nothing.
        if (teamId == CLOSE_BUTTON_TEAM_ID) {
            return;
        }

        // Validate teamId.
        String teamName = teamConfigManager.getTeamNameById(teamId);
        if (teamName == null) {
            player.sendMessage(ChatColor.RED + "无效的队伍选择！");
            return;
        }

        // Check capacity.
        if (teamConfigManager.isTeamFull(teamName)) {
            player.sendMessage(ChatColor.RED + "该队伍已满员，请选择其他队伍！");
            return;
        }

        // Previous team (if any).
        UUID playerId = player.getUniqueId();
        String previousTeam = teamConfigManager.getPlayerTeam(playerId);

        // Join the new team.
        boolean success = teamConfigManager.joinTeam(playerId, player.getName(), teamId);

        if (success) {
            String displayName = teamConfigManager.getTeamDisplayName(teamId);

            if (previousTeam != null && previousTeam.equals(teamName)) {
                player.sendMessage(
                        ChatColor.YELLOW + "你已经在"
                                + displayName
                                + ChatColor.YELLOW + "中了！"
                );
            } else {
                player.sendMessage(
                        ChatColor.GREEN + "你已成功加入"
                                + displayName
                                + ChatColor.GREEN + "！"
                );

                // Broadcast to other players (optional).
                String broadcastMessage = ChatColor.GRAY + player.getName()
                        + ChatColor.WHITE + " 加入了 " + displayName;
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(broadcastMessage));

                // Show team info.
                displayTeamInfo(player, teamName);
            }
        } else {
            player.sendMessage(ChatColor.RED + "加入队伍失败，请稍后再试！");
        }
    }

    /**
     * Displays selected team's information to the player.
     *
     * @param player player to receive messages
     * @param teamName team name key
     */
    private void displayTeamInfo(final Player player, final String teamName) {
        Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);

        player.sendMessage(ChatColor.GOLD + "========== 队伍信息 ==========");
        player.sendMessage(
                ChatColor.WHITE + "队伍: "
                        + teamInfo.get("color")
                        + teamInfo.get("display-name")
        );
        player.sendMessage(
                ChatColor.WHITE + "当前人数: "
                        + ChatColor.YELLOW + teamInfo.get("player-count")
                        + ChatColor.WHITE + "/" + teamInfo.get("max-players")
        );
        player.sendMessage(
                ChatColor.WHITE + "当前分数: "
                        + ChatColor.GREEN + teamInfo.get("score")
        );
        player.sendMessage(ChatColor.GOLD + "============================");
    }

    /**
     * Displays all teams information (for GUI display or other purposes).
     *
     * @param player player to receive messages
     */
    public void displayAllTeamsInfo(final Player player) {
        Map<String, Map<String, Object>> allTeams = teamConfigManager.getAllTeamsInfo();

        player.sendMessage(ChatColor.GOLD + "========== 所有队伍信息 ==========");
        allTeams.forEach((teamName, info) -> {
            boolean hasPlayers = (boolean) info.get("has-players");
            String status = hasPlayers
                    ? ChatColor.GREEN + "有玩家"
                    : ChatColor.RED + "空队伍";

            String line = String.format(
                    "%s%s %s- %s 人数: %s%d/%d %s- 分数: %s%d",
                    info.get("color"),
                    info.get("display-name"),
                    ChatColor.WHITE,
                    status,
                    ChatColor.YELLOW,
                    info.get("player-count"),
                    info.get("max-players"),
                    ChatColor.WHITE,
                    ChatColor.GREEN,
                    info.get("score")
            );

            player.sendMessage(line);
        });
        player.sendMessage(ChatColor.GOLD + "=================================");
    }
}
