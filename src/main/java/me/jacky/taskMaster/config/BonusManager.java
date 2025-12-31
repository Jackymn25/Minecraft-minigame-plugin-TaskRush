package me.jacky.taskMaster.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class BonusManager {

    private final JavaPlugin plugin;

    private final Map<Material, Integer> blockBonus = new HashMap<>();
    private final Map<Material, Integer> itemBonus = new HashMap<>();
    private final Map<EntityType, Integer> mobBonus = new HashMap<>();
    private final Map<EntityDamageEvent.DamageCause, Integer> deathBonus = new HashMap<>();
    private final Map<String, Integer> advancementBonus = new HashMap<>();

    private final List<Material> blockPool = new ArrayList<>();
    private final List<Material> itemPool = new ArrayList<>();
    private final List<EntityType> mobPool = new ArrayList<>();
    private final List<EntityDamageEvent.DamageCause> deathPool = new ArrayList<>();
    private final List<String> advancementPool = new ArrayList<>();

    public BonusManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        blockBonus.clear();
        itemBonus.clear();
        mobBonus.clear();
        deathBonus.clear();
        advancementBonus.clear();

        blockPool.clear();
        itemPool.clear();
        mobPool.clear();
        deathPool.clear();
        advancementPool.clear();

        File dir = new File(plugin.getDataFolder(), "bonuses");
        if (!dir.exists()) dir.mkdirs();

        loadMaterialMapAndPool(new File(dir, "blocks.yml"), blockBonus, blockPool);
        loadMaterialMapAndPool(new File(dir, "items.yml"), itemBonus, itemPool);
        loadEntityTypeMapAndPool(new File(dir, "mobs.yml"), mobBonus, mobPool);
        loadDamageCauseMapAndPool(new File(dir, "deaths.yml"), deathBonus, deathPool);
        loadStringIntMapAndPool(new File(dir, "advancements.yml"), advancementBonus, advancementPool);

        plugin.getLogger().info("[TaskMaster] Bonus configs + pools reloaded.");
    }

    private void loadMaterialMapAndPool(File file, Map<Material, Integer> out, List<Material> pool) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        for (String key : yml.getKeys(false)) {
            Material m = Material.getMaterial(key.toUpperCase());
            if (m == null) {
                plugin.getLogger().warning("[TaskMaster] Unknown Material in " + file.getName() + ": " + key);
                continue;
            }
            out.put(m, yml.getInt(key, 0));
            pool.add(m);
        }
    }

    private void loadEntityTypeMapAndPool(File file, Map<EntityType, Integer> out, List<EntityType> pool) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        for (String key : yml.getKeys(false)) {
            try {
                EntityType t = EntityType.valueOf(key.toUpperCase());
                out.put(t, yml.getInt(key, 0));
                pool.add(t);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[TaskMaster] Unknown EntityType in " + file.getName() + ": " + key);
            }
        }
    }

    private void loadDamageCauseMapAndPool(File file, Map<EntityDamageEvent.DamageCause, Integer> out, List<EntityDamageEvent.DamageCause> pool) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        for (String key : yml.getKeys(false)) {
            try {
                EntityDamageEvent.DamageCause c = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
                out.put(c, yml.getInt(key, 0));
                pool.add(c);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[TaskMaster] Unknown DamageCause in " + file.getName() + ": " + key);
            }
        }
    }

    private void loadStringIntMapAndPool(File file, Map<String, Integer> out, List<String> pool) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        for (String key : yml.getKeys(false)) {
            String k = key.trim();
            out.put(k, yml.getInt(key, 0));
            pool.add(k);
        }
    }

    public int getBlockBonus(Material m) {
        return blockBonus.getOrDefault(m, 0);
    }

    public int getItemBonus(Material m) {
        return itemBonus.getOrDefault(m, 0);
    }

    public int getMobBonus(EntityType t) {
        return mobBonus.getOrDefault(t, 0);
    }

    public int getDeathBonus(EntityDamageEvent.DamageCause c) {
        return deathBonus.getOrDefault(c, 0);
    }

    public int getAdvancementBonus(String key) {
        return advancementBonus.getOrDefault(key, 0);
    }

    public List<Material> getBlockPool() { return Collections.unmodifiableList(blockPool); }
    public List<Material> getItemPool() { return Collections.unmodifiableList(itemPool); }
    public List<EntityType> getMobPool() { return Collections.unmodifiableList(mobPool); }
    public List<EntityDamageEvent.DamageCause> getDeathPool() { return Collections.unmodifiableList(deathPool); }
    public List<String> getAdvancementPool() { return Collections.unmodifiableList(advancementPool); }
}
