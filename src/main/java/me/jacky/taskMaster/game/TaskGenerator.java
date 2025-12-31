package me.jacky.taskMaster.game;

import me.jacky.taskMaster.config.BonusManager;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates tasks based on probability weights (from config) and pools (from BonusManager).
 * Output format matches your current Game.java:
 *   BLOCK_BREAK:STONE
 *   HAVE_ITEM:DIAMOND
 *   KILL_MOB:ZOMBIE
 *   DEATH_CAUSE:FALL
 *   ADVANCEMENT:story/mine_diamond
 *   CHAT_CODE:AbC... (10 chars)
 */
public final class TaskGenerator {

    // ====== config keys: 概率权重（与你 Game.java 完全一致） ======
    private static final String CFG_PROB_BLOCK = "block-probability-weight";
    private static final String CFG_PROB_ENTITY = "entity-hunt-probability-weight";
    private static final String CFG_PROB_HAVE_ITEM = "have-item-probability-weight";
    private static final String CFG_PROB_DEATH_TYPE = "death-type-probability-weight";
    private static final String CFG_PROB_ADVANCEMENT = "complete-advancement-probability-weight";
    private static final String CFG_PROB_CHAT = "type-chat-probability-weight";

    /** 任务类型（保持与你 Game.java 当前 enum 的语义一致） */
    public enum TaskType {
        FIND_BLOCK,
        HAVE_ITEM,
        KILL_MOB,
        DEATH_TYPE,
        COMPLETE_ACHIEVEMENT,
        TYPE_CHAT
    }

    /** 任务输出数据：name=taskKey, value=payload(可选) */
    private static final class TaskOutputData {
        private final String name;
        private final Object value;

        private TaskOutputData(String name, Object value) {
            this.name = name;
            this.value = value;
        }
        public String getName() { return name; }
        public Object getValue() { return value; }
    }

    private final JavaPlugin plugin;
    private final BonusManager bonusManager;
    private final Random random;

    /** 概率权重表 */
    private final Map<TaskType, Integer> taskTypeProbabilityWeights = new HashMap<>();

    public TaskGenerator(JavaPlugin plugin, BonusManager bonusManager, Random random) {
        this.plugin = plugin;
        this.bonusManager = bonusManager;
        this.random = random;
    }

    /**
     * 从 config.yml 读取任务类型概率权重。
     * 若缺省则使用合理默认值，避免全 0 造成随机崩溃。
     */
    public void reloadWeightsFromConfig() {
        int block = plugin.getConfig().getInt(CFG_PROB_BLOCK, 19);
        int entity = plugin.getConfig().getInt(CFG_PROB_ENTITY, 19);
        int haveItem = plugin.getConfig().getInt(CFG_PROB_HAVE_ITEM, 19);
        int deathType = plugin.getConfig().getInt(CFG_PROB_DEATH_TYPE, 19);
        int advancement = plugin.getConfig().getInt(CFG_PROB_ADVANCEMENT, 19);
        int chat = plugin.getConfig().getInt(CFG_PROB_CHAT, 5);

        taskTypeProbabilityWeights.put(TaskType.FIND_BLOCK, Math.max(0, block));
        taskTypeProbabilityWeights.put(TaskType.KILL_MOB, Math.max(0, entity));
        taskTypeProbabilityWeights.put(TaskType.HAVE_ITEM, Math.max(0, haveItem));
        taskTypeProbabilityWeights.put(TaskType.DEATH_TYPE, Math.max(0, deathType));
        taskTypeProbabilityWeights.put(TaskType.COMPLETE_ACHIEVEMENT, Math.max(0, advancement));
        taskTypeProbabilityWeights.put(TaskType.TYPE_CHAT, Math.max(0, chat));
    }

    /** 生成一个新的 task key 字符串 */
    public String nextTaskKey() {
        int totalWeight = 0;
        for (TaskType type : TaskType.values()) {
            totalWeight += taskTypeProbabilityWeights.getOrDefault(type, 0);
        }
        if (totalWeight <= 0) {
            return "未知任务";
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        TaskType selectedType = null;
        for (TaskType type : TaskType.values()) {
            cumulativeWeight += taskTypeProbabilityWeights.getOrDefault(type, 0);
            if (randomValue < cumulativeWeight) {
                selectedType = type;
                break;
            }
        }

        if (selectedType == null) {
            return "未知任务";
        }
        return generateTaskDetail(selectedType).getName();
    }

    private TaskOutputData generateTaskDetail(final TaskType type) {
        switch (type) {
            case FIND_BLOCK:
                return findBlockTask();
            case HAVE_ITEM:
                return haveItemTask();
            case KILL_MOB:
                return killMobTask();
            case DEATH_TYPE:
                return deathTypeTask();
            case COMPLETE_ACHIEVEMENT:
                return achievementTask();
            case TYPE_CHAT:
                return typeChatTask();
            default:
                return new TaskOutputData("未知任务", null);
        }
    }

    private TaskOutputData findBlockTask() {
        List<Material> pool = bonusManager.getBlockPool();
        Material block = pool.get(random.nextInt(pool.size()));
        String key = "BLOCK_BREAK:" + block.name();
        return new TaskOutputData(key, block.name());
    }

    private TaskOutputData haveItemTask() {
        List<Material> pool = bonusManager.getItemPool();
        Material item = pool.get(random.nextInt(pool.size()));
        String key = "HAVE_ITEM:" + item.name();
        return new TaskOutputData(key, item.name());
    }

    private TaskOutputData killMobTask() {
        List<EntityType> pool = bonusManager.getMobPool();
        EntityType mob = pool.get(random.nextInt(pool.size()));
        String key = "KILL_MOB:" + mob.name();
        return new TaskOutputData(key, mob.name());
    }

    private TaskOutputData deathTypeTask() {
        List<EntityDamageEvent.DamageCause> pool = bonusManager.getDeathPool();
        EntityDamageEvent.DamageCause deathType = pool.get(random.nextInt(pool.size()));
        String key = "DEATH_CAUSE:" + deathType.name();
        return new TaskOutputData(key, deathType.name());
    }

    private TaskOutputData achievementTask() {
        List<String> pool = bonusManager.getAdvancementPool();
        String achievement = pool.get(random.nextInt(pool.size()));
        String key = "ADVANCEMENT:" + achievement;
        return new TaskOutputData(key, achievement);
    }

    private TaskOutputData typeChatTask() {
        String chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                        + "!@#$%^&*()+_-~[]{};:.,<>?/|\\";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        String key = "CHAT_CODE:" + code;
        return new TaskOutputData(key, code.toString());
    }
}
