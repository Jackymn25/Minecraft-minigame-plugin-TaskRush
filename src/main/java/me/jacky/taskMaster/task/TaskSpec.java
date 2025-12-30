package me.jacky.taskMaster.task;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

public class TaskSpec {
    public final String rawTask; // 原始 TaskKey 字符串
    public final TaskType type;

    public final Material materialTarget;
    public final EntityType entityTarget;
    public final EntityDamageEvent.DamageCause deathCauseTarget;
    public final String advancementKeyTarget;
    public final String chatCodeTarget;

    private TaskSpec(
            String rawTask,
            TaskType type,
            Material materialTarget,
            EntityType entityTarget,
            EntityDamageEvent.DamageCause deathCauseTarget,
            String advancementKeyTarget,
            String chatCodeTarget
    ) {
        this.rawTask = rawTask;
        this.type = type;
        this.materialTarget = materialTarget;
        this.entityTarget = entityTarget;
        this.deathCauseTarget = deathCauseTarget;
        this.advancementKeyTarget = advancementKeyTarget;
        this.chatCodeTarget = chatCodeTarget;
    }

    public static TaskSpec blockBreak(String rawTask, Material target) {
        return new TaskSpec(rawTask, TaskType.BLOCK_BREAK, target, null, null, null, null);
    }

    public static TaskSpec haveItem(String rawTask, Material target) {
        return new TaskSpec(rawTask, TaskType.HAVE_ITEM, target, null, null, null, null);
    }

    public static TaskSpec killMob(String rawTask, EntityType target) {
        return new TaskSpec(rawTask, TaskType.KILL_MOB, null, target, null, null, null);
    }

    public static TaskSpec deathCause(String rawTask, EntityDamageEvent.DamageCause target) {
        return new TaskSpec(rawTask, TaskType.DEATH_CAUSE, null, null, target, null, null);
    }

    public static TaskSpec advancement(String rawTask, String key) {
        return new TaskSpec(rawTask, TaskType.ADVANCEMENT, null, null, null, key, null);
    }

    public static TaskSpec chatCode(String rawTask, String code) {
        return new TaskSpec(rawTask, TaskType.CHAT_CODE, null, null, null, null, code);
    }

    public static TaskSpec unknown(String rawTask) {
        return new TaskSpec(rawTask, TaskType.UNKNOWN, null, null, null, null, null);
    }
}
