package me.jacky.taskMaster.text;

import me.jacky.taskMaster.task.TaskParser;
import me.jacky.taskMaster.task.TaskSpec;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * 只负责把内部 taskRaw（协议字符串）转换成给玩家看的文本。
 * 判定逻辑不依赖这里。
 */
public class TaskTextFormatter {

    private final TaskParser parser;
    private final LangManager lang;

    public TaskTextFormatter(TaskParser parser, LangManager lang) {
        this.parser = parser;
        this.lang = lang;
    }

    /** 长文本：用于聊天/任务列表/广播 */
    public String toDisplay(String taskRaw) {
        TaskSpec spec = parser.parse(taskRaw);

        switch (spec.type) {
            case BLOCK_BREAK:
                return "找到 " + zh(spec.materialTarget) + " 方块";
            case HAVE_ITEM:
                return "收集 " + zh(spec.materialTarget) + " 物品";
            case KILL_MOB:
                return "杀死一只 " + zh(spec.entityTarget);
            case DEATH_CAUSE:
                return "死于 " + zh(spec.deathCauseTarget);
            case ADVANCEMENT:
                return "成就:" + zh(spec.advancementKeyTarget);
            case CHAT_CODE:
                return "在聊天框输入: " + safe(spec.chatCodeTarget);
            default:
                return taskRaw;
        }
    }

    private String zh(Material m) {
        if (m == null) return "UNKNOWN";
        return lang.material(m.name());
    }

    private String zh(EntityType e) {
        if (e == null) return "UNKNOWN";
        return lang.entity(e.name());
    }

    private String zh(EntityDamageEvent.DamageCause c) {
        if (c == null) return "UNKNOWN";
        return lang.death(c.name());
    }

    private String zh(String key) {
        if (key == null) return "UNKNOWN";
        return lang.advancement(key);
    }

    private String safe(String s) {
        return s == null ? "UNKNOWN" : s;
    }
}
