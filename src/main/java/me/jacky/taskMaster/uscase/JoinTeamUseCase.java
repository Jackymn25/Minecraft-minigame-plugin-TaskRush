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
            player.sendMessage(ChatColor.RED + "INVALID TEAM ID");
            return;
        }

        // Check capacity.
        if (teamConfigManager.isTeamFull(teamName)) {
            player.sendMessage(ChatColor.RED + "Team is full");
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
                        ChatColor.YELLOW + "You are already in this team"
                                + displayName
                );
            } else {
                player.sendMessage(
                        ChatColor.GREEN + "You joined the team"
                                + displayName
                                + ChatColor.GREEN + "！"
                );

                // Broadcast to other players (optional).
                String broadcastMessage = ChatColor.GRAY + player.getName()
                        + ChatColor.WHITE + " joined " + displayName;
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(broadcastMessage));

                // Show team info.
                displayTeamInfo(player, teamName);
            }
        } else {
            player.sendMessage(ChatColor.RED + "failed to join team, try again");
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

        player.sendMessage(ChatColor.GOLD + "========== Team Info ==========");
        player.sendMessage(
                ChatColor.WHITE + "Team Name: "
                        + teamInfo.get("color")
                        + teamInfo.get("display-name")
        );
        player.sendMessage(
                ChatColor.WHITE + "Team size: "
                        + ChatColor.YELLOW + teamInfo.get("player-count")
                        + ChatColor.WHITE + "/" + teamInfo.get("max-players")
        );
        player.sendMessage(
                ChatColor.WHITE + "pts: "
                        + ChatColor.GREEN + teamInfo.get("score")
        );
        player.sendMessage(ChatColor.GOLD + "============================");
    }
}
