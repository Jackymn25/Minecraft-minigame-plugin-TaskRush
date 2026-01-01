package me.jacky.taskMaster.commands;

import me.jacky.taskMaster.TaskMaster;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadPluginCommand implements CommandExecutor {

    private final TaskMaster plugin;

    public ReloadPluginCommand(TaskMaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // 允许：/reload_tm 或 /reload_tm en_us
        if (args.length >= 1) {
            String lang = args[0].trim().toLowerCase();
            plugin.getConfig().set("language", lang);
            plugin.saveConfig();
        }

        plugin.reloadAll();

        String nowLang = plugin.getConfig().getString("language", "zh_cn");
        sender.sendMessage("§a[TaskMaster] Reloaded. language=" + nowLang);
        return true;
    }
}
