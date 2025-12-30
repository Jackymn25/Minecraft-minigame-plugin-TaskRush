package me.jacky.taskMaster;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for creating and identifying the TaskMaster compass item.
 */
public final class TaskCompass {

    /**
     * Private constructor to hide utility class constructor.
     */
    private TaskCompass() {
        // Utility class.
    }

    /**
     * Creates a compass used to open the task GUI.
     *
     * @param plugin plugin instance used for NamespacedKey
     * @return a TaskMaster compass ItemStack
     */
    public static ItemStack create(final JavaPlugin plugin) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(ChatColor.GOLD + "任务指南针");

        List<String> lore = Arrays.asList(
                ChatColor.GRAY + "右键打开任务面板",
                ChatColor.DARK_GRAY + "显示所有队伍任务"
        );
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(plugin, "task_compass");
        meta.getPersistentDataContainer().set(
                key,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Checks whether the given item is the TaskMaster compass.
     *
     * @param plugin plugin instance used for NamespacedKey
     * @param item item to check
     * @return true if the item is a TaskMaster compass, false otherwise
     */
    public static boolean isTaskCompass(final JavaPlugin plugin, final ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        NamespacedKey key = new NamespacedKey(plugin, "task_compass");
        Byte v = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return v != null && v == (byte) 1;
    }
}
