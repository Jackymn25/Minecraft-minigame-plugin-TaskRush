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
    private Map<String, String> ui = Collections.emptyMap();

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        // 0) 先清空，避免加载失败还沿用旧语言
        this.materials = Collections.emptyMap();
        this.entities = Collections.emptyMap();
        this.deaths = Collections.emptyMap();
        this.advancements = Collections.emptyMap();
        this.ui = Collections.emptyMap();

        String langName = plugin.getConfig().getString(CFG_LANGUAGE, DEFAULT_LANGUAGE);
        if (langName == null || langName.trim().isEmpty()) langName = DEFAULT_LANGUAGE;
        langName = langName.trim().toLowerCase();

        if (!tryLoad(langName)) {
            plugin.getLogger().warning("Failed to load language: " + langName + ", fallback to " + DEFAULT_LANGUAGE);
            tryLoad(DEFAULT_LANGUAGE.toLowerCase());
        }
    }

    private boolean tryLoad(String langName) {
        String fileName = langName + ".yml";
        String relPath = LANG_DIR + "/" + fileName; // lang/xx.yml
        File out = new File(plugin.getDataFolder(), relPath);

        plugin.getLogger().info("[Lang] tryLoad=" + langName + " path=" + out.getAbsolutePath());

        // 1) 如果数据目录没有，且 jar 里有资源，就复制出来
        if (!out.exists()) {
            if (plugin.getResource(relPath) != null) {
                plugin.saveResource(relPath, false);
            }
        }

        // 2) 仍然不存在：直接失败（说明文件名/语言名对不上）
        if (!out.exists()) {
            plugin.getLogger().warning("[Lang] file not found: " + relPath);
            return false;
        }

        // 3) 读 dataFolder 文件
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(out);

        // 4) 如果读出来是空的，再尝试读 jar 内资源（UTF-8）
        if (yml.getKeys(false).isEmpty() && plugin.getResource(relPath) != null) {
            try (InputStreamReader reader = new InputStreamReader(plugin.getResource(relPath), StandardCharsets.UTF_8)) {
                yml = YamlConfiguration.loadConfiguration(reader);
            } catch (Exception e) {
                plugin.getLogger().warning("[Lang] failed to read jar resource: " + relPath + " err=" + e.getMessage());
            }
        }

        // 5) 真正写入 map
        this.materials = readSectionToMap(yml.getConfigurationSection("materials"));
        this.entities = readSectionToMap(yml.getConfigurationSection("entities"));
        this.deaths = readSectionToMap(yml.getConfigurationSection("deaths"));
        this.advancements = readSectionToMap(yml.getConfigurationSection("advancements"));
        this.ui = readSectionToMap(yml.getConfigurationSection("ui"));

        boolean ok = !ui.isEmpty(); // 至少 ui 要有，不然显示模板都没了
        plugin.getLogger().info("[Lang] loaded=" + langName + " ok=" + ok
                + " materials=" + materials.size()
                + " entities=" + entities.size()
                + " deaths=" + deaths.size()
                + " advancements=" + advancements.size()
                + " ui=" + ui.size());

        return ok;
    }

    public String material(String key) { return lookup(materials, key); }
    public String entity(String key) { return lookup(entities, key); }
    public String death(String key) { return lookup(deaths, key); }
    public String advancement(String key) { return lookup(advancements, key); }
    public String ui(String key) { return lookup(ui, key); }
    public String unknown() { return ui("unknown"); }

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
