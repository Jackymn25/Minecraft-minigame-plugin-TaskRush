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
                return render(lang.ui("task_block_break"), zh(spec.materialTarget));
            case HAVE_ITEM:
                return render(lang.ui("task_have_item"), zh(spec.materialTarget));
            case KILL_MOB:
                return render(lang.ui("task_kill_mob"), zh(spec.entityTarget));
            case DEATH_CAUSE:
                return render(lang.ui("task_death_cause"), zh(spec.deathCauseTarget));
            case ADVANCEMENT:
                return render(lang.ui("task_advancement"), zh(spec.advancementKeyTarget));
            case CHAT_CODE:
                return render(lang.ui("task_chat_code"), safe(spec.chatCodeTarget));
            default:
                return taskRaw;
        }
    }

    private String render(String template, String target) {
        if (template == null || template.trim().isEmpty()) {
            // 如果 ui 没写，就退回一个最简单拼接，避免 NPE
            return target == null ? lang.unknown() : target;
        }
        if (target == null) target = lang.unknown();
        return template.replace("{target}", target);
    }

    private String zh(Material m) {
        if (m == null) return lang.unknown();
        return lang.material(m.name());
    }
    private String zh(EntityType e) {
        if (e == null) return lang.unknown();
        return lang.entity(e.name());
    }
    private String zh(EntityDamageEvent.DamageCause c) {
        if (c == null) return lang.unknown();
        return lang.death(c.name());
    }
    private String zh(String key) {
        if (key == null) return lang.unknown();
        return lang.advancement(key);
    }
    private String safe(String s) {
        return s == null ? lang.unknown() : s;
    }
}
