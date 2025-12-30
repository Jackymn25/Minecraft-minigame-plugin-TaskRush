package me.jacky.taskMaster.view;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * View for the "Join Team" GUI.
 * Provides a simple inventory menu for players to pick a team.
 */
public class JoinTeamView {

    /** GUI size (one row). */
    private static final int MENU_SIZE = 9;

    /** Slot indices. */
    private static final int SLOT_GREEN = 0;
    private static final int SLOT_YELLOW = 1;
    private static final int SLOT_RED = 2;
    private static final int SLOT_BLUE = 3;
    private static final int SLOT_PURPLE = 4;
    private static final int SLOT_CYAN = 5;
    private static final int SLOT_PINK = 6;
    private static final int SLOT_WHITE = 7;

    /**
     * Opens the join team menu for a player.
     *
     * @param player the player who will see the menu
     */
    public void joinTeamMenu(final Player player) {
        Inventory mainMenu = Bukkit.createInventory(
                player,
                MENU_SIZE,
                ChatColor.GOLD + "Join Team"
        );

        // Green button.
        ItemStack green = new ItemStack(Material.GREEN_WOOL);
        ItemMeta greenMeta = green.getItemMeta();
        if (greenMeta != null) {
            greenMeta.setDisplayName(ChatColor.GREEN + "Join Team Green");
            ArrayList<String> loreG = new ArrayList<>();
            loreG.add(ChatColor.DARK_PURPLE + "Click to Join Green");
            greenMeta.setLore(loreG);
            green.setItemMeta(greenMeta);
        }

        // Yellow button.
        ItemStack yellow = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta yellowMeta = yellow.getItemMeta();
        if (yellowMeta != null) {
            yellowMeta.setDisplayName(ChatColor.YELLOW + "Join Team Yellow");
            ArrayList<String> loreY = new ArrayList<>();
            loreY.add(ChatColor.DARK_PURPLE + "Click to Join Yellow");
            yellowMeta.setLore(loreY);
            yellow.setItemMeta(yellowMeta);
        }

        // Red button.
        ItemStack red = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = red.getItemMeta();
        if (redMeta != null) {
            redMeta.setDisplayName(ChatColor.RED + "Join Team Red");
            ArrayList<String> loreR = new ArrayList<>();
            loreR.add(ChatColor.DARK_PURPLE + "Click to Join Red");
            redMeta.setLore(loreR);
            red.setItemMeta(redMeta);
        }

        // Blue button.
        ItemStack blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blue.getItemMeta();
        if (blueMeta != null) {
            blueMeta.setDisplayName(ChatColor.BLUE + "Join Team Blue");
            ArrayList<String> loreB = new ArrayList<>();
            loreB.add(ChatColor.DARK_PURPLE + "Click to Join Blue");
            blueMeta.setLore(loreB);
            blue.setItemMeta(blueMeta);
        }

        // Purple button.
        ItemStack purple = new ItemStack(Material.PURPLE_WOOL);
        ItemMeta purpleMeta = purple.getItemMeta();
        if (purpleMeta != null) {
            purpleMeta.setDisplayName(ChatColor.DARK_PURPLE + "Join Team Purple");
            ArrayList<String> loreP = new ArrayList<>();
            loreP.add(ChatColor.DARK_PURPLE + "Click to Join Purple");
            purpleMeta.setLore(loreP);
            purple.setItemMeta(purpleMeta);
        }

        // Cyan button.
        ItemStack cyan = new ItemStack(Material.CYAN_WOOL);
        ItemMeta cyanMeta = cyan.getItemMeta();
        if (cyanMeta != null) {
            cyanMeta.setDisplayName(ChatColor.AQUA + "Join Team Cyan");
            ArrayList<String> loreC = new ArrayList<>();
            loreC.add(ChatColor.DARK_PURPLE + "Click to Join Cyan");
            cyanMeta.setLore(loreC);
            cyan.setItemMeta(cyanMeta);
        }

        // Pink button.
        ItemStack pink = new ItemStack(Material.PINK_WOOL);
        ItemMeta pinkMeta = pink.getItemMeta();
        if (pinkMeta != null) {
            pinkMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Join Team Pink");
            ArrayList<String> lorePink = new ArrayList<>();
            lorePink.add(ChatColor.DARK_PURPLE + "Click to Join Pink");
            pinkMeta.setLore(lorePink);
            pink.setItemMeta(pinkMeta);
        }

        // White button.
        ItemStack white = new ItemStack(Material.WHITE_WOOL);
        ItemMeta whiteMeta = white.getItemMeta();
        if (whiteMeta != null) {
            whiteMeta.setDisplayName(ChatColor.WHITE + "Join Team White");
            ArrayList<String> loreW = new ArrayList<>();
            loreW.add(ChatColor.DARK_PURPLE + "Click to Join White");
            whiteMeta.setLore(loreW);
            white.setItemMeta(whiteMeta);
        }

        // Place items.
        mainMenu.setItem(SLOT_GREEN, green);
        mainMenu.setItem(SLOT_YELLOW, yellow);
        mainMenu.setItem(SLOT_RED, red);
        mainMenu.setItem(SLOT_BLUE, blue);
        mainMenu.setItem(SLOT_PURPLE, purple);
        mainMenu.setItem(SLOT_CYAN, cyan);
        mainMenu.setItem(SLOT_PINK, pink);
        mainMenu.setItem(SLOT_WHITE, white);

        // Open menu.
        player.openInventory(mainMenu);
    }
}
