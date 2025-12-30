package me.jacky.taskMaster.resolver;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class MaterialResolver {

    private final JavaPlugin plugin;

    public MaterialResolver(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 只支持 Material 枚举名（key），例如：
     * DIAMOND_ORE, ENDER_PEARL, NETHERITE_INGOT
     *
     * 允许一些宽松输入：大小写不敏感、空格/短横线自动转下划线。
     */
    public Material resolve(String input) {
        if (input == null) return null;

        String key = normalize(input);
        Material m = Material.getMaterial(key);

        if (m == null) {
            plugin.getLogger().warning("[MaterialResolver] Unknown material key: " + input + " -> " + key);
        }

        return m;
    }

    private String normalize(String s) {
        return s.trim()
                .toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}
