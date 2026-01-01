package me.jacky.taskMaster.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.Map;

public final class ScoreboardService {

    private Scoreboard scoreboard;
    private Objective objective;

    public Scoreboard getScoreboardOrNull() {
        return scoreboard;
    }

    public void create(final Map<String, TeamState> teamTasks,
                       final Map<String, Map<String, Object>> teamInfoMap) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective(
                "tasks",
                "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "TaskMaster"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String teamName : teamTasks.keySet()) {
            TeamState task = teamTasks.get(teamName);

            Map<String, Object> info = teamInfoMap.get(teamName);
            if (info == null) continue;

            String color = (String) info.get("color");
            String displayName = (String) info.get("display-name");

            // create score line
            String scoreLine = color + displayName + ": " + ChatColor.GREEN + task.getScore();
            Score score = objective.getScore(scoreLine);
            score.setScore(0);

            // create team (for color in sidebar)
            Team team = scoreboard.registerNewTeam(teamName.substring(0, Math.min(teamName.length(), 16)));
            team.setColor(getChatColorFromColorCode(color));
        }
    }

    public void applyToPlayers(final Collection<Player> players) {
        if (scoreboard == null) return;
        for (Player p : players) {
            p.setScoreboard(scoreboard);
        }
    }

    public void applyToPlayer(final Player player) {
        if (scoreboard == null) return;
        player.setScoreboard(scoreboard);
    }

    public void updateTeamScore(final String color,
                                final String displayName,
                                final int scoreValue) {
        if (objective == null || scoreboard == null) return;

        // remove old entries for this team line
        for (String entry : scoreboard.getEntries()) {
            if (entry.startsWith(color + displayName + ":")) {
                scoreboard.resetScores(entry);
            }
        }

        String newScoreLine = color + displayName + ": " + ChatColor.GREEN + scoreValue;
        Score score = objective.getScore(newScoreLine);
        score.setScore(0);
    }

    public void cleanup() {
        if (scoreboard == null) return;

        // reset player scoreboards
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard empty = (manager == null) ? null : manager.getNewScoreboard();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (empty != null) player.setScoreboard(empty);
        }

        for (Objective obj : scoreboard.getObjectives()) {
            obj.unregister();
        }
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }

        scoreboard = null;
        objective = null;
    }

    private ChatColor getChatColorFromColorCode(final String colorCode) {
        if (colorCode == null) return ChatColor.WHITE;

        if (colorCode.contains("§a")) return ChatColor.GREEN;
        if (colorCode.contains("§e")) return ChatColor.YELLOW;
        if (colorCode.contains("§c")) return ChatColor.RED;
        if (colorCode.contains("§9")) return ChatColor.BLUE;
        if (colorCode.contains("§5")) return ChatColor.DARK_PURPLE;
        if (colorCode.contains("§b")) return ChatColor.AQUA;
        if (colorCode.contains("§d")) return ChatColor.LIGHT_PURPLE;
        if (colorCode.contains("§f")) return ChatColor.WHITE;
        return ChatColor.WHITE;
    }
}
