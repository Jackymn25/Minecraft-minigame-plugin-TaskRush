package me.jacky.taskMaster.listeners;

import me.jacky.taskMaster.Game;
import me.jacky.taskMaster.TaskCompass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Listener for the task compass.
 * Right-clicking the compass opens a GUI showing all teams' tasks.
 */
public class TaskCompassListener implements Listener {

    /** GUI size (6 rows). */
    private static final int INVENTORY_SIZE = 54;

    /** Close button slot (last slot). */
    private static final int CLOSE_SLOT = 53;

    private final JavaPlugin plugin;
    private final Game game;

    /**
     * Creates a listener bound to the plugin and game instance.
     *
     * @param plugin the plugin instance
     * @param game the game instance
     */
    public TaskCompassListener(final JavaPlugin plugin, final Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    /**
     * Opens task GUI when player right-clicks with a task compass.
     *
     * @param event the interact event
     */
    @EventHandler
    public void onUse(final PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack inHand = event.getItem();
        if (!TaskCompass.isTaskCompass(plugin, inHand)) {
            return;
        }

        event.setCancelled(true);
        openTasksGui(event.getPlayer());
    }

    /**
     * Opens the "all teams tasks" GUI for a viewer.
     *
     * @param viewer the player viewing the GUI
     */
    private void openTasksGui(final Player viewer) {
        Inventory inv = Bukkit.createInventory(
                null,
                INVENTORY_SIZE,
                ChatColor.DARK_AQUA + "所有队伍任务"
        );

        Map<String, Map<String, Object>> allTeams = game.getTeamConfigManager().getAllTeamsInfo();
        List<String> teamNames = new ArrayList<>(allTeams.keySet());

        String viewerTeam = game.getTeamConfigManager().getPlayerTeam(viewer.getUniqueId());

        int slot = 0;
        for (String team : teamNames) {
            if (slot >= inv.getSize()) {
                break;
            }

            ItemStack icon = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) {
                inv.setItem(slot, icon);
                slot++;
                continue;
            }

            boolean isSelf = viewerTeam != null && team.equalsIgnoreCase(viewerTeam);
            meta.setDisplayName((isSelf ? ChatColor.GREEN : ChatColor.RED) + "队伍: " + team);

            List<String> tasks = game.getTeamActiveTasks(team);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "分数: " + ChatColor.WHITE + game.getTeamScore(team));
            lore.add(ChatColor.DARK_GRAY + "----------------");
            for (String line : tasks) {
                lore.add(ChatColor.WHITE + "• " + ChatColor.GRAY + line);
            }
            meta.setLore(lore);

            icon.setItemMeta(meta);
            inv.setItem(slot, icon);
            slot++;
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "关闭");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(CLOSE_SLOT, close);

        viewer.openInventory(inv);
    }
}
