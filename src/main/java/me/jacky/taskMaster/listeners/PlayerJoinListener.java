package me.jacky.taskMaster.listeners;
import me.jacky.taskMaster.TaskMaster;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final TaskMaster plugin;

    public PlayerJoinListener(TaskMaster plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        String joinMessage = plugin.getConfig().getString("join-message");
        if (joinMessage != null) {
            joinMessage = joinMessage.replace("%name%", event.getPlayer().getName());
            event.getPlayer().sendMessage(
                    ChatColor.translateAlternateColorCodes('&', joinMessage));
            event.getPlayer().sendMessage("use /taskMaster to join the game. Use /force_cancel to cancel the game.");
        }
        boolean refresh = plugin.getConfig().getBoolean("refresh-while-join");
        if (refresh) {
            event.getPlayer().setHealth(20);
            event.getPlayer().setFoodLevel(20);
        }
        // plugin.getConfig().getStringList("xxx")
        // setter
        // plugin.getConfig().set("xxx", xxx);
        // plugin.saveConfig();
    }
}
