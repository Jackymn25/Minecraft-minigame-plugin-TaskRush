package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.BonusManager;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Centralized points calculation for task completion.
 * Reads base points from config and adds bonus from BonusManager when applicable.
 *
 * Task key format:
 *  BLOCK_BREAK:STONE
 *  HAVE_ITEM:DIAMOND
 *  KILL_MOB:ZOMBIE
 *  DEATH_CAUSE:FALL
 *  ADVANCEMENT:story/mine_diamond
 *  CHAT_CODE:AbCd...
 */
public final class PointsCalculator {

    // ====== config keys: 分数权重（与你 Game.java 完全一致） ======
    private static final String CFG_POINTS_BLOCK = "block-weight";
    private static final String CFG_POINTS_ENTITY = "entity-weight";
    private static final String CFG_POINTS_HAVE_ITEM = "have-item-weight";
    private static final String CFG_POINTS_DEATH_TYPE = "death-type-weight";
    private static final String CFG_POINTS_ADVANCEMENT = "complete-advancement-weight";
    private static final String CFG_POINTS_CHAT = "player-chat-weight";

    // task prefixes
    private static final String P_BLOCK = "BLOCK_BREAK:";
    private static final String P_ITEM = "HAVE_ITEM:";
    private static final String P_MOB = "KILL_MOB:";
    private static final String P_DEATH = "DEATH_CAUSE:";
    private static final String P_ADV = "ADVANCEMENT:";
    private static final String P_CHAT = "CHAT_CODE:";

    private final JavaPlugin plugin;
    private final BonusManager bonusManager;

    public PointsCalculator(JavaPlugin plugin, BonusManager bonusManager) {
        this.plugin = plugin;
        this.bonusManager = bonusManager;
    }

    public int pointsFor(final String taskKey) {
        if (taskKey == null) return 0;

        // BLOCK_BREAK:STONE
        if (taskKey.startsWith(P_BLOCK)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_BLOCK, 1);

            String matName = taskKey.substring(P_BLOCK.length()).trim();
            Material m = Material.getMaterial(matName.toUpperCase());
            int bonus = (m == null) ? 0 : bonusManager.getBlockBonus(m);

            return safe(base + bonus);
        }

        // HAVE_ITEM:DIAMOND
        if (taskKey.startsWith(P_ITEM)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_HAVE_ITEM, 1);

            String matName = taskKey.substring(P_ITEM.length()).trim();
            Material m = Material.getMaterial(matName.toUpperCase());
            int bonus = (m == null) ? 0 : bonusManager.getItemBonus(m);

            return safe(base + bonus);
        }

        // KILL_MOB:ZOMBIE
        if (taskKey.startsWith(P_MOB)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_ENTITY, 1);

            String mobName = taskKey.substring(P_MOB.length()).trim();
            EntityType t;
            try {
                t = EntityType.valueOf(mobName.toUpperCase());
            } catch (IllegalArgumentException e) {
                t = null;
            }
            int bonus = (t == null) ? 0 : bonusManager.getMobBonus(t);

            return safe(base + bonus);
        }

        // DEATH_CAUSE:FALL
        if (taskKey.startsWith(P_DEATH)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_DEATH_TYPE, 1);

            String causeName = taskKey.substring(P_DEATH.length()).trim();
            EntityDamageEvent.DamageCause c;
            try {
                c = EntityDamageEvent.DamageCause.valueOf(causeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                c = null;
            }
            int bonus = (c == null) ? 0 : bonusManager.getDeathBonus(c);

            return safe(base + bonus);
        }

        // ADVANCEMENT:story/mine_diamond
        if (taskKey.startsWith(P_ADV)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_ADVANCEMENT, 1);
            String key = taskKey.substring(P_ADV.length()).trim();
            int bonus = bonusManager.getAdvancementBonus(key);

            return safe(base + bonus);
        }

        // CHAT_CODE:xxxx
        if (taskKey.startsWith(P_CHAT)) {
            int base = plugin.getConfig().getInt(CFG_POINTS_CHAT, 1);
            return safe(base);
        }

        return 0;
    }

    private int safe(int v) {
        return Math.max(0, v);
    }
}
