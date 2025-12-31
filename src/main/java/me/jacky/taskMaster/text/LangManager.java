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
 * 负责加载 lang/*.yml，并提供翻译查询。
 * 只用于“显示”，不参与任务判定。
 */
public class LangManager {

    private static final String CFG_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "zh_cn"; // 默认语言名
    private static final String LANG_DIR = "lang";

    private final JavaPlugin plugin;

    private Map<String, String> materials = Collections.emptyMap();
    private Map<String, String> entities = Collections.emptyMap();
    private Map<String, String> deaths = Collections.emptyMap();
    private Map<String, String> advancements = Collections.emptyMap();

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String langName = plugin.getConfig().getString(CFG_LANGUAGE, DEFAULT_LANGUAGE);
        if (langName == null || langName.trim().isEmpty()) {
            langName = DEFAULT_LANGUAGE;
        }

        String fileName = langName.trim().toLowerCase() + ".yml";
        String relPath = LANG_DIR + "/" + fileName; // e.g. lang/ja_jp.yml

        // 1) 确保用户数据目录存在该语言文件（若 jar 内有同名资源则复制）
        File out = new File(plugin.getDataFolder(), relPath);
        if (!out.exists()) {
            // 只有当 jar 里真的有这个资源时才复制，否则会抛异常
            if (plugin.getResource(relPath) != null) {
                plugin.saveResource(relPath, false);
            } else {
                // 如果 jar 内没有，就回退到默认语言
                plugin.getLogger().warning("Language file not found in jar: " + relPath + ", fallback to " + DEFAULT_LANGUAGE);
                langName = DEFAULT_LANGUAGE;
                fileName = langName + ".yml";
                relPath = LANG_DIR + "/" + fileName;
                out = new File(plugin.getDataFolder(), relPath);
                if (!out.exists() && plugin.getResource(relPath) != null) {
                    plugin.saveResource(relPath, false);
                }
            }
        }

        // 2) 读取数据目录版本
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(out);

        // 3) 如果数据目录版本空，再读 jar 内资源 :)
        if (yml.getKeys(false).isEmpty() && plugin.getResource(relPath) != null) {
            try (InputStreamReader reader = new InputStreamReader(
                    plugin.getResource(relPath),
                    StandardCharsets.UTF_8
            )) {
                yml = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception ignored) {}
        }

        this.materials = readSectionToMap(yml.getConfigurationSection("materials"));
        this.entities = readSectionToMap(yml.getConfigurationSection("entities"));
        this.deaths = readSectionToMap(yml.getConfigurationSection("deaths"));
        this.advancements = readSectionToMap(yml.getConfigurationSection("advancements"));

        plugin.getLogger().info("Lang loaded (" + langName + "): materials=" + materials.size()
                + ", entities=" + entities.size()
                + ", deaths=" + deaths.size()
                + ", advancements=" + advancements.size());
    }

    public String material(String key) { return lookup(materials, key); }
    public String entity(String key) { return lookup(entities, key); }
    public String death(String key) { return lookup(deaths, key); }
    public String advancement(String key) { return lookup(advancements, key); }

    private String lookup(Map<String, String> map, String key) {
        if (key == null) return "UNKNOWN";
        String v = map.get(key);
        if (v == null || v.trim().isEmpty()) return key; // 没翻译就回退显示 key
        return v;
    }

    private Map<String, String> readSectionToMap(ConfigurationSection sec) {
        if (sec == null) return Collections.emptyMap();
        Map<String, String> m = new HashMap<>();
        for (String k : sec.getKeys(false)) {
            m.put(k, sec.getString(k, ""));
        }
        return m;
    }
}
