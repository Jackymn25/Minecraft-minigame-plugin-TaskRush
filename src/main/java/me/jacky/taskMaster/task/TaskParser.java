package me.jacky.taskMaster.task;

import me.jacky.taskMaster.resolver.EntityTypeResolver;
import me.jacky.taskMaster.resolver.MaterialResolver;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

public class TaskParser {

    private final MaterialResolver materialResolver;
    private final EntityTypeResolver entityTypeResolver;

    public TaskParser(MaterialResolver materialResolver, EntityTypeResolver entityTypeResolver) {
        this.materialResolver = materialResolver;
        this.entityTypeResolver = entityTypeResolver;
    }

    public TaskSpec parse(String rawTask) {
        if (rawTask == null) return TaskSpec.unknown("null");

        String s = rawTask.trim();
        if (s.isEmpty()) return TaskSpec.unknown(rawTask);

        // TaskKey 协议：TYPE:PAYLOAD
        int idx = s.indexOf(':');
        if (idx <= 0 || idx == s.length() - 1) {
            return TaskSpec.unknown(rawTask);
        }

        String typeStr = s.substring(0, idx).trim().toUpperCase();
        String payload = s.substring(idx + 1).trim();

        TaskType type;
        try {
            type = TaskType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return TaskSpec.unknown(rawTask);
        }

        switch (type) {
            case BLOCK_BREAK: {
                Material m = materialResolver.resolve(payload);
                if (m == null) return TaskSpec.unknown(rawTask);
                return TaskSpec.blockBreak(rawTask, m);
            }
            case HAVE_ITEM: {
                Material m = materialResolver.resolve(payload);
                if (m == null) return TaskSpec.unknown(rawTask);
                return TaskSpec.haveItem(rawTask, m);
            }
            case KILL_MOB: {
                EntityType e = entityTypeResolver.resolve(payload);
                if (e == null) return TaskSpec.unknown(rawTask);
                return TaskSpec.killMob(rawTask, e);
            }
            case DEATH_CAUSE: {
                EntityDamageEvent.DamageCause c = parseDamageCause(payload);
                if (c == null) return TaskSpec.unknown(rawTask);
                return TaskSpec.deathCause(rawTask, c);
            }
            case ADVANCEMENT:
                return TaskSpec.advancement(rawTask, payload);
            case CHAT_CODE:
                return TaskSpec.chatCode(rawTask, payload);
            default:
                return TaskSpec.unknown(rawTask);
        }
    }

    private EntityDamageEvent.DamageCause parseDamageCause(String s) {
        try {
            return EntityDamageEvent.DamageCause.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
