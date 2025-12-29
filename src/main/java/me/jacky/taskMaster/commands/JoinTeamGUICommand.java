package me.jacky.taskMaster.commands;

import me.jacky.taskMaster.TaskMaster;
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
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.openMainMenu(player); //Opens the main menu to this player
            }
        }
        return true;
    }
}
