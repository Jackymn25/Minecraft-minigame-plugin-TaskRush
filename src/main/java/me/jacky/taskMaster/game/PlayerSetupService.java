package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.TeamConfigManager;
import me.jacky.taskMaster.text.TaskTextFormatter;
import me.jacky.taskMaster.view.TaskCompass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public final class PlayerSetupService {

    private static final long TIME_MORNING = 0L;

    private static final double BASE_MAX_HEALTH = 20.0;
    private static final double BASE_HEALTH = 20.0;
    private static final int BASE_FOOD = 20;
    private static final float BASE_SATURATION = 10.0f;

    private static final int START_TITLE_FADE_IN = 10;
    private static final int START_TITLE_STAY = 70;
    private static final int START_TITLE_FADE_OUT = 20;

    private static final int START_BREAD_AMOUNT = 16;

    private static final int SPEED_DURATION_TICKS = 1200;
    private static final int JUMP_DURATION_TICKS = 1200;

    private static final int SLOW_FALL_TICKS = 540;

    private final JavaPlugin plugin;
    private final TeamConfigManager teamConfigManager;
    private final TaskTextFormatter formatter;
    private final ScoreboardService scoreboardService;

    public PlayerSetupService(JavaPlugin plugin,
                              TeamConfigManager teamConfigManager,
                              TaskTextFormatter formatter,
                              ScoreboardService scoreboardService) {
        this.plugin = plugin;
        this.teamConfigManager = teamConfigManager;
        this.formatter = formatter;
        this.scoreboardService = scoreboardService;
    }

    public void setupTeamPlayers(final Map<String, TeamState> teamTasks) {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            world.setTime(TIME_MORNING);
            world.setStorm(false);
            world.setThundering(false);
        }

        Map<String, List<Player>> teamPlayers = teamConfigManager.getTeamOnlinePlayersMap();

        for (String teamName : teamPlayers.keySet()) {
            List<Player> players = teamPlayers.get(teamName);
            if (players == null) continue;

            for (Player player : players) {
                player.setGameMode(GameMode.SURVIVAL);

                AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(BASE_MAX_HEALTH);
                }

                player.setHealth(BASE_HEALTH);
                player.setFoodLevel(BASE_FOOD);
                player.setSaturation(BASE_SATURATION);

                PlayerInventory inventory = player.getInventory();
                inventory.clear();

                inventory.addItem(new ItemStack(Material.STONE_SWORD));
                inventory.addItem(new ItemStack(Material.STONE_PICKAXE));
                inventory.addItem(new ItemStack(Material.BREAD, START_BREAD_AMOUNT));

                player.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);

                player.sendTitle(
                        ChatColor.GOLD + "任务大师游戏开始！",
                        ChatColor.YELLOW + "你有3个任务需要完成",
                        START_TITLE_FADE_IN,
                        START_TITLE_STAY,
                        START_TITLE_FADE_OUT
                );

                // send tasks
                player.sendMessage(ChatColor.GOLD + "========== 你的任务 ==========");
                TeamState tasks = teamTasks.get(teamName);
                if (tasks != null) {
                    int taskNum = 1;
                    for (String task : tasks.getActiveTasks()) {
                        player.sendMessage(
                                ChatColor.YELLOW + Integer.toString(taskNum) + ". "
                                        + ChatColor.WHITE + formatter.toDisplay(task)
                        );
                        taskNum++;
                    }
                }
                player.sendMessage(ChatColor.GOLD + "============================");
            }
        }
    }

    public void setupSpectators() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String team = teamConfigManager.getPlayerTeam(player.getUniqueId());
            if (team != null) continue;

            player.setGameMode(GameMode.SPECTATOR);

            ItemStack infoCompass = TaskCompass.create(plugin);
            player.getInventory().clear();
            player.getInventory().addItem(infoCompass.clone());

            if (scoreboardService.getScoreboardOrNull() != null) {
                scoreboardService.applyToPlayer(player);
            }

            player.sendMessage("§b你尚未选择队伍，已进入旁观模式。");
            player.sendMessage("§7使用指南针可以查看任务/队伍信息。");
        }
    }

    public void applyPotionEffectsToTeamPlayers() {
        List<Player> allPlayers = teamConfigManager.getAllOnlineTeamPlayers();
        for (Player player : allPlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION_TICKS, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, JUMP_DURATION_TICKS, 1, false, false));

            ItemStack compass = TaskCompass.create(plugin);
            if (!player.getInventory().contains(Material.COMPASS)) {
                player.getInventory().addItem(compass.clone());
            }

            player.sendMessage(ChatColor.AQUA + "你获得了速度和跳跃提升效果，持续60秒！");
        }
    }
}
