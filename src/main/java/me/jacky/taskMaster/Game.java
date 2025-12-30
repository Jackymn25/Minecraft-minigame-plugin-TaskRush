package me.jacky.taskMaster;

import me.jacky.taskMaster.config.BonusManager;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务大师游戏核心类。
 * 负责管理游戏逻辑、任务分配、分数跟踪和胜利条件。
 * 采用动态任务系统：任务完成后立即刷新新任务。
 * 胜利条件：最先达到20分的队伍获胜。
 */
public final class Game {

    private static final int TASKS_PER_TEAM = 3;
    private static final int DEFAULT_WIN_SCORE = 20;
    private static final String CFG_GAME_TARGET_POINTS = "game-target-points";
    private static final int ACTIONBAR_MAX_TASKS = 3;

    private static final long TIME_MORNING = 0L;

    private static final double BASE_MAX_HEALTH = 20.0;
    private static final double BASE_HEALTH = 20.0;
    private static final int BASE_FOOD = 20;
    private static final float BASE_SATURATION = 10.0f;

    private static final int START_TITLE_FADE_IN = 10;
    private static final int START_TITLE_STAY = 70;
    private static final int START_TITLE_FADE_OUT = 20;

    private static final int START_BREAD_AMOUNT = 16;

    private static final int SPEED_DURATION_TICKS = 1200;
    private static final int JUMP_DURATION_TICKS = 1200;

    private static final int SLOW_FALL_TICKS = 540;

    private static final double TELEPORT_OFFSET_MIN = -3.0;
    private static final double TELEPORT_OFFSET_MAX = 3.0;

    private static final int RANDOM_LOC_TRIES = 50;
    private static final int RAND_XZ_MIN = -50000;
    private static final int RAND_XZ_MAX = 50000;

    private static final long END_DELAY_TICKS = 200L;

    /** Advancement task prefix used in task strings. */
    private static final String ACHIEVEMENT_TASK_PREFIX = "完成成就: ";

    /** Safety guard to avoid infinite loops when auto-completing tasks. */
    private static final int MAX_AUTOCOMPLETE_CHAIN = 10;

    // ====== config keys: 分数权重（你说的那 6 个） ======
    private static final String CFG_POINTS_BLOCK = "block-weight";
    private static final String CFG_POINTS_ENTITY = "entity-weight";
    private static final String CFG_POINTS_HAVE_ITEM = "have-item-weight";
    private static final String CFG_POINTS_DEATH_TYPE = "death-type-weight";
    private static final String CFG_POINTS_ADVANCEMENT = "complete-advancement-weight";
    private static final String CFG_POINTS_CHAT = "player-chat-weight";

    // ====== config keys: 概率权重（你之前提到的那 6 个） ======
    private static final String CFG_PROB_BLOCK = "block-probability-weight";
    private static final String CFG_PROB_ENTITY = "entity-hunt-probability-weight";
    private static final String CFG_PROB_HAVE_ITEM = "have-item-probability-weight";
    private static final String CFG_PROB_DEATH_TYPE = "death-type-probability-weight";
    private static final String CFG_PROB_ADVANCEMENT = "complete-advancement-probability-weight";
    private static final String CFG_PROB_CHAT = "type-chat-probability-weight";

    /** 队伍配置管理器。 */
    private final TeamConfigManager teamConfigManager;
    /** 随机数生成器。 */
    private final Random random = new Random();
    /** 插件主类引用。 */
    private final JavaPlugin plugin;
    /** ActionBar ticker。 */
    private ActionbarTaskTicker actionbarTicker;

    /** 存储每个队伍的任务和分数。 */
    private final Map<String, TeamTask> teamTasks = new HashMap<>();
    /** 计分板。 */
    private Scoreboard scoreboard;
    /** 计分板目标。 */
    private Objective objective;
    /** 游戏是否正在进行。 */
    private boolean gameRunning = false;
    /** 获胜队伍名称。 */
    private String winningTeam = null;

    /** 任务类型概率权重（从 config 读取）。 */
    private final Map<TaskType, Integer> taskTypeProbabilityWeights = new HashMap<>();

    private final BonusManager bonusManager;

    private int winScore = DEFAULT_WIN_SCORE;

    /**
     * 获取队伍配置管理器。
     *
     * @return 队伍配置管理器。
     */
    public TeamConfigManager getTeamConfigManager() {
        return teamConfigManager;
    }

    /**
     * 构造函数。
     *
     * @param teamConfigManager 队伍配置管理器。
     * @param plugin 插件主类。
     */
    public Game(final TeamConfigManager teamConfigManager, final JavaPlugin plugin, BonusManager bonusManager) {
        this.teamConfigManager = teamConfigManager;
        this.plugin = plugin;
        this.bonusManager = bonusManager;
    }

    public BonusManager getBonusManager() {
        return bonusManager;
    }

    /**
     * 强制结束并结算当前局。
     *
     * @param operatorName 执行操作的管理员名。
     */
    public void cancelAndSettle(final String operatorName) {
        if (!gameRunning) {
            return;
        }

        Map<String, Integer> scoreSnapshot = new HashMap<>();
        Map<String, Integer> completedSnapshot = new HashMap<>();

        for (Map.Entry<String, TeamTask> entry : teamTasks.entrySet()) {
            scoreSnapshot.put(entry.getKey(), entry.getValue().getScore());
            completedSnapshot.put(
                    entry.getKey(),
                    entry.getValue().getCompletedTasks().size()
            );
        }

        plugin.getConfig().set("game-status", false);
        plugin.saveConfig();

        if (actionbarTicker != null) {
            actionbarTicker.stop();
            actionbarTicker = null;
        }

        gameRunning = false;

        Bukkit.broadcastMessage(
                "§c§lTaskMaster 已被管理员强制结束！§7(" + operatorName + ")"
        );

        Bukkit.broadcastMessage("§6§l最终统计：");
        List<Map.Entry<String, Integer>> ranking =
                new ArrayList<>(scoreSnapshot.entrySet());

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
                    "§e"
                            + (i + 1)
                            + ". "
                            + color
                            + displayName
                            + " §7- §b"
                            + score
                            + " §7分  §8(完成: §f"
                            + done
                            + "§8)"
            );
        }

        endGame(false);
    }

    /**
     * 任务类型枚举。
     */
    private enum TaskType {
        FIND_BLOCK("找到特定方块"),
        HAVE_ITEM("背包里有特定物品"),
        KILL_MOB("杀死特定生物"),
        DEATH_TYPE("以特定方式死亡"),
        COMPLETE_ACHIEVEMENT("完成随机成就"),
        TYPE_CHAT("在聊天栏里打出特定随机字符");

        private final String description;

        TaskType(final String taskDescription) {
            this.description = taskDescription;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 从 config.yml 读取任务类型概率权重。
     * 若缺省则使用合理默认值，避免全 0 造成随机崩溃。
     */
    private void loadTaskTypeProbabilityWeightsFromConfig() {
        int block = plugin.getConfig().getInt(CFG_PROB_BLOCK, 19);
        int entity = plugin.getConfig().getInt(CFG_PROB_ENTITY, 19);
        int haveItem = plugin.getConfig().getInt(CFG_PROB_HAVE_ITEM, 19);
        int deathType = plugin.getConfig().getInt(CFG_PROB_DEATH_TYPE, 19);
        int advancement = plugin.getConfig().getInt(CFG_PROB_ADVANCEMENT, 19);
        int chat = plugin.getConfig().getInt(CFG_PROB_CHAT, 5);

        taskTypeProbabilityWeights.put(TaskType.FIND_BLOCK, Math.max(0, block));
        taskTypeProbabilityWeights.put(TaskType.KILL_MOB, Math.max(0, entity));
        taskTypeProbabilityWeights.put(TaskType.HAVE_ITEM, Math.max(0, haveItem));
        taskTypeProbabilityWeights.put(TaskType.DEATH_TYPE, Math.max(0, deathType));
        taskTypeProbabilityWeights.put(TaskType.COMPLETE_ACHIEVEMENT, Math.max(0, advancement));
        taskTypeProbabilityWeights.put(TaskType.TYPE_CHAT, Math.max(0, chat));
    }

    /**
     * 从 config.yml 读取某个任务类型完成后加多少分。
     */
    public int getPointsForTaskType(final TaskType type) {
        if (type == TaskType.FIND_BLOCK) {
            return plugin.getConfig().getInt(CFG_POINTS_BLOCK, 1);
        }
        if (type == TaskType.KILL_MOB) {
            return plugin.getConfig().getInt(CFG_POINTS_ENTITY, 1);
        }
        if (type == TaskType.HAVE_ITEM) {
            return plugin.getConfig().getInt(CFG_POINTS_HAVE_ITEM, 1);
        }
        if (type == TaskType.DEATH_TYPE) {
            return plugin.getConfig().getInt(CFG_POINTS_DEATH_TYPE, 1);
        }
        if (type == TaskType.COMPLETE_ACHIEVEMENT) {
            return plugin.getConfig().getInt(CFG_POINTS_ADVANCEMENT, 1);
        }
        if (type == TaskType.TYPE_CHAT) {
            return plugin.getConfig().getInt(CFG_POINTS_CHAT, 1);
        }
        return 1;
    }

    /**
     * 队伍任务类。
     */
    private class TeamTask {
        private final List<String> activeTasks = new ArrayList<>();
        private final List<String> completedTasks = new ArrayList<>();
        private int score = 0;
        private boolean hasWon = false;

        TeamTask() {
            for (int i = 0; i < TASKS_PER_TEAM; i++) {
                refreshOneTask();
            }
        }

        List<String> getActiveTasks() {
            return activeTasks;
        }

        List<String> getCompletedTasks() {
            return completedTasks;
        }

        int getScore() {
            return score;
        }

        void addScore(final int points) {
            score += points;
        }

        boolean hasWon() {
            return hasWon;
        }

        void setHasWon(final boolean won) {
            this.hasWon = won;
        }

        void refreshTask(final int taskIndex) {
            if (taskIndex >= 0 && taskIndex < activeTasks.size()) {
                String oldTask = activeTasks.remove(taskIndex);
                String newTask = generateNewTask();
                activeTasks.add(taskIndex, newTask);
                System.out.println("刷新任务: " + oldTask + " -> " + newTask);
            }
        }

        private void refreshOneTask() {
            activeTasks.add(generateNewTask());
        }

        boolean completeTask(final String task, final int points) {
            int taskIndex = -1;

            for (int i = 0; i < activeTasks.size(); i++) {
                if (activeTasks.get(i).equals(task)) {
                    taskIndex = i;
                    break;
                }
            }

            if (taskIndex != -1) {
                completedTasks.add(activeTasks.get(taskIndex));
                refreshTask(taskIndex);
                return true;
            }
            return false;
        }

        private String generateNewTask() {
            int totalWeight = 0;
            for (TaskType type : TaskType.values()) {
                totalWeight += taskTypeProbabilityWeights.getOrDefault(type, 0);
            }

            if (totalWeight <= 0) {
                // 避免全 0 造成 nextInt 崩溃
                return "未知任务";
            }

            int randomValue = random.nextInt(totalWeight);
            int cumulativeWeight = 0;

            TaskType selectedType = null;
            for (TaskType type : TaskType.values()) {
                cumulativeWeight += taskTypeProbabilityWeights.getOrDefault(type, 0);
                if (randomValue < cumulativeWeight) {
                    selectedType = type;
                    break;
                }
            }

            if (selectedType != null) {
                return generateTaskDetail(selectedType).getName();
            }

            return "未知任务";
        }
    }

    /**
     * 任务输出数据类。
     */
    private class TaskOutputData {
        private final String name;
        private final Object value;

        public TaskOutputData(final String taskName, final Object taskValue) {
            this.name = taskName;
            this.value = taskValue;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
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
        loadTaskTypeProbabilityWeightsFromConfig();

        bonusManager.reload();

        winScore = plugin.getConfig().getInt(CFG_GAME_TARGET_POINTS, DEFAULT_WIN_SCORE);
        if (winScore <= 0) {
            winScore = DEFAULT_WIN_SCORE;
        }

        gameRunning = true;
        winningTeam = null;

        initializeTeamTasks();
        createScoreboard();
        setupPlayers();
        setupSpectators();
        teleportTeams();
        applyPotionEffects();

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

        for (String teamName : allTeams.keySet()) {
            teamTasks.put(teamName, new TeamTask());

            // 初始化后如果刷到了“已获得的成就”，直接补分并继续刷新
            autoCompleteAdvancementTasks(teamName);

            Bukkit.broadcastMessage("队伍 " + teamName + " 的任务:");
            for (String task : teamTasks.get(teamName).getActiveTasks()) {
                Bukkit.broadcastMessage("  - " + task);
            }
        }
    }

    private TaskOutputData generateTaskDetail(final TaskType type) {
        switch (type) {
            case FIND_BLOCK:
                return findBlockTask();
            case HAVE_ITEM:
                return haveItemTask();
            case KILL_MOB:
                return killMobTask();
            case DEATH_TYPE:
                return deathTypeTask();
            case COMPLETE_ACHIEVEMENT:
                return achievementTask();
            case TYPE_CHAT:
                return typeChatTask();
            default:
                return new TaskOutputData("未知任务", null);
        }
    }

    private TaskOutputData findBlockTask() {
        List<Material> pool = bonusManager.getBlockPool();
        if (pool.isEmpty()) {
            // 兜底：避免 yml 空导致崩溃
            Material fallback = Material.DIAMOND_ORE;
            return new TaskOutputData("找到 " + formatMaterialName(fallback.name()) + " 方块", fallback);
        }

        Material block = pool.get(random.nextInt(pool.size()));
        return new TaskOutputData("找到 " + formatMaterialName(block.name()) + " 方块", block);
    }


    private TaskOutputData haveItemTask() {
        List<Material> pool = bonusManager.getItemPool();
        if (pool.isEmpty()) {
            Material fallback = Material.DIAMOND;
            return new TaskOutputData("收集 " + formatMaterialName(fallback.name()) + " 物品", fallback);
        }

        Material item = pool.get(random.nextInt(pool.size()));
        return new TaskOutputData("收集 " + formatMaterialName(item.name()) + " 物品", item);
    }

    private TaskOutputData killMobTask() {
        List<EntityType> pool = bonusManager.getMobPool();
        if (pool.isEmpty()) {
            EntityType fallback = EntityType.SKELETON;
            return new TaskOutputData("杀死一只 " + fallback, fallback);
        }

        EntityType mob = pool.get(random.nextInt(pool.size()));
        return new TaskOutputData("杀死一只 " + mob, mob);
    }

    private TaskOutputData deathTypeTask() {
        List<EntityDamageEvent.DamageCause> pool = bonusManager.getDeathPool();
        if (pool.isEmpty()) {
            EntityDamageEvent.DamageCause fallback = EntityDamageEvent.DamageCause.DROWNING;
            return new TaskOutputData("死于 " + fallback, fallback);
        }

        EntityDamageEvent.DamageCause deathType = pool.get(random.nextInt(pool.size()));
        return new TaskOutputData("死于 " + deathType, deathType);
    }

    private TaskOutputData achievementTask() {
        List<String> pool = bonusManager.getAdvancementPool();
        if (pool.isEmpty()) {
            String fallback = "story/mine_diamond";
            return new TaskOutputData("完成成就: " + fallback, fallback);
        }

        String achievement = pool.get(random.nextInt(pool.size()));
        return new TaskOutputData("完成成就: " + achievement, achievement);
    }


    private TaskOutputData typeChatTask() {
        String chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                        + "!@#$%^&*()+_-~[]{};:.,<>?/|\\";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return new TaskOutputData("在聊天框输入: " + code, code.toString());
    }

    private String formatMaterialName(final String materialName) {
        String lowerCase = materialName.toLowerCase().replace('_', ' ');
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("\\b(\\w)");
        java.util.regex.Matcher matcher = pattern.matcher(lowerCase);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private void createScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }

        scoreboard = manager.getNewScoreboard();

        objective = scoreboard.registerNewObjective(
                "tasks",
                "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "任务大师"
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String teamName : teamTasks.keySet()) {
            TeamTask task = teamTasks.get(teamName);
            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);

            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            String scoreLine =
                    color + displayName + ": " + ChatColor.GREEN + task.getScore();
            Score score = objective.getScore(scoreLine);
            score.setScore(0);

            Team team = scoreboard.registerNewTeam(
                    teamName.substring(0, Math.min(teamName.length(), 16))
            );
            team.setColor(getChatColorFromColorCode(color));
        }

        updateScoreboardForAllPlayers();
    }

    private void updateScoreboardForAllPlayers() {
        List<Player> allPlayers = teamConfigManager.getAllOnlineTeamPlayers();
        for (Player player : allPlayers) {
            player.setScoreboard(scoreboard);
        }
    }

    private ChatColor getChatColorFromColorCode(final String colorCode) {
        if (colorCode.contains("§a")) {
            return ChatColor.GREEN;
        }
        if (colorCode.contains("§e")) {
            return ChatColor.YELLOW;
        }
        if (colorCode.contains("§c")) {
            return ChatColor.RED;
        }
        if (colorCode.contains("§9")) {
            return ChatColor.BLUE;
        }
        if (colorCode.contains("§5")) {
            return ChatColor.DARK_PURPLE;
        }
        if (colorCode.contains("§b")) {
            return ChatColor.AQUA;
        }
        if (colorCode.contains("§d")) {
            return ChatColor.LIGHT_PURPLE;
        }
        if (colorCode.contains("§f")) {
            return ChatColor.WHITE;
        }
        return ChatColor.WHITE;
    }

    public void updateTeamScore(final String teamName) {
        if (objective == null) {
            return;
        }

        TeamTask task = teamTasks.get(teamName);
        if (task == null) {
            return;
        }

        Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
        String color = (String) teamInfo.get("color");
        String displayName = (String) teamInfo.get("display-name");

        for (String entry : scoreboard.getEntries()) {
            if (entry.startsWith(color + displayName + ":")) {
                scoreboard.resetScores(entry);
            }
        }

        String newScoreLine =
                color + displayName + ": " + ChatColor.GREEN + task.getScore();
        Score score = objective.getScore(newScoreLine);
        score.setScore(0);
    }

    private ItemStack createSpectatorCompass() {
        return TaskCompass.create(plugin);
    }

    private void setupSpectators() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String team = teamConfigManager.getPlayerTeam(player.getUniqueId());
            if (team != null) {
                continue;
            }

            player.setGameMode(GameMode.SPECTATOR);

            ItemStack infoCompass = createSpectatorCompass();
            player.getInventory().clear();
            player.getInventory().addItem(infoCompass.clone());

            if (scoreboard != null) {
                player.setScoreboard(scoreboard);
            }

            player.sendMessage("§b你尚未选择队伍，已进入旁观模式。");
            player.sendMessage("§7使用指南针可以查看任务/队伍信息。");
        }
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

    private void setupPlayers() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(TIME_MORNING);
            world.setStorm(false);
            world.setThundering(false);
        }

        Map<String, List<Player>> teamPlayers =
                teamConfigManager.getTeamOnlinePlayersMap();

        for (String teamName : teamPlayers.keySet()) {
            List<Player> players = teamPlayers.get(teamName);

            for (Player player : players) {
                player.setGameMode(GameMode.SURVIVAL);

                AttributeInstance maxHealthAttr =
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(BASE_MAX_HEALTH);
                }

                player.setHealth(BASE_HEALTH);
                player.setFoodLevel(BASE_FOOD);
                player.setSaturation(BASE_SATURATION);

                PlayerInventory inventory = player.getInventory();
                inventory.clear();

                inventory.addItem(new ItemStack(Material.STONE_SWORD));
                inventory.addItem(new ItemStack(Material.STONE_PICKAXE));
                inventory.addItem(new ItemStack(Material.BREAD, START_BREAD_AMOUNT));

                player.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);

                player.sendTitle(
                        ChatColor.GOLD + "任务大师游戏开始！",
                        ChatColor.YELLOW + "你有3个任务需要完成",
                        START_TITLE_FADE_IN,
                        START_TITLE_STAY,
                        START_TITLE_FADE_OUT
                );

                player.sendMessage(ChatColor.GOLD + "========== 你的任务 ==========");
                TeamTask tasks = teamTasks.get(teamName);
                if (tasks != null) {
                    int taskNum = 1;
                    for (String task : tasks.getActiveTasks()) {
                        player.sendMessage(
                                ChatColor.YELLOW
                                        + Integer.toString(taskNum)
                                        + ". "
                                        + ChatColor.WHITE
                                        + task
                        );
                        taskNum++;
                    }
                }
                player.sendMessage(ChatColor.GOLD + "============================");
            }
        }
    }

    private void teleportTeams() {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }

        Map<String, List<Player>> teamPlayers =
                teamConfigManager.getTeamOnlinePlayersMap();

        for (String teamName : teamPlayers.keySet()) {
            List<Player> players = teamPlayers.get(teamName);

            Location teamLocation = generateRandomLocation(world);

            for (Player player : players) {
                Location playerLocation = teamLocation.clone();
                playerLocation.add(
                        ThreadLocalRandom.current().nextDouble(
                                TELEPORT_OFFSET_MIN,
                                TELEPORT_OFFSET_MAX
                        ),
                        0,
                        ThreadLocalRandom.current().nextDouble(
                                TELEPORT_OFFSET_MIN,
                                TELEPORT_OFFSET_MAX
                        )
                );

                player.teleport(playerLocation);
                player.setBedSpawnLocation(teamLocation, true);

                player.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.SLOW_FALLING,
                                SLOW_FALL_TICKS,
                                0,
                                false,
                                false
                        )
                );

                player.sendMessage(ChatColor.GREEN + "你已被传送到任务区域！");
            }
        }
    }

    private Location generateRandomLocation(final World world) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < RANDOM_LOC_TRIES; i++) {
            int x = rand.nextInt(RAND_XZ_MIN, RAND_XZ_MAX);
            int z = rand.nextInt(RAND_XZ_MIN, RAND_XZ_MAX);

            int y = world.getHighestBlockYAt(x, z);

            Block ground = world.getBlockAt(x, y - 1, z);
            Material type = ground.getType();

            if (type == Material.WATER) {
                continue;
            }

            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }

        return world.getSpawnLocation();
    }

    private void applyPotionEffects() {
        List<Player> allPlayers = teamConfigManager.getAllOnlineTeamPlayers();

        for (Player player : allPlayers) {
            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.SPEED,
                            SPEED_DURATION_TICKS,
                            0,
                            false,
                            false
                    )
            );

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.JUMP,
                            JUMP_DURATION_TICKS,
                            1,
                            false,
                            false
                    )
            );

            ItemStack compass = TaskCompass.create(plugin);
            if (!player.getInventory().contains(Material.COMPASS)) {
                player.getInventory().addItem(compass.clone());
            }

            player.sendMessage(
                    ChatColor.AQUA + "你获得了速度和跳跃提升效果，持续60秒！"
            );
        }
    }

    public List<String> getTeamActiveTasks(final String teamName) {
        TeamTask task = teamTasks.get(teamName);
        if (task != null) {
            return task.getActiveTasks();
        }
        return new ArrayList<>();
    }

    public List<String> getTeamCompletedTasks(final String teamName) {
        TeamTask task = teamTasks.get(teamName);
        if (task != null) {
            return task.getCompletedTasks();
        }
        return new ArrayList<>();
    }

    public void addScoreToTeam(final String teamName, final int points) {
        if (!gameRunning) {
            return;
        }

        TeamTask task = teamTasks.get(teamName);
        if (task == null || task.hasWon()) {
            return;
        }

        task.addScore(points);

        updateTeamScore(teamName);

        if (task.getScore() >= winScore) {
            declareWinner(teamName);
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
        TeamTask task = teamTasks.get(teamName);
        if (task != null) {
            return task.getScore();
        }
        return 0;
    }

    private void declareWinner(final String teamName) {
        if (winningTeam != null) {
            return;
        }

        winningTeam = teamName;
        gameRunning = false;

        TeamTask task = teamTasks.get(teamName);
        if (task != null) {
            task.setHasWon(true);
        }

        Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
        String color = (String) teamInfo.get("color");
        String displayName = (String) teamInfo.get("display-name");

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════");
        Bukkit.broadcastMessage(ChatColor.GOLD + "      🎉 游戏结束！ 🎉");
        Bukkit.broadcastMessage(
                color + displayName + ChatColor.GOLD + " 队伍获得胜利！"
        );
        Bukkit.broadcastMessage(
                ChatColor.YELLOW
                        + "最终分数: "
                        + ChatColor.GREEN
                        + task.getScore()
                        + "分"
        );
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════");
        Bukkit.broadcastMessage("");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(
                    player.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    1.0f,
                    1.0f
            );
            player.sendTitle(
                    color + displayName + " 胜利！",
                    ChatColor.YELLOW + "分数: " + task.getScore(),
                    START_TITLE_FADE_IN,
                    START_TITLE_STAY,
                    START_TITLE_FADE_OUT
            );
        }

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> endGame(true),
                END_DELAY_TICKS
        );
    }

    public boolean completeTeamTask(
            final String teamName,
            final String task,
            final int points
    ) {
        if (!gameRunning) {
            return false;
        }

        TeamTask teamTask = teamTasks.get(teamName);
        if (teamTask == null) {
            return false;
        }

        boolean success = teamTask.completeTask(task, points);

        if (success) {
            addScoreToTeam(teamName, points);

            // 刷新任务后，若刷出“已完成成就任务”，直接补分并再刷新
            autoCompleteAdvancementTasks(teamName);

            notifyNewTask(teamName, teamTask.getActiveTasks());
            Bukkit.broadcastMessage(
                    teamName
                            + " 完成了一项任务,获得"
                            + Integer.toString(points)
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

        for (Map.Entry<String, TeamTask> entry : teamTasks.entrySet()) {
            String teamName = entry.getKey();
            TeamTask task = entry.getValue();

            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);
            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            Bukkit.broadcastMessage(color + "§l" + displayName + " 队伍:");

            List<String> tasks = task.getActiveTasks();
            for (int i = 0; i < tasks.size(); i++) {
                Bukkit.broadcastMessage(
                        "  §f" + (i + 1) + ". §e" + tasks.get(i)
                );
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
                                + activeTasks.get(i)
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

    public void endGame() {
        endGame(true);
    }

    public void endGame(final boolean showStats) {
        plugin.getConfig().set("game-status", false);
        plugin.saveConfig();

        if (actionbarTicker != null) {
            actionbarTicker.stop();
            actionbarTicker = null;
        }

        if (showStats) {
            showFinalStatistics();
        }

        cleanupScoreboard();

        gameRunning = false;
        winningTeam = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            teamConfigManager.removePlayerFromAllTeams(player.getUniqueId());
        }

        teamTasks.clear();

        Bukkit.broadcastMessage(ChatColor.GRAY + "游戏已结束，感谢参与！");
    }

    private void showFinalStatistics() {
        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════ 最终统计 ════════════");

        for (String teamName : teamTasks.keySet()) {
            TeamTask task = teamTasks.get(teamName);
            Map<String, Object> teamInfo = teamConfigManager.getTeamInfo(teamName);

            String color = (String) teamInfo.get("color");
            String displayName = (String) teamInfo.get("display-name");

            Bukkit.broadcastMessage(
                    color
                            + displayName
                            + ChatColor.WHITE
                            + " - 分数: "
                            + ChatColor.GREEN
                            + task.getScore()
                            + ChatColor.WHITE
                            + " - 完成任务: "
                            + ChatColor.YELLOW
                            + task.getCompletedTasks().size()
            );
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "════════════════════════════════════");
    }

    private void cleanupScoreboard() {
        if (scoreboard == null) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard emptyScoreboard =
                    Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(emptyScoreboard);
        }

        for (Objective obj : scoreboard.getObjectives()) {
            obj.unregister();
        }

        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }

    public String buildTeamTaskActionbar(final String teamName) {
        List<String> tasks = getTeamActiveTasks(teamName);
        if (tasks == null || tasks.isEmpty()) {
            return "§7暂无任务";
        }

        int max = Math.min(ACTIONBAR_MAX_TASKS, tasks.size());
        List<String> shortTasks = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            String t = tasks.get(i);

            t = t.replace("找到 ", "")
                    .replace("收集 ", "")
                    .replace("完成成就: ", "成就 ")
                    .replace("杀死一只 ", "击杀 ")
                    .replace("死于 ", "死于 ");

            shortTasks.add(t);
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
                if (!task.startsWith(ACHIEVEMENT_TASK_PREFIX)) {
                    continue;
                }

                String key = task.substring(ACHIEVEMENT_TASK_PREFIX.length()).trim();
                if (!teamHasAdvancement(teamName, key)) {
                    continue;
                }

                TeamTask teamTask = teamTasks.get(teamName);
                if (teamTask == null) {
                    return;
                }

                int base = plugin.getConfig().getInt(CFG_POINTS_ADVANCEMENT, 1);
                int bonus = bonusManager.getAdvancementBonus(key);
                int points = base + bonus;

                boolean success = teamTask.completeTask(task, points);
                if (success) {
                    addScoreToTeam(teamName, points);

                    Bukkit.broadcastMessage(
                            teamName
                                    + " 自动完成成就任务: "
                                    + key
                                    + " (已获得), +"
                                    + Integer.toString(points)
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
}
