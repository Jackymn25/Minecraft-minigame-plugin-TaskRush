package me.jacky.taskMaster;

import me.jacky.taskMaster.commands.JoinTeamGUICommand;
import me.jacky.taskMaster.commands.PingCommand;
import me.jacky.taskMaster.commands.StartTaskRushCommand;
import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.listeners.MenuListener;
import me.jacky.taskMaster.listeners.PlayerJoinListener;
import me.jacky.taskMaster.uscase.JoinTeamUseCase;
import me.jacky.taskMaster.view.JoinTeamView;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public final class TaskMaster extends JavaPlugin {

    private final JoinTeamView joinTeamView = new JoinTeamView();
    private final JoinTeamUseCase joinTeamUseCase = new JoinTeamUseCase(new TeamConfigManager(this));

    //    private static TaskRush plugin;
//
//    public static TaskRush getPlugin() {
//        return plugin;
//    }

    @Override
    public void onEnable() {
//        plugin = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("taskmaster").setExecutor(new StartTaskRushCommand(this, new Game(new TeamConfigManager(this))));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(joinTeamUseCase), this);
        //getCommand("armorstand").setExecutor(new JoinTeamGUICommand(this)); //Opens main menu
        getCommand("jointeam").setExecutor(new JoinTeamGUICommand(this));
        System.out.println("Plugin has been enabled!");
    }

    //Create and open the main Armor Stand menu
    public void openMainMenu(Player player){
        joinTeamView.joinTeamMenu(player);
    }

    @Override
    public void onDisable() {
        System.out.println("Plugin has been disabled!");
    }
}
