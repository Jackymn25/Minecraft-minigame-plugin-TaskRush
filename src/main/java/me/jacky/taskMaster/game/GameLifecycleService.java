package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameLifecycleService {

    private static final int START_TITLE_FADE_IN = 10;
    private static final int START_TITLE_STAY = 70;
    private static final int START_TITLE_FADE_OUT = 20;

    private static final long END_DELAY_TICKS = 200L;

    private final JavaPlugin plugin;
    private final TeamConfigManager teamConfigManager;
    private final ScoreboardService scoreboardService;

    public GameLifecycleService(JavaPlugin plugin,
                                TeamConfigManager teamConfigManager,
                                ScoreboardService scoreboardService) {
        this.plugin = plugin;
        this.teamConfigManager = teamConfigManager;
        this.scoreboardService = scoreboardService;
    }

    /**
     * 强制结束并结算当前局。
     */
    public void cancelAndSettle(String operatorName,
                                Map<String, TeamState> teamTasks,
                                Runnable endGameNoStats) {

        Map<String, Integer> scoreSnapshot = new HashMap<>();
        Map<String, Integer> completedSnapshot = new HashMap<>();

        for (Map.Entry<String, TeamState> entry : teamTasks.entrySet()) {
            scoreSnapshot.put(entry.getKey(), entry.getValue().getScore());
            completedSnapshot.put(entry.getKey(), entry.getValue().getCompletedTasks().size());
        }

        Bukkit.broadcastMessage("§c§lTaskMaster 已被管理员强制结束！§7(" + operatorName + ")");
        Bukkit.broadcastMessage("§6§l最终统计：");

        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(scoreSnapshot.entrySet());
        ranking.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int maxRank = Math.min(8, ranking.size());
        for (int i = 0; i < maxRank; i++) {
            String team = ranking.get(i).getKey();
            int score = ranking.get(i).getValue();

            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(team);
            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            int done = completedSnapshot.getOrDefault(team, 0);
            Bukkit.broadcastMessage(
                    "§e" + (i + 1) + ". " + color + displayName
                            + " §7- §b" + score + " §7分  §8(完成: §f" + done + "§8)"
            );
        }

        endGameNoStats.run();
    }

    /**
     * 宣布胜利（会延迟调用 endGameWithStats）。
     */
    public void declareWinner(String teamName,
                              TeamState teamState,
                              Runnable endGameWithStats) {

        Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
        String color = (String) teamInfo.get("color");
        String displayName = (String) teamInfo.get("display-name");

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════");
        Bukkit.broadcastMessage(ChatColor.GOLD + "      🎉 游戏结束！ 🎉");
        Bukkit.broadcastMessage(color + displayName + ChatColor.GOLD + " 队伍获得胜利！");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "最终分数: " + ChatColor.GREEN + teamState.getScore() + "分");
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════");
        Bukkit.broadcastMessage("");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.sendTitle(
                    color + displayName + " 胜利！",
                    ChatColor.YELLOW + "分数: " + teamState.getScore(),
                    START_TITLE_FADE_IN,
                    START_TITLE_STAY,
                    START_TITLE_FADE_OUT
            );
        }

        Bukkit.getScheduler().runTaskLater(plugin, endGameWithStats, END_DELAY_TICKS);
    }

    /**
     * 游戏结束：清理数据、计分板、队伍归属。
     */
    public void endGame(boolean showStats,
                        Map<String, TeamState> teamTasks) {

        plugin.getConfig().set("game-status", false);
        plugin.saveConfig();

        if (showStats) {
            showFinalStatistics(teamTasks);
        }

        scoreboardService.cleanup();

        for (Player player : Bukkit.getOnlinePlayers()) {
            teamConfigManager.removePlayerFromAllTeams(player.getUniqueId());
        }

        teamTasks.clear();
        Bukkit.broadcastMessage(ChatColor.GRAY + "游戏已结束，感谢参与！");
    }

    private void showFinalStatistics(Map<String, TeamState> teamTasks) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════ 最终统计 ════════════");

        for (String teamName : teamTasks.keySet()) {
            TeamState task = teamTasks.get(teamName);
            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);

            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            Bukkit.broadcastMessage(
                    color + displayName
                            + ChatColor.WHITE + " - 分数: " + ChatColor.GREEN + task.getScore()
                            + ChatColor.WHITE + " - 完成任务: " + ChatColor.YELLOW + task.getCompletedTasks().size()
            );
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════════════");
    }
}
