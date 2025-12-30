package me.jacky.taskMaster.commands;

import me.jacky.taskMaster.TaskMaster;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinTeamGUICommand implements CommandExecutor {

    TaskMaster plugin;

    public JoinTeamGUICommand(TaskMaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("jointeam")) {
            if (sender instanceof Player player) {
                if (plugin.getConfig().getBoolean("game-status")) {
                    player.sendMessage(ChatColor.RED + "游戏进行中，无法加入/更换队伍！");
                    return true;
                }
                plugin.openMainMenu(player); //Opens the main menu to this player
            }
        }
        return true;
    }
}
