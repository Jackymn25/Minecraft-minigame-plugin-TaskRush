package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.view.TaskCompass;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InGameRejoinService {

    public void rejoin(Player player, TeamConfigManager teamConfigManager, JavaPlugin plugin) {
        // restore gamemode based on team assignment
        String team = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (team == null) {
            player.setGameMode(GameMode.SPECTATOR);

            // give info compass (spectator guide)
            ItemStack infoCompass = TaskCompass.create(plugin);
            player.getInventory().clear();
            player.getInventory().addItem(infoCompass.clone());

            player.sendMessage("§bYou haven't joined a team, you are assigned to spectators!");
            player.sendMessage("§7Right click compass to find out more info");
            return;
        }

        // team player
        player.setGameMode(GameMode.SURVIVAL);

        // ensure task compass exists
        if (!player.getInventory().contains(Material.COMPASS)) {
            ItemStack compass = TaskCompass.create(plugin);
            player.getInventory().addItem(compass.clone());
        }
    }
}
