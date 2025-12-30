package me.jacky.taskMaster.listeners;

import me.jacky.taskMaster.TaskMaster;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RespawnListener implements Listener {

    private final TaskMaster plugin;

    public RespawnListener(TaskMaster plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event)
    {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL)
        {
            if (plugin.getConfig().getBoolean("game-status")) {

                Player player = event.getPlayer();
                // 给予缓降效果，避免摔伤
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        500, // 25秒（500 ticks）
                        0,
                        false,
                        false
                ));
            }
        }
    }
}
