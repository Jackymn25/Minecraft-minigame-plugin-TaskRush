package me.jacky.taskMaster.text;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责加载 lang/zh_cn.yml，并提供翻译查询。
 * 只用于“显示”，不参与任务判定。
 */
public class LangManager {

    private static final String DEFAULT_LANG_PATH = "lang/zh_cn.yml";

    private final JavaPlugin plugin;

    private Map<String, String> materials = Collections.emptyMap();
    private Map<String, String> entities = Collections.emptyMap();
    private Map<String, String> deaths = Collections.emptyMap();

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** 在插件启动时调用一次；之后你也可以在 /reload 或自定义命令时调用 */
    public void load() {
        // 确保资源文件存在于插件数据目录 :)
        File out = new File(plugin.getDataFolder(), DEFAULT_LANG_PATH);
        if (!out.exists()) {
            plugin.saveResource(DEFAULT_LANG_PATH, false);
        }

        // 先尝试读取数据目录版本（用户可修改）
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(out);

        // 如果数据目录没有内容（极端情况），再读 jar 内资源兜底
        if (yml.getKeys(false).isEmpty()) {
            try (InputStreamReader reader = new InputStreamReader(
                    plugin.getResource(DEFAULT_LANG_PATH),
                    StandardCharsets.UTF_8
            )) {
                if (reader != null) {
                    yml = YamlConfiguration.loadConfiguration(reader);
                }
            } catch (Exception ignored) {
                // ignore
            }
        }

        this.materials = readSectionToMap(yml.getConfigurationSection("materials"));
        this.entities = readSectionToMap(yml.getConfigurationSection("entities"));
        this.deaths = readSectionToMap(yml.getConfigurationSection("deaths"));

        plugin.getLogger().info("Lang loaded: materials=" + materials.size()
                + ", entities=" + entities.size()
                + ", deaths=" + deaths.size());
    }

    public String material(String key) {
        return lookup(materials, key);
    }

    public String entity(String key) {
        return lookup(entities, key);
    }

    public String death(String key) {
        return lookup(deaths, key);
    }

    private String lookup(Map<String, String> map, String key) {
        if (key == null) return "UNKNOWN";
        String v = map.get(key);
        if (v == null || v.trim().isEmpty()) {
            // 没翻译就回退显示 key（你担心刷不到翻译时，就是这种情况）
            return key;
        }
        return v;
    }

    private Map<String, String> readSectionToMap(ConfigurationSection sec) {
        if (sec == null) return Collections.emptyMap();

        Map<String, String> m = new HashMap<>();
        for (String k : sec.getKeys(false)) {
            String v = sec.getString(k, "");
            m.put(k, v);
        }
        return m;
    }
}
