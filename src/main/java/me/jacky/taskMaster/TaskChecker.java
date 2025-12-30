package me.jacky.taskMaster;

import me.jacky.taskMaster.config.TeamConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务判定器 - 监听游戏事件并检查任务完成情况
 * 负责将玩家的游戏行为与队伍任务进行匹配
 */
public class TaskChecker implements Listener {

    private static final String PREFIX_FIND = "找到 ";
    private static final String SUFFIX_BLOCK = " 方块";

    private static final String PREFIX_HAVE_ITEM = "收集 ";
    private static final String SUFFIX_ITEM = " 物品";

    private static final String PREFIX_KILL = "杀死一只 ";

    private static final String PREFIX_DEATH = "尝试 ";

    private static final String PREFIX_ADV = "完成成就: ";

    private static final String PREFIX_CHAT = "在聊天框输入: ";

    // config keys: 分数权重
    private static final String CFG_POINTS_BLOCK = "block-weight";
    private static final String CFG_POINTS_ENTITY = "entity-weight";
    private static final String CFG_POINTS_HAVE_ITEM = "have-item-weight";
    private static final String CFG_POINTS_DEATH_TYPE = "death-type-weight";
    private static final String CFG_POINTS_ADVANCEMENT = "complete-advancement-weight";
    private static final String CFG_POINTS_CHAT = "player-chat-weight";

    private final Game game;
    private final TeamConfigManager teamConfigManager;
    private final JavaPlugin plugin;

    /**
     * 构造函数
     *
     * @param game              游戏主类
     * @param teamConfigManager 队伍配置管理器
     * @param plugin            插件主类
     */
    public TaskChecker(
            final Game game,
            final TeamConfigManager teamConfigManager,
            final JavaPlugin plugin
    ) {
        this.game = game;
        this.teamConfigManager = teamConfigManager;
        this.plugin = plugin;

        plugin.getLogger().info("任务判定器已初始化");
    }

    private int pointsBlock() {
        return plugin.getConfig().getInt(CFG_POINTS_BLOCK, 1);
    }

    private int pointsEntity() {
        return plugin.getConfig().getInt(CFG_POINTS_ENTITY, 1);
    }

    private int pointsHaveItem() {
        return plugin.getConfig().getInt(CFG_POINTS_HAVE_ITEM, 1);
    }

    private int pointsDeathType() {
        return plugin.getConfig().getInt(CFG_POINTS_DEATH_TYPE, 1);
    }

    private int pointsAdvancement() {
        return plugin.getConfig().getInt(CFG_POINTS_ADVANCEMENT, 1);
    }

    private int pointsChat() {
        return plugin.getConfig().getInt(CFG_POINTS_CHAT, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!game.isGameRunning()) {
            return;
        }

        Player player = event.getPlayer();
        Material brokenBlock = event.getBlock().getType();

        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            return;
        }

        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_FIND) && task.endsWith(SUFFIX_BLOCK)) {
                String blockName = task.substring(
                        PREFIX_FIND.length(),
                        task.length() - SUFFIX_BLOCK.length()
                );

                Material targetMaterial = getMaterialFromDisplayName(blockName);

                if (targetMaterial != null && brokenBlock == targetMaterial) {
                    int bonus = game.getBonusManager().getBlockBonus(targetMaterial);
                    completeTask(player, teamName, task, pointsBlock() + bonus);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!game.isGameRunning()) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            return;
        }

        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_HAVE_ITEM) && task.endsWith(SUFFIX_ITEM)) {
                String itemName = task.substring(
                        PREFIX_HAVE_ITEM.length(),
                        task.length() - SUFFIX_ITEM.length()
                );
                Material targetMaterial = getMaterialFromDisplayName(itemName);

                if (targetMaterial != null && hasItemInInventory(player, targetMaterial)) {
                    int bonus = game.getBonusManager().getItemBonus(targetMaterial);
                    completeTask(player, teamName, task, pointsHaveItem() + bonus);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (!game.isGameRunning()) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        String teamName = teamConfigManager.getPlayerTeam(killer.getUniqueId());
        if (teamName == null) {
            return;
        }

        EntityType killedType = event.getEntityType();
        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_KILL)) {
                String mobName = task.substring(PREFIX_KILL.length()).trim();
                EntityType targetType = getEntityTypeFromName(mobName);

                if (targetType != null && killedType == targetType) {
                    int bonus = game.getBonusManager().getMobBonus(targetType);
                    completeTask(killer, teamName, task, pointsEntity() + bonus);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (!game.isGameRunning()) {
            return;
        }

        Player player = event.getEntity();
        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            return;
        }

        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_DEATH)) {
                String deathType = task.substring(PREFIX_DEATH.length());

                if (player.getLastDamageCause() != null) {

                    EntityDamageEvent.DamageCause cause = player.getLastDamageCause().getCause();
                    if (deathType.equalsIgnoreCase(cause.toString())) {
                        int bonus = game.getBonusManager().getDeathBonus(cause);
                        completeTask(player, teamName, task, pointsDeathType() + bonus);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancementDone(final PlayerAdvancementDoneEvent event) {
        if (!game.isGameRunning()) {
            return;
        }

        Player player = event.getPlayer();
        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            return;
        }

        Advancement advancement = event.getAdvancement();
        String advancementKey = advancement.getKey().getKey();

        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_ADV)) {
                String targetAdvancement = task.substring(PREFIX_ADV.length()).trim();

                if (advancementKey.equals(targetAdvancement)) {
                    int bonus = game.getBonusManager().getAdvancementBonus(targetAdvancement);
                    completeTask(player, teamName, task, pointsAdvancement() + bonus);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (!game.isGameRunning()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) {
            return;
        }

        List<String> teamTasks = game.getTeamActiveTasks(teamName);

        for (String task : teamTasks) {
            if (task.startsWith(PREFIX_CHAT)) {
                String targetCode = task.substring(PREFIX_CHAT.length()).trim();
                if (message.equals(targetCode)) {
                    Bukkit.getScheduler().runTask(
                            plugin,
                            () -> completeTask(player, teamName, task, pointsChat())
                    );
                    break;
                }
            }
        }
    }

    private void completeTask(
            final Player player,
            final String teamName,
            final String task,
            final int points
    ) {
        boolean success = game.completeTeamTask(teamName, task, points);

        if (success) {
            player.sendMessage(ChatColor.GREEN + "✓ 完成任务: " + ChatColor.YELLOW + task);
            player.sendMessage(ChatColor.GREEN + "  获得 " + points + " 分！");

            player.playSound(
                    player.getLocation(),
                    org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                    1.0f,
                    1.0f
            );

            player.spawnParticle(
                    org.bukkit.Particle.VILLAGER_HAPPY,
                    player.getLocation().add(0, 2, 0),
                    10,
                    0.5,
                    0.5,
                    0.5
            );

            if (!game.isGameRunning()) {
                String winningTeam = game.getWinningTeam();
                if (winningTeam != null) {
                    Map<String, Object> info = teamConfigManager.getTeamInfo(winningTeam);
                    String color = (String) info.getOrDefault("color", "§f");
                    String display = (String) info.getOrDefault("display-name", winningTeam);
                    player.sendMessage(ChatColor.GOLD + "🎉 游戏结束！获胜队伍: " + color + display);
                }
            }
        }
    }

    private boolean hasItemInInventory(final Player player, final Material material) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && item.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    private Material getMaterialFromDisplayName(final String displayName) {
        try {
            String enumName = displayName.toUpperCase()
                    .replace(" ", "_")
                    .replace("矿石", "_ORE")
                    .replace("锭", "_INGOT")
                    .replace("珍珠", "_PEARL")
                    .replace("棒", "_ROD")
                    .replace("泪", "_TEAR");

            Material material = Material.getMaterial(enumName);
            if (material != null) {
                return material;
            }

            Map<String, Material> manualMapping = new HashMap<>();
            manualMapping.put("钻石矿石", Material.DIAMOND_ORE);
            manualMapping.put("绿宝石矿石", Material.EMERALD_ORE);
            manualMapping.put("远古残骸", Material.ANCIENT_DEBRIS);
            manualMapping.put("下界石英矿石", Material.NETHER_QUARTZ_ORE);
            manualMapping.put("青金石矿石", Material.LAPIS_ORE);
            manualMapping.put("金矿石", Material.GOLD_ORE);
            manualMapping.put("钻石", Material.DIAMOND);
            manualMapping.put("绿宝石", Material.EMERALD);
            manualMapping.put("下界合金锭", Material.NETHERITE_INGOT);
            manualMapping.put("末影珍珠", Material.ENDER_PEARL);
            manualMapping.put("烈焰棒", Material.BLAZE_ROD);
            manualMapping.put("恶魂之泪", Material.GHAST_TEAR);

            return manualMapping.get(displayName);

        } catch (Exception e) {
            plugin.getLogger().warning("无法转换显示名称: " + displayName);
            return null;
        }
    }

    private EntityType getEntityTypeFromName(final String mobName) {
        try {
            return EntityType.valueOf(mobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, EntityType> manualMapping = new HashMap<>();
            manualMapping.put("骷髅", EntityType.SKELETON);
            manualMapping.put("僵尸", EntityType.ZOMBIE);
            manualMapping.put("爬行者", EntityType.CREEPER);
            manualMapping.put("蜘蛛", EntityType.SPIDER);
            manualMapping.put("末影人", EntityType.ENDERMAN);
            manualMapping.put("苦力怕", EntityType.CREEPER);

            return manualMapping.get(mobName);
        }
    }
}
