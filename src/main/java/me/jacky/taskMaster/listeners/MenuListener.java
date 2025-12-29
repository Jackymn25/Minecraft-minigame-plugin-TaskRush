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

public class MenuListener implements Listener {

    private final JoinTeamUseCase joinTeamUseCase;

    public MenuListener(JoinTeamUseCase joinTeamUseCase) {
        this.joinTeamUseCase = joinTeamUseCase;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        // 获取点击菜单的玩家
        Player player = (Player) e.getWhoClicked();

        // 定义菜单标题常量 - 与创建菜单时使用的标题一致
        // 注意：这里应该使用金色"Join Team"，因为菜单创建时是ChatColor.GOLD + "Join Team"
        final String TEAM_SELECTION_MENU = ChatColor.GOLD + "Join Team";

        // 检查点击的菜单是否为我们的队伍选择菜单
        // 注意：使用equals()而不是equalsIgnoreCase()，因为颜色代码区分大小写
        if (e.getView().getTitle().equals(TEAM_SELECTION_MENU)) {

            // 防止玩家将物品从菜单中拖出
            e.setCancelled(true);

            // 检查是否为有效的物品点击（避免点击空槽位）
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            int res = -1;

            // 根据点击的物品类型执行相应操作
            switch(e.getCurrentItem().getType()) {
                case GREEN_WOOL:
                    // 玩家点击绿队按钮
                    player.sendMessage(ChatColor.GREEN + "你已加入绿队！");
                    // 可以在此处添加将玩家加入绿队的逻辑
                    // 例如：teamManager.joinTeam(player, "green");
                    player.closeInventory(); // 关闭菜单
                    res = 0;
                    break;

                case YELLOW_WOOL:
                    // 玩家点击黄队按钮
                    player.sendMessage(ChatColor.YELLOW + "你已加入黄队！");
                    player.closeInventory();
                    res = 1;
                    break;

                case RED_WOOL:
                    // 玩家点击红队按钮
                    player.sendMessage(ChatColor.RED + "你已加入红队！");
                    player.closeInventory();
                    res = 2;
                    break;

                case BLUE_WOOL:
                    // 玩家点击蓝队按钮
                    player.sendMessage(ChatColor.BLUE + "你已加入蓝队！");
                    player.closeInventory();
                    res = 3;
                    break;

                case PURPLE_WOOL:
                    // 玩家点击紫队按钮
                    player.sendMessage(ChatColor.DARK_PURPLE + "你已加入紫队！");
                    player.closeInventory();
                    res = 4;
                    break;

                case CYAN_WOOL:
                    // 玩家点击青队按钮
                    player.sendMessage(ChatColor.AQUA + "你已加入青队！");
                    player.closeInventory();
                    res = 5;
                    break;

                case PINK_WOOL:
                    // 玩家点击粉队按钮
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "你已加入粉队！");
                    player.closeInventory();
                    res = 6;
                    break;

                case WHITE_WOOL:
                    // 玩家点击白队按钮
                    player.sendMessage(ChatColor.WHITE + "你已加入白队！");
                    player.closeInventory();
                    res = 7;
                    break;

                case BARRIER:
                    // 如果添加了关闭按钮（索引8位置）
                    player.sendMessage(ChatColor.RED + "已关闭队伍选择菜单");
                    player.closeInventory();
                    res = 8;
                    break;

                default:
                    // 点击了其他物品（理论上不会发生，但作为安全处理）
                    break;
            }
            if (res < 0) return;
            joinTeamUseCase.join(new JoinTeamInputData(player, res));
            // 可选：播放点击音效
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

    }

}


