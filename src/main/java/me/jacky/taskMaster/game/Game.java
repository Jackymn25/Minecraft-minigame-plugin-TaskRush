package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.BonusManager;
import me.jacky.taskMaster.text.TaskTextFormatter;
import me.jacky.taskMaster.view.ActionbarTaskTicker;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 任务大师游戏核心类。
 * 负责管理游戏逻辑、任务分配、分数跟踪和胜利条件。
 * 采用动态任务系统：任务完成后立即刷新新任务。
 * 胜利条件：最先达到20分的队伍获胜。
 */
public final class Game {

    private static final int TASKS_PER_TEAM = 3;
    private static final int DEFAULT_WIN_SCORE = 30;
    private static final String CFG_GAME_TARGET_POINTS = "game-target-points";
    private static final int ACTIONBAR_MAX_TASKS = 3;

    /** Advancement task prefix used in task strings. */
    private static final String TASKKEY_ADV_PREFIX = "ADVANCEMENT:";

    /** Safety guard to avoid infinite loops when auto-completing tasks. */
    private static final int MAX_AUTOCOMPLETE_CHAIN = 10;

    /** 队伍配置管理器。 */
    private final TeamConfigManager teamConfigManager;
    /** 随机数生成器。 */
    private final Random random = new Random();
    /** 插件主类引用。 */
    private final JavaPlugin plugin;
    /** ActionBar ticker。 */
    private ActionbarTaskTicker actionbarTicker;
    /** 存储每个队伍的任务和分数。 */
    private final Map<String, TeamState> teamTasks = new HashMap<>();
    /** 计分板 **/
    private final ScoreboardService scoreboardService = new ScoreboardService();
    /** 游戏是否正在进行。 */
    private boolean gameRunning = false;
    /** 获胜队伍名称。 */
    private String winningTeam = null;
    private int winScore = DEFAULT_WIN_SCORE;

    /** Utils / Services :) **/
    private final BonusManager bonusManager;
    private final TaskGenerator taskGenerator;
    private final PointsCalculator pointsCalculator;
    private final TaskTextFormatter formatter;
    private final PlayerSetupService playerSetupService;
    private final TeleportService teleportService;
    private final GameLifecycleService lifecycleService;

    /**
     * 获取队伍配置管理器。
     *
     * @return 队伍配置管理器。
     */
    public TeamConfigManager getTeamConfigManager() {
        return teamConfigManager;
    }

    /**
     * 构造函数。可以改成builder format 但不想干
     *
     * @param teamConfigManager 队伍配置管理器。
     * @param plugin 插件主类。
     */
    public Game(final TeamConfigManager teamConfigManager,
                final JavaPlugin plugin,
                final BonusManager bonusManager,
                final TaskTextFormatter formatter) {
        this.teamConfigManager = teamConfigManager;
        this.plugin = plugin;
        this.bonusManager = bonusManager;
        this.formatter = formatter;
        this.taskGenerator = new TaskGenerator(plugin, bonusManager, random);
        this.pointsCalculator = new PointsCalculator(plugin, bonusManager);
        this.playerSetupService = new PlayerSetupService(plugin, teamConfigManager, formatter, scoreboardService);
        this.teleportService = new TeleportService(plugin, teamConfigManager);
        this.lifecycleService = new GameLifecycleService(plugin, teamConfigManager, scoreboardService);
    }

    public BonusManager getBonusManager() {
        return bonusManager;
    }

    /**
     * 开始游戏。
     */
    public void startGame() {
        if (!hasAnyTeamPlayerOnline()) {
            Bukkit.broadcastMessage(
                    ChatColor.RED
                            + "无法开始：没有任何玩家选择队伍（全员旁观者）。"
            );
            Bukkit.broadcastMessage(
                    ChatColor.GRAY
                            + "请先使用 /jointeam 选择队伍后再开始。"
            );

            plugin.getConfig().set("game-status", false);
            plugin.saveConfig();
            return;
        }

        // 每局开始前 reload 一次，保证 config 修改能生效
        plugin.reloadConfig();
        taskGenerator.reloadWeightsFromConfig();
        bonusManager.reload();

        winScore = plugin.getConfig().getInt(CFG_GAME_TARGET_POINTS, DEFAULT_WIN_SCORE);
        if (winScore <= 0) {
            winScore = DEFAULT_WIN_SCORE;
        }

        gameRunning = true;
        winningTeam = null;

        initializeTeamTasks();
        createScoreboard();
        playerSetupService.setupTeamPlayers(teamTasks);
        playerSetupService.setupSpectators();
        teleportService.teleportTeams(Bukkit.getWorld("world"));
        playerSetupService.applyPotionEffectsToTeamPlayers();
        actionbarTicker = new ActionbarTaskTicker(plugin, this);
        actionbarTicker.start();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "任务大师游戏开始！");
        Bukkit.broadcastMessage(
                ChatColor.YELLOW + "规则：最先达到" + winScore + "分的队伍获胜！"
        );
        Bukkit.broadcastMessage(ChatColor.YELLOW + "完成任务会立即刷新新任务！");
    }

    private void initializeTeamTasks() {
        Map<String, Map<String, Object>> allTeams = teamConfigManager.getAllTeamsInfo();
        // red, green ...
        for (String teamName : allTeams.keySet()) {
            teamTasks.put(teamName, new TeamState(TASKS_PER_TEAM, this::generateNewTask));

            // 初始化后如果刷到了“已获得的成就”，直接补分并继续刷新
            autoCompleteAdvancementTasks(teamName);

            Bukkit.broadcastMessage("队伍 " + teamName + " 的任务:");
            for (String taskRaw : teamTasks.get(teamName).getActiveTasks()) {
                Bukkit.broadcastMessage("  - " + formatter.toDisplay(taskRaw));
            }
        }
    }

    private String generateNewTask() {
        return taskGenerator.nextTaskKey();
    }

    private void createScoreboard() {
        // team info map: 原来每次都 getTeamInfo，这里一次性拿全
        Map<String, Map<String, Object>> allTeamsInfo = teamConfigManager.getAllTeamsInfo();

        scoreboardService.create(teamTasks, allTeamsInfo);
        scoreboardService.applyToPlayers(teamConfigManager.getAllOnlineTeamPlayers());
    }

    public void updateTeamScore(final String teamName) {
        TeamState task = teamTasks.get(teamName);
        if (task == null) return;

        Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
        if (teamInfo == null) return;

        String color = (String) teamInfo.get("color");
        String displayName = (String) teamInfo.get("display-name");

        scoreboardService.updateTeamScore(color, displayName, task.getScore());
    }

    private boolean hasAnyTeamPlayerOnline() {
        Map<String, List<Player>> teamPlayers =
                teamConfigManager.getTeamOnlinePlayersMap();

        for (List<Player> players : teamPlayers.values()) {
            if (players != null && !players.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getTeamActiveTasks(final String teamName) {
        TeamState task = teamTasks.get(teamName);
        if (task != null) {
            return task.getActiveTasks();
        }
        return new ArrayList<>();
    }

    public void addScoreToTeam(final String teamName, final int points) {
        if (!gameRunning) {
            return;
        }

        TeamState task = teamTasks.get(teamName);
        if (task == null || task.hasWon()) {
            return;
        }

        task.addScore(points);

        updateTeamScore(teamName);

        if (task.getScore() >= winScore) {
            if (winningTeam == null) {
                winningTeam = teamName;
                gameRunning = false;
                task.setHasWon(true);

                lifecycleService.declareWinner(teamName, task, () -> endGame(true));
            }
        }

        List<Player> players = teamConfigManager.getOnlinePlayersByTeam(teamName);
        for (Player player : players) {
            player.sendMessage(ChatColor.GREEN + "你的队伍获得了 " + points + " 分！");
            player.sendMessage(
                    ChatColor.GREEN
                            + "当前总分: "
                            + ChatColor.YELLOW
                            + task.getScore()
                            + ChatColor.GREEN
                            + "/" + winScore
            );
        }
    }

    public int getTeamScore(final String teamName) {
        TeamState task = teamTasks.get(teamName);
        if (task != null) {
            return task.getScore();
        }
        return 0;
    }

    public boolean completeTeamTask(
            final String teamName,
            final String task
    ) {
        if (!gameRunning) {
            return false;
        }

        TeamState teamTask = teamTasks.get(teamName);
        if (teamTask == null) {
            return false;
        }

        int points = pointsCalculator.pointsFor(task);
        boolean success = teamTask.completeTask(task, points);

        if (success) {
            addScoreToTeam(teamName, points);

            // 刷新任务后，若刷出“已完成成就任务”，直接补分并再刷新
            autoCompleteAdvancementTasks(teamName);

            notifyNewTask(teamName, teamTask.getActiveTasks());
            Bukkit.broadcastMessage(
                    teamName
                            + " 完成了一项任务,获得"
                            + points
                            + "分"
            );

            broadcastAllTeamTasks();

            return true;
        }
        return false;
    }

    /**
     * 广播所有队伍的当前任务（只读展示，不修改数据）。
     */
    public void broadcastAllTeamTasks() {
        Bukkit.broadcastMessage("§8━━━━━━━━━━ §6当前任务一览 §8━━━━━━━━━━");

        for (Map.Entry<String, TeamState> entry : teamTasks.entrySet()) {
            String teamName = entry.getKey();
            TeamState task = entry.getValue();

            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            Bukkit.broadcastMessage(color + "§l" + displayName + " 队伍:");

            List<String> tasks = task.getActiveTasks();
            for (int i = 0; i < tasks.size(); i++) {
                String display = formatter.toDisplay(tasks.get(i));
                Bukkit.broadcastMessage("  §f" + (i + 1) + ". §e" + display);
            }

            Bukkit.broadcastMessage("");
        }

        Bukkit.broadcastMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void notifyNewTask(final String teamName, final List<String> activeTasks) {
        List<Player> players = teamConfigManager.getOnlinePlayersByTeam(teamName);

        for (Player player : players) {
            player.sendMessage(ChatColor.AQUA + "✧ 任务已刷新！新任务列表:");
            for (int i = 0; i < activeTasks.size(); i++) {
                player.sendMessage(
                        ChatColor.YELLOW
                                + Integer.toString(i + 1)
                                + ". "
                                + ChatColor.WHITE
                                + formatter.toDisplay(activeTasks.get(i))
                );
            }
        }
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public String getWinningTeam() {
        return winningTeam;
    }

    public String buildTeamTaskActionbar(final String teamName) {
        List<String> tasks = getTeamActiveTasks(teamName);
        if (tasks == null || tasks.isEmpty()) {
            return "§7暂无任务";
        }

        int max = Math.min(ACTIONBAR_MAX_TASKS, tasks.size());
        List<String> shortTasks = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            String display = formatter.toDisplay(tasks.get(i));

            if (display.length() > 18) {
                display = display.substring(0, 18) + "...";
            }

            shortTasks.add(display);
        }

        return "§b任务 §7| §f" + String.join(" §8• §f", shortTasks);
    }

    /**
     * 自动完成：队伍当前活跃任务里，如果刷到了“已拥有的成就”，则直接加分并继续刷新。
     */
    private void autoCompleteAdvancementTasks(final String teamName) {
        int guard = 0;
        boolean completedAny;

        do {
            completedAny = false;

            for (String task : new ArrayList<>(getTeamActiveTasks(teamName))) {
                if (!task.startsWith(TASKKEY_ADV_PREFIX)) {
                    continue;
                }

                String key = task.substring(TASKKEY_ADV_PREFIX.length()).trim();
                if (!teamHasAdvancement(teamName, key)) {
                    continue;
                }

                TeamState teamTask = teamTasks.get(teamName);
                if (teamTask == null) {
                    return;
                }

                int points = pointsCalculator.pointsFor(task);

                boolean success = teamTask.completeTask(task, points);
                if (success) {
                    addScoreToTeam(teamName, points);

                    Bukkit.broadcastMessage(
                            teamName
                                    + " 自动完成成就任务: "
                                    + key
                                    + " (已获得), +"
                                    + points
                                    + "分"
                    );

                    notifyNewTask(teamName, teamTask.getActiveTasks());
                    completedAny = true;
                    break;
                }
            }

            guard++;
        } while (completedAny && guard < MAX_AUTOCOMPLETE_CHAIN);
    }

    /**
     * 判断队伍内任意在线玩家是否已经完成某个成就（key 形如 "story/mine_diamond"）。
     */
    private boolean teamHasAdvancement(
            final String teamName,
            final String advancementKey
    ) {
        NamespacedKey key = NamespacedKey.minecraft(advancementKey);
        Advancement adv = Bukkit.getAdvancement(key);
        if (adv == null) {
            return false;
        }

        for (Player player : teamConfigManager.getOnlinePlayersByTeam(teamName)) {
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            if (progress.isDone()) {
                return true;
            }
        }
        return false;
    }

    public void cancelAndSettle(final String operatorName) {
        if (!gameRunning) return;

        if (actionbarTicker != null) {
            actionbarTicker.stop();
            actionbarTicker = null;
        }
        gameRunning = false;

        lifecycleService.cancelAndSettle(operatorName, teamTasks, () -> endGame(false));

        // 注意：endGame 内部会 teamTasks.clear()，这里不要再 clear
        winningTeam = null;
    }

    public void endGame(final boolean showStats) {
        if (!gameRunning && winningTeam == null && teamTasks.isEmpty()) return;
        plugin.getConfig().set("game-status", false);
        plugin.saveConfig();
        if (actionbarTicker != null) {
            actionbarTicker.stop();
            actionbarTicker = null;
        }

        gameRunning = false;
        winningTeam = null;

        lifecycleService.endGame(showStats, teamTasks);
    }
}
