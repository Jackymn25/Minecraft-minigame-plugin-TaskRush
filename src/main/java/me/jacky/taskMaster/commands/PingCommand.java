package me.jacky.taskMaster.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ping")) {
            if (args.length == 0) {
                if (sender instanceof Player p) {
                    int ping = p.getPing();
                    p.sendMessage(ChatColor.GREEN + "Ping: " + ping);
                    return true;
                }
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found");
                    return true;
                }

                int ping = target.getPing();
                sender.sendMessage(ChatColor.GREEN + target.getName() + " Ping: " + ping);

                return true;
            }
        }
        return true;
    }
}
