package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class TeleportService {

    private static final double TELEPORT_OFFSET_MIN = -3.0;
    private static final double TELEPORT_OFFSET_MAX = 3.0;

    private static final int RANDOM_LOC_TRIES = 50;
    private static final int RAND_XZ_MIN = -50000;
    private static final int RAND_XZ_MAX = 50000;

    private final JavaPlugin plugin;
    private final TeamConfigManager teamConfigManager;

    public TeleportService(JavaPlugin plugin, TeamConfigManager teamConfigManager) {
        this.plugin = plugin;
        this.teamConfigManager = teamConfigManager;
    }

    public void teleportTeams(final World world) {
        World w = world;
        if (w == null) {
            w = plugin.getServer().getWorld("world");
        }
        if (w == null) {
            w = plugin.getServer().getWorlds().get(0);
        }

        Map<String, List<Player>> teamPlayers = teamConfigManager.getTeamOnlinePlayersMap();

        for (String teamName : teamPlayers.keySet()) {
            List<Player> players = teamPlayers.get(teamName);
            if (players == null || players.isEmpty()) continue;

            Location teamLocation = generateRandomLocation(w);

            for (Player player : players) {
                Location playerLocation = teamLocation.clone();
                playerLocation.add(
                        ThreadLocalRandom.current().nextDouble(TELEPORT_OFFSET_MIN, TELEPORT_OFFSET_MAX),
                        0,
                        ThreadLocalRandom.current().nextDouble(TELEPORT_OFFSET_MIN, TELEPORT_OFFSET_MAX)
                );

                player.teleport(playerLocation);
                player.setBedSpawnLocation(teamLocation, true);

                // slow falling for safety
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        getSlowFallTicks(),
                        0,
                        false,
                        false
                ));

                player.sendMessage(ChatColor.GREEN + "你已被传送到任务区域！");
            }
        }
    }

    private int getSlowFallTicks() {
        return 540; // 不要在意魔法数字
    }

    private Location generateRandomLocation(final World world) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < RANDOM_LOC_TRIES; i++) {
            int x = rand.nextInt(RAND_XZ_MIN, RAND_XZ_MAX);
            int z = rand.nextInt(RAND_XZ_MIN, RAND_XZ_MAX);

            int y = world.getHighestBlockYAt(x, z);

            Block ground = world.getBlockAt(x, y - 1, z);
            Material type = ground.getType();

            if (type == Material.WATER) continue;

            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }

        return world.getSpawnLocation();
    }
}
