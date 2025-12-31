package me.jacky.taskMaster;

import me.jacky.taskMaster.commands.CancelTaskMasterCommand;
import me.jacky.taskMaster.commands.JoinTeamGUICommand;
import me.jacky.taskMaster.commands.PingCommand;
import me.jacky.taskMaster.commands.StartTaskRushCommand;
import me.jacky.taskMaster.config.BonusManager;
import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.game.Game;
import me.jacky.taskMaster.listeners.MenuListener;
import me.jacky.taskMaster.listeners.PlayerJoinListener;
import me.jacky.taskMaster.listeners.RespawnListener;
import me.jacky.taskMaster.listeners.TaskCompassListener;
import me.jacky.taskMaster.listeners.TaskGuiListener;
import me.jacky.taskMaster.resolver.EntityTypeResolver;
import me.jacky.taskMaster.resolver.MaterialResolver;
import me.jacky.taskMaster.task.TaskChecker;
import me.jacky.taskMaster.task.TaskParser;
import me.jacky.taskMaster.text.LangManager;
import me.jacky.taskMaster.text.TaskTextFormatter;
import me.jacky.taskMaster.uscase.JoinTeamUseCase;
import me.jacky.taskMaster.view.JoinTeamView;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * 任务大师插件主类。
 * 负责初始化所有组件、注册事件监听器和命令处理器。
 */
public final class TaskMaster extends JavaPlugin {

    private final JoinTeamView joinTeamView = new JoinTeamView();

    private TeamConfigManager teamConfigManager;
    private BonusManager bonusManager;

    private Game game;
    private TaskChecker taskChecker;
    private StartTaskRushCommand taskMasterCommand;

    private LangManager langManager;
    private TaskTextFormatter taskTextFormatter;
    private TaskParser taskParser;

    @Override
    public void onEnable() {
        getLogger().info("=== TaskMaster enabling ===");

        try {
            initConfigFiles();
            initCoreServices();
            registerListeners();
            registerCommands();
            printStartupInfo();
        } catch (Exception e) {
            getLogger().severe("插件启动失败: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void initConfigFiles() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        ensureBonusFiles();
    }

    private void initCoreServices() {
        teamConfigManager = new TeamConfigManager(this);
        getLogger().info("✓ TeamConfigManager loaded");

        bonusManager = new BonusManager(this);
        getLogger().info("✓ BonusManager loaded");

        // ===== new: lang + formatter =====
        langManager = new LangManager(this);
        langManager.load();

        MaterialResolver materialResolver = new MaterialResolver(this);
        EntityTypeResolver entityTypeResolver = new EntityTypeResolver();
        taskParser = new TaskParser(materialResolver, entityTypeResolver);
        taskTextFormatter = new TaskTextFormatter(taskParser, langManager);

        // ===== inject formatter into Game =====
        game = new Game(teamConfigManager, this, bonusManager, taskTextFormatter);
        getLogger().info("✓ Game loaded");

        // ===== inject parser into TaskChecker =====
        taskChecker = new TaskChecker(game, teamConfigManager, this, taskParser, taskTextFormatter);

        taskMasterCommand = new StartTaskRushCommand(this, game);

        if (getCommand("canceltaskmaster") != null) {
            getCommand("canceltaskmaster").setExecutor(new CancelTaskMasterCommand(this, game));
        }
    }

    private void registerListeners() {
        JoinTeamUseCase joinTeamUseCase = new JoinTeamUseCase(teamConfigManager);

        getServer().getPluginManager().registerEvents(taskChecker, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(joinTeamUseCase), this);

        getServer().getPluginManager().registerEvents(new TaskCompassListener(this, game), this);
        getServer().getPluginManager().registerEvents(new TaskGuiListener(), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);

        getLogger().info("✓ Listeners registered");
    }

    private void registerCommands() {
        if (getCommand("ping") != null) {
            getCommand("ping").setExecutor(new PingCommand());
        }
        if (getCommand("taskmaster") != null) {
            getCommand("taskmaster").setExecutor(taskMasterCommand);
        }
        if (getCommand("jointeam") != null) {
            getCommand("jointeam").setExecutor(new JoinTeamGUICommand(this));
        }
        getLogger().info("✓ Commands registered");
    }

    private void printStartupInfo() {
        getLogger().info("=== TaskMaster enabled ===");
    }

    public void openMainMenu(final Player player) {
        joinTeamView.joinTeamMenu(player);
    }

    @Override
    public void onDisable() {
        if (game != null && game.isGameRunning()) {
            game.endGame(false); // 不显示统计，安静退出
        }
        getLogger().info("=== TaskMaster disabled ===");
    }

    public Game getGame() {
        return game;
    }

    public TeamConfigManager getTeamConfigManager() {
        return teamConfigManager;
    }

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
            saveResource(path, false);
        }
    }
}
