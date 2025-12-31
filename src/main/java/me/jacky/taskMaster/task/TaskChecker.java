package me.jacky.taskMaster.task;

import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.game.Game;
import me.jacky.taskMaster.text.TaskTextFormatter;
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

import java.util.List;
import java.util.Map;

/**
 * 任务判定器 - 监听游戏事件并检查任务完成情况
 */
public class TaskChecker implements Listener {

    // ===== config keys: points =====
    private static final String CFG_POINTS_BLOCK = "block-weight";
    private static final String CFG_POINTS_ENTITY = "entity-weight";
    private static final String CFG_POINTS_HAVE_ITEM = "have-item-weight";
    private static final String CFG_POINTS_DEATH_TYPE = "death-type-weight";
    private static final String CFG_POINTS_ADVANCEMENT = "complete-advancement-weight";
    private static final String CFG_POINTS_CHAT = "player-chat-weight";

    private final Game game;
    private final TeamConfigManager teamConfigManager;
    private final JavaPlugin plugin;

    private final TaskParser taskParser;
    private final TaskTextFormatter formatter;

    public TaskChecker(final Game game,
                       final TeamConfigManager teamConfigManager,
                       final JavaPlugin plugin,
                       final TaskParser taskParser, TaskTextFormatter formatter) {
        this.game = game;
        this.teamConfigManager = teamConfigManager;
        this.plugin = plugin;
        this.taskParser = taskParser;
        this.formatter = formatter;

        plugin.getLogger().info("任务判定器已初始化");
    }

    // ===== points getters =====
    private int pointsBlock() { return plugin.getConfig().getInt(CFG_POINTS_BLOCK, 1); }
    private int pointsEntity() { return plugin.getConfig().getInt(CFG_POINTS_ENTITY, 1); }
    private int pointsHaveItem() { return plugin.getConfig().getInt(CFG_POINTS_HAVE_ITEM, 1); }
    private int pointsDeathType() { return plugin.getConfig().getInt(CFG_POINTS_DEATH_TYPE, 1); }
    private int pointsAdvancement() { return plugin.getConfig().getInt(CFG_POINTS_ADVANCEMENT, 1); }
    private int pointsChat() { return plugin.getConfig().getInt(CFG_POINTS_CHAT, 1); }

    // ===== common context =====
    private static final class TeamContext {
        private final Player player;
        private final String teamName;
        private final List<String> tasks;

        private TeamContext(Player player, String teamName, List<String> tasks) {
            this.player = player;
            this.teamName = teamName;
            this.tasks = tasks;
        }
    }

    private TeamContext ctxOrNull(final Player player) {
        if (!game.isGameRunning()) return null;

        String teamName = teamConfigManager.getPlayerTeam(player.getUniqueId());
        if (teamName == null) return null;

        List<String> teamTasks = game.getTeamActiveTasks(teamName);
        return new TeamContext(player, teamName, teamTasks);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(final BlockBreakEvent event) {
        TeamContext ctx = ctxOrNull(event.getPlayer());
        if (ctx == null) return;

        Material brokenBlock = event.getBlock().getType();
        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.BLOCK_BREAK) continue;

            if (spec.materialTarget != null && brokenBlock == spec.materialTarget) {
                int bonus = game.getBonusManager().getBlockBonus(spec.materialTarget);
                completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsBlock() + bonus);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        TeamContext ctx = ctxOrNull(player);
        if (ctx == null) return;

        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.HAVE_ITEM) continue;

            if (spec.materialTarget != null && hasItemInInventory(ctx.player, spec.materialTarget)) {
                int bonus = game.getBonusManager().getItemBonus(spec.materialTarget);
                completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsHaveItem() + bonus);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (!game.isGameRunning()) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        TeamContext ctx = ctxOrNull(killer);
        if (ctx == null) return;

        EntityType killedType = event.getEntityType();

        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.KILL_MOB) continue;

            if (spec.entityTarget != null && killedType == spec.entityTarget) {
                int bonus = game.getBonusManager().getMobBonus(spec.entityTarget);
                completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsEntity() + bonus);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        TeamContext ctx = ctxOrNull(event.getEntity());
        if (ctx == null) return;

        if (ctx.player.getLastDamageCause() == null) return;

        EntityDamageEvent.DamageCause cause = ctx.player.getLastDamageCause().getCause();

        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.DEATH_CAUSE) continue;

            if (spec.deathCauseTarget != null && spec.deathCauseTarget == cause) {
                int bonus = game.getBonusManager().getDeathBonus(cause);
                completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsDeathType() + bonus);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancementDone(final PlayerAdvancementDoneEvent event) {
        TeamContext ctx = ctxOrNull(event.getPlayer());
        if (ctx == null) return;

        Advancement adv = event.getAdvancement();
        String advancementKey = adv.getKey().getKey();

        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.ADVANCEMENT) continue;

            if (spec.advancementKeyTarget != null && spec.advancementKeyTarget.equals(advancementKey)) {
                int bonus = game.getBonusManager().getAdvancementBonus(advancementKey);
                completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsAdvancement() + bonus);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (!game.isGameRunning()) return;

        Player player = event.getPlayer();
        TeamContext ctx = ctxOrNull(player);
        if (ctx == null) return;

        String message = event.getMessage().trim();

        for (String taskRaw : ctx.tasks) {
            TaskSpec spec = taskParser.parse(taskRaw);
            if (spec.type != TaskType.CHAT_CODE) continue;

            if (spec.chatCodeTarget != null && message.equals(spec.chatCodeTarget)) {
                Bukkit.getScheduler().runTask(plugin,
                        () -> completeTask(ctx.player, ctx.teamName, spec.rawTask, pointsChat()));
                return;
            }
        }
    }

    private void completeTask(final Player player, final String teamName, final String taskRaw, final int points) {
        boolean success = game.completeTeamTask(teamName, taskRaw);

        if (success) {
            // 显示层统一走 formatter（逻辑依旧传 taskRaw）
            String display = formatter.toDisplay(taskRaw);

            player.sendMessage(ChatColor.GREEN + "✓ 完成任务: " + ChatColor.YELLOW + display);
            player.sendMessage(ChatColor.GREEN + "  获得 " + points + " 分！");

            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

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
                    String displayName = (String) info.getOrDefault("display-name", winningTeam);
                    player.sendMessage(ChatColor.GOLD + "🎉 游戏结束！获胜队伍: " + color + displayName);
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
}
