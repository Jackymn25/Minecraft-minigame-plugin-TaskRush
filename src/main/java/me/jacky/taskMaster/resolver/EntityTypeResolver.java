package me.jacky.taskMaster.resolver;

import org.bukkit.entity.EntityType;

public class EntityTypeResolver {

    /**
     * 只支持 EntityType 枚举名（key），例如：
     * CREEPER, ENDERMAN, ZOMBIE
     */
    public EntityType resolve(String input) {
        if (input == null) return null;

        String key = normalize(input);
        try {
            return EntityType.valueOf(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalize(String s) {
        return s.trim()
                .toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}
