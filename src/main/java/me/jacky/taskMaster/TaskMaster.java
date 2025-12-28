package me.jacky.taskMaster;

import me.jacky.taskMaster.commands.PingCommand;
import me.jacky.taskMaster.commands.StartTaskRushCommand;
import me.jacky.taskMaster.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TaskMaster extends JavaPlugin {

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
        getCommand("starttaskrush").setExecutor(new StartTaskRushCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        System.out.println("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("Plugin has been disabled!");
    }
}
