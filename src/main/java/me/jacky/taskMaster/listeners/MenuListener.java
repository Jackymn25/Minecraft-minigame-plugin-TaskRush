package me.jacky.taskMaster.listeners;

import me.jacky.taskMaster.uscase.JoinTeamInputData;
import me.jacky.taskMaster.uscase.JoinTeamUseCase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles clicks in the team selection GUI.
 */
public class MenuListener implements Listener {

    /** Team selection menu title (must match the GUI title). */
    private static final String TEAM_SELECTION_MENU_TITLE =
            ChatColor.GOLD + "Join Team";

    /** Result code for closing the menu. */
    private static final int CLOSE_RESULT = 8;

    /** Result code indicating an invalid selection. */
    private static final int INVALID_RESULT = -1;

    private final JoinTeamUseCase joinTeamUseCase;

    /**
     * Creates a menu listener.
     *
     * @param joinTeamUseCase use case for joining teams
     */
    public MenuListener(final JoinTeamUseCase joinTeamUseCase) {
        this.joinTeamUseCase = joinTeamUseCase;
    }

    /**
     * Handles inventory clicks and routes team selection to the use case.
     *
     * @param event the inventory click event
     */
    @EventHandler
    public void onMenuClick(final InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(TEAM_SELECTION_MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        int res = mapClickToResult(player, event.getCurrentItem().getType());
        if (res < 0) {
            return;
        }

        joinTeamUseCase.join(new JoinTeamInputData(player, res));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    /**
     * Maps clicked item type to team selection result code.
     *
     * @param player the clicking player
     * @param type the clicked item type
     * @return result code used by JoinTeamUseCase
     */
    private int mapClickToResult(final Player player, final Material type) {
        switch (type) {
            case GREEN_WOOL:
                player.sendMessage(ChatColor.GREEN + "你已加入绿队！");
                player.closeInventory();
                return 0;
            case YELLOW_WOOL:
                player.sendMessage(ChatColor.YELLOW + "你已加入黄队！");
                player.closeInventory();
                return 1;
            case RED_WOOL:
                player.sendMessage(ChatColor.RED + "你已加入红队！");
                player.closeInventory();
                return 2;
            case BLUE_WOOL:
                player.sendMessage(ChatColor.BLUE + "你已加入蓝队！");
                player.closeInventory();
                return 3;
            case PURPLE_WOOL:
                player.sendMessage(ChatColor.DARK_PURPLE + "你已加入紫队！");
                player.closeInventory();
                return 4;
            case CYAN_WOOL:
                player.sendMessage(ChatColor.AQUA + "你已加入青队！");
                player.closeInventory();
                return 5;
            case PINK_WOOL:
                player.sendMessage(ChatColor.LIGHT_PURPLE + "你已加入粉队！");
                player.closeInventory();
                return 6;
            case WHITE_WOOL:
                player.sendMessage(ChatColor.WHITE + "你已加入白队！");
                player.closeInventory();
                return 7;
            case BARRIER:
                player.sendMessage(ChatColor.RED + "已关闭队伍选择菜单");
                player.closeInventory();
                return CLOSE_RESULT;
            default:
                return INVALID_RESULT;
        }
    }
}
