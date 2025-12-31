package me.jacky.taskMaster.commands;
import me.jacky.taskMaster.game.Game;
import me.jacky.taskMaster.TaskMaster;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartTaskRushCommand implements CommandExecutor {

    private final TaskMaster plugin;
    private final Game game;

    public StartTaskRushCommand(TaskMaster plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (command.getName().equalsIgnoreCase("TaskMaster")) {
            if (sender instanceof Player p) {
                boolean game_status = plugin.getConfig().getBoolean("game-status");
                if (game_status) {
                    p.sendMessage(ChatColor.RED + "TaskRush has been started.");
                    return true;
                } else {
                    plugin.getConfig().set("game-status", true);
                    plugin.saveConfig();
                    game.startGame();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "TaskRush has been started.");
                }
            }
        }
        return true;
    }
}
