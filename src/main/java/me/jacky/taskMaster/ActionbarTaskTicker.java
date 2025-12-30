package me.jacky.taskMaster;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically sends ActionBar messages to players,
 * showing their team's current tasks.
 */
public class ActionbarTaskTicker {

    /** Delay before the first tick (in ticks). */
    private static final long INITIAL_DELAY_TICKS = 20L;

    /** Interval between updates (in ticks). */
    private static final long PERIOD_TICKS = 20L;

    /** Plugin instance. */
    private final JavaPlugin plugin;

    /** Game instance. */
    private final Game game;

    /** Bukkit scheduled task. */
    private BukkitTask task;

    /**
     * Creates a new ActionBar task ticker.
     *
     * @param plugin the plugin instance
     * @param game the game instance
     */
    public ActionbarTaskTicker(final JavaPlugin plugin, final Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    /**
     * Starts the ActionBar update task.
     * If a previous task is running, it will be cancelled first.
     */
    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::sendActionBars,
                INITIAL_DELAY_TICKS,
                PERIOD_TICKS
        );
    }

    /**
     * Stops the ActionBar update task if it is running.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Sends ActionBar messages to all online players
     * who belong to a team.
     */
    private void sendActionBars() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String team =
                    game.getTeamConfigManager()
                            .getPlayerTeam(player.getUniqueId());

            if (team == null) {
                continue;
            }

            String text = game.buildTeamTaskActionbar(team);

            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    new TextComponent(text)
            );
        }
    }
}
