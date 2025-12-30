package me.jacky.taskMaster.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class TaskGuiListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equals("所有队伍任务")) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;

        if (clicked.getType() == Material.BARRIER) {
            e.getWhoClicked().closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTitle() == null) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equals("所有队伍任务")) return;
        e.setCancelled(true);
    }
}
