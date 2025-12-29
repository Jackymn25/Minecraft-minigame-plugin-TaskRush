package me.jacky.taskMaster.view;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class JoinTeamView {

    public void joinTeamMenu(Player player) {
        // 创建一个标题为金色"Join Team"、大小为9格的GUI菜单
// Bukkit.createInventory() 方法用于创建新容器
// 参数1: 菜单持有者 (这里应为玩家或null)
// 参数2: 菜单大小 (9格，即一行)
// 参数3: 菜单标题 (使用金色字体)
        Inventory main_menu = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Join Team");

// ==================== 创建"绿队"按钮 ====================
// 使用绿色羊毛作为绿队按钮的图标
        ItemStack green = new ItemStack(Material.GREEN_WOOL);
// 获取物品的元数据，用于设置显示名称和描述
        ItemMeta green_meta = green.getItemMeta();
// 设置按钮显示名称为绿色"Join Team Green"
        green_meta.setDisplayName(ChatColor.GREEN + "Join Team Green");
// 创建描述文本列表
        ArrayList<String> loreG = new ArrayList<>();
// 添加紫色描述文本
        loreG.add(ChatColor.DARK_PURPLE + "Click to Join Green");
// 将描述文本设置到物品元数据中
        green_meta.setLore(loreG);
// 将更新后的元数据应用回物品
        green.setItemMeta(green_meta);

// ==================== 创建"黄队"按钮 ====================
// 修复：为黄色物品创建新的ItemMeta对象，而不是使用green的meta
        ItemStack yellow = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta yellow_meta = yellow.getItemMeta(); // 修复此行
// 设置按钮显示名称为黄色"Join Team Yellow"
        yellow_meta.setDisplayName(ChatColor.YELLOW + "Join Team Yellow");
// 创建描述文本列表
        ArrayList<String> loreY = new ArrayList<>();
// 添加紫色描述文本
        loreY.add(ChatColor.DARK_PURPLE + "Click to Join Yellow");
// 将描述文本设置到物品元数据中
        yellow_meta.setLore(loreY);
// 将更新后的元数据应用回物品
        yellow.setItemMeta(yellow_meta);

// ==================== 创建"红队"按钮 ====================
        ItemStack red = new ItemStack(Material.RED_WOOL);
        ItemMeta red_meta = red.getItemMeta();
        red_meta.setDisplayName(ChatColor.RED + "Join Team Red");
        ArrayList<String> loreR = new ArrayList<>();
        loreR.add(ChatColor.DARK_PURPLE + "Click to Join Red");
        red_meta.setLore(loreR);
        red.setItemMeta(red_meta);

// ==================== 创建"蓝队"按钮 ====================
        ItemStack blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blue_meta = blue.getItemMeta();
        blue_meta.setDisplayName(ChatColor.BLUE + "Join Team Blue");
        ArrayList<String> loreB = new ArrayList<>();
        loreB.add(ChatColor.DARK_PURPLE + "Click to Join Blue");
        blue_meta.setLore(loreB);
        blue.setItemMeta(blue_meta);

// ==================== 创建"紫队"按钮 ====================
        ItemStack purple = new ItemStack(Material.PURPLE_WOOL);
        ItemMeta purple_meta = purple.getItemMeta();
        purple_meta.setDisplayName(ChatColor.DARK_PURPLE + "Join Team Purple");
        ArrayList<String> loreP = new ArrayList<>();
        loreP.add(ChatColor.DARK_PURPLE + "Click to Join Purple");
        purple_meta.setLore(loreP);
        purple.setItemMeta(purple_meta);

// ==================== 创建"青队"按钮 ====================
        ItemStack cyan = new ItemStack(Material.CYAN_WOOL);
        ItemMeta cyan_meta = cyan.getItemMeta();
        cyan_meta.setDisplayName(ChatColor.AQUA + "Join Team Cyan");
        ArrayList<String> loreC = new ArrayList<>();
        loreC.add(ChatColor.DARK_PURPLE + "Click to Join Cyan");
        cyan_meta.setLore(loreC);
        cyan.setItemMeta(cyan_meta);

// ==================== 创建"粉队"按钮 ====================
        ItemStack pink = new ItemStack(Material.PINK_WOOL);
        ItemMeta pink_meta = pink.getItemMeta();
        pink_meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Join Team Pink");
        ArrayList<String> lorePink = new ArrayList<>();
        lorePink.add(ChatColor.DARK_PURPLE + "Click to Join Pink");
        pink_meta.setLore(lorePink);
        pink.setItemMeta(pink_meta);

// ==================== 创建"白队"按钮 ====================
        ItemStack white = new ItemStack(Material.WHITE_WOOL);
        ItemMeta white_meta = white.getItemMeta();
        white_meta.setDisplayName(ChatColor.WHITE + "Join Team White");
        ArrayList<String> loreW = new ArrayList<>();
        loreW.add(ChatColor.DARK_PURPLE + "Click to Join White");
        white_meta.setLore(loreW);
        white.setItemMeta(white_meta);

// ==================== 将按钮添加到菜单 ====================
// 将各队伍按钮放置到菜单的不同位置
        main_menu.setItem(0, green);    // 索引0: 绿队
        main_menu.setItem(1, yellow);   // 索引1: 黄队
        main_menu.setItem(2, red);      // 索引2: 红队
        main_menu.setItem(3, blue);     // 索引3: 蓝队
        main_menu.setItem(4, purple);   // 索引4: 紫队
        main_menu.setItem(5, cyan);     // 索引5: 青队
        main_menu.setItem(6, pink);     // 索引6: 粉队
        main_menu.setItem(7, white);    // 索引7: 白队

// 注意: 9格菜单的索引8位置为空，可添加关闭按钮或其他功能

// 打开菜单供玩家使用
        player.openInventory(main_menu);
    }
}
