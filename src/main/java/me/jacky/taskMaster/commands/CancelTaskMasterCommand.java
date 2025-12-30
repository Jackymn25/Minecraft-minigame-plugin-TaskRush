package me.jacky.taskMaster.commands;

import me.jacky.taskMaster.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CancelTaskMasterCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Game game;

    public CancelTaskMasterCommand(JavaPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("taskmaster.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用该命令。");
            return true;
        }

        // 1) 更新 config 中的游戏状态
        plugin.getConfig().set("game.running", false);
        plugin.saveConfig();

        // 2) 直接结算并结束
        game.cancelAndSettle(sender.getName());
        sender.sendMessage(ChatColor.YELLOW + "已强制结算并结束 TaskMaster 游戏。");
        return true;
    }
}
