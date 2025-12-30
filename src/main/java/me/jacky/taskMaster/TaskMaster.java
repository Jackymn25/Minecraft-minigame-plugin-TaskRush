package me.jacky.taskMaster;

import me.jacky.taskMaster.commands.CancelTaskMasterCommand;
import me.jacky.taskMaster.commands.JoinTeamGUICommand;
import me.jacky.taskMaster.commands.PingCommand;
import me.jacky.taskMaster.commands.StartTaskRushCommand;
import me.jacky.taskMaster.config.BonusManager;
import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.listeners.MenuListener;
import me.jacky.taskMaster.listeners.PlayerJoinListener;
import me.jacky.taskMaster.listeners.RespawnListener;
import me.jacky.taskMaster.listeners.TaskCompassListener;
import me.jacky.taskMaster.listeners.TaskGuiListener;
import me.jacky.taskMaster.uscase.JoinTeamUseCase;
import me.jacky.taskMaster.view.JoinTeamView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * 任务大师插件主类。
 * 负责初始化所有组件、注册事件监听器和命令处理器。
 */
public final class TaskMaster extends JavaPlugin {

    /** 队伍选择视图。 */
    private final JoinTeamView joinTeamView = new JoinTeamView();
    /** 队伍配置管理器。 */
    private TeamConfigManager teamConfigManager;
    /** 游戏核心。 */
    private Game game;
    /** 任务判定器。 */
    private TaskChecker taskChecker;
    /** /taskmaster 命令处理器。 */
    private StartTaskRushCommand taskMasterCommand;

    /**
     * 插件启用时调用。
     */
    @Override
    public void onEnable() {
        getLogger().info("=== 任务大师插件启动中 ===");

        try {
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
            ensureBonusFiles();

            teamConfigManager = new TeamConfigManager(this);
            getLogger().info("✓ 队伍配置管理器已初始化");

            game = new Game(teamConfigManager, this, new BonusManager(this));
            getLogger().info("✓ 游戏核心已初始化");

            taskChecker = new TaskChecker(game, teamConfigManager, this);

            taskMasterCommand = new StartTaskRushCommand(this, game);
            getCommand("canceltaskmaster")
                    .setExecutor(new CancelTaskMasterCommand(this, game));

            JoinTeamUseCase joinTeamUseCase = new JoinTeamUseCase(teamConfigManager);

            registerListeners(joinTeamUseCase);

            ItemStack compass = TaskCompass.create(this);

            getServer().getPluginManager()
                    .registerEvents(new TaskCompassListener(this, game), this);
            getServer().getPluginManager()
                    .registerEvents(new TaskGuiListener(), this);
            getServer().getPluginManager()
                    .registerEvents(new RespawnListener(this), this);

            registerCommands();

            getLogger().info("=== 任务大师插件启动完成 ===");
            getLogger().info("可用命令：");
            getLogger().info("  /jointeam - 打开队伍选择界面");
            getLogger().info("  /taskmaster - 任务大师主命令");
            getLogger().info("  /ping - 测试连接");

        } catch (Exception e) {
            getLogger().severe("插件启动失败: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * 注册所有事件监听器。
     *
     * @param joinTeamUseCase 加入队伍用例。
     */
    private void registerListeners(final JoinTeamUseCase joinTeamUseCase) {
        getServer().getPluginManager().registerEvents(taskChecker, this);
        getLogger().info("✓ 任务判定器已注册");

        getServer().getPluginManager()
                .registerEvents(new PlayerJoinListener(this), this);
        getLogger().info("✓ 玩家加入监听器已注册");

        getServer().getPluginManager()
                .registerEvents(new MenuListener(joinTeamUseCase), this);
        getLogger().info("✓ 菜单监听器已注册");
    }

    /**
     * 注册所有命令。
     */
    private void registerCommands() {
        getCommand("ping").setExecutor(new PingCommand());

        getCommand("taskmaster").setExecutor(taskMasterCommand);

        getCommand("jointeam").setExecutor(new JoinTeamGUICommand(this));

        getLogger().info("✓ 所有命令已注册");
    }

    /**
     * 打开主菜单（供其他类调用）。
     *
     * @param player 玩家。
     */
    public void openMainMenu(final Player player) {
        joinTeamView.joinTeamMenu(player);
    }

    /**
     * 插件禁用时调用。
     */
    @Override
    public void onDisable() {
        if (game != null && game.isGameRunning()) {
            game.endGame();
            getLogger().info("游戏已强制结束");
        }

        getLogger().info("=== 任务大师插件已禁用 ===");
    }

    /**
     * 获取游戏核心。
     *
     * @return 游戏核心。
     */
    public Game getGame() {
        return game;
    }

    /**
     * 获取队伍配置管理器。
     *
     * @return 队伍配置管理器。
     */
    public TeamConfigManager getTeamConfigManager() {
        return teamConfigManager;
    }

    /**
     * 获取任务判定器。
     *
     * @return 任务判定器。
     */
    public TaskChecker getTaskChecker() {
        return taskChecker;
    }

    private void ensureBonusFiles() {
        File dir = new File(getDataFolder(), "bonuses");
        if (!dir.exists()) dir.mkdirs();

        saveBonus("bonuses/blocks.yml");
        saveBonus("bonuses/items.yml");
        saveBonus("bonuses/mobs.yml");
        saveBonus("bonuses/deaths.yml");
        saveBonus("bonuses/advancements.yml");
    }

    private void saveBonus(String path) {
        File out = new File(getDataFolder(), path);
        if (!out.exists()) {
            saveResource(path, false); // 从 jar 的 resources 复制出来
        }
    }
}
