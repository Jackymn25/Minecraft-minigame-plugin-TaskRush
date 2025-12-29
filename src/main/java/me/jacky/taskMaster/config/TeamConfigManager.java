package me.jacky.taskMaster.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamConfigManager {

    private final JavaPlugin plugin;  // 插件主类实例
    private File configFile;          // 配置文件对象
    private YamlConfiguration config; // YAML配置对象

    // 队伍编号到名称的映射，用于将点击事件中的数字索引转换为队伍名称
    private final Map<Integer, String> teamIdToName = new HashMap<>();
    // 队伍名称到颜色代码的映射，用于显示彩色文本
    private final Map<String, String> teamNameToColor = new HashMap<>();

    public TeamConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;  // 传入插件主类实例
        setupMappings();       // 初始化映射表
        loadConfig();          // 加载配置文件
    }

    /**
     * 设置队伍编号和颜色的映射关系
     * 这个方法初始化两个HashMap，将数字ID、队伍名称和颜色代码关联起来
     */
    private void setupMappings() {
        // 设置队伍编号到名称的映射
        // 对应GUI菜单中物品的点击事件：0=绿色羊毛，1=黄色羊毛等
        teamIdToName.put(0, "green");    // 索引0对应绿队
        teamIdToName.put(1, "yellow");   // 索引1对应黄队
        teamIdToName.put(2, "red");      // 索引2对应红队
        teamIdToName.put(3, "blue");     // 索引3对应蓝队
        teamIdToName.put(4, "purple");   // 索引4对应紫队
        teamIdToName.put(5, "cyan");     // 索引5对应青队
        teamIdToName.put(6, "pink");     // 索引6对应粉队
        teamIdToName.put(7, "white");    // 索引7对应白队

        // 设置队伍名称到颜色代码的映射
        // 使用Minecraft颜色代码格式：§+颜色代码
        teamNameToColor.put("green", "§a");      // 绿色
        teamNameToColor.put("yellow", "§e");     // 黄色
        teamNameToColor.put("red", "§c");        // 红色
        teamNameToColor.put("blue", "§9");       // 蓝色
        teamNameToColor.put("purple", "§5");     // 紫色
        teamNameToColor.put("cyan", "§b");       // 青色
        teamNameToColor.put("pink", "§d");       // 粉色
        teamNameToColor.put("white", "§f");      // 白色
    }

    /**
     * 加载配置文件
     * 如果配置文件不存在，则从插件jar中复制默认配置
     */
    private void loadConfig() {
        // 创建配置文件对象，路径为插件数据文件夹下的in-game-info.yml
        configFile = new File(plugin.getDataFolder(), "in-game-info.yml");

        // 如果配置文件不存在，从插件jar中复制默认配置
        if (!configFile.exists()) {
            plugin.saveResource("in-game-info.yml", false);  // 第二个参数false表示不覆盖已存在的文件
        }

        // 加载YAML配置
        config = YamlConfiguration.loadConfiguration(configFile);
        // 初始化默认队伍配置
        initializeDefaultTeams();
    }

    /**
     * 初始化默认队伍配置
     * 检查每个队伍是否已配置，如果未配置则设置默认值
     */
    private void initializeDefaultTeams() {
        // 遍历所有队伍名称
        for (String teamName : teamNameToColor.keySet()) {
            // 检查配置中是否已存在该队伍
            if (!config.contains("teams." + teamName)) {
                // 设置队伍显示名称（首字母大写）
                config.set("teams." + teamName + ".display-name",
                        teamName.substring(0, 1).toUpperCase() + teamName.substring(1));
                // 设置队伍颜色
                config.set("teams." + teamName + ".color", teamNameToColor.get(teamName));
                // 初始化队伍分数为0
                config.set("teams." + teamName + ".score", 0);
                // 初始化玩家列表为空列表
                config.set("teams." + teamName + ".players", new ArrayList<String>());
                // 设置队伍最大人数为10人
                config.set("teams." + teamName + ".max-players", 10);
            }
        }
        // 保存配置到文件
        saveConfig();
    }

    /**
     * 保存配置文件到磁盘
     */
    public void saveConfig() {
        try {
            config.save(configFile);  // 保存配置
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存队伍配置文件: " + e.getMessage());
        }
    }

    /**
     * 将玩家加入队伍
     * @param playerId 玩家的UUID
     * @param playerName 玩家的名称
     * @param teamId 队伍编号（0-7）
     * @return 是否成功加入
     */
    public boolean joinTeam(UUID playerId, String playerName, int teamId) {
        // 通过队伍编号获取队伍名称
        String teamName = teamIdToName.get(teamId);
        if (teamName == null) {
            return false;  // 队伍编号无效
        }

        // 先从所有队伍中移除该玩家（避免重复加入多个队伍）
        removePlayerFromAllTeams(playerId);

        // 获取队伍当前的玩家列表
        List<String> players = config.getStringList("teams." + teamName + ".players");

        // 检查队伍是否已满
        int maxPlayers = config.getInt("teams." + teamName + ".max-players", 10);
        if (players.size() >= maxPlayers) {
            return false;  // 队伍已满
        }

        // 添加玩家UUID到队伍列表
        players.add(playerId.toString());
        config.set("teams." + teamName + ".players", players);

        // 更新玩家个人信息
        config.set("players." + playerId + ".name", playerName);
        config.set("players." + playerId + ".team", teamName);
        config.set("players." + playerId + ".join-time", System.currentTimeMillis());

        // 保存配置
        saveConfig();
        return true;
    }

    /**
     * 从所有队伍中移除玩家
     * @param playerId 玩家的UUID
     */
    public void removePlayerFromAllTeams(UUID playerId) {
        // 遍历所有队伍
        for (String teamName : teamNameToColor.keySet()) {
            // 获取队伍玩家列表
            List<String> players = config.getStringList("teams." + teamName + ".players");
            // 如果玩家在这个队伍中，移除他
            if (players.remove(playerId.toString())) {
                config.set("teams." + teamName + ".players", players);
            }
        }
        // 删除玩家的个人信息
        config.set("players." + playerId, null);
    }

    /**
     * 获取玩家所在的队伍名称
     * @param playerId 玩家的UUID
     * @return 队伍名称，如果玩家不在任何队伍中则返回null
     */
    public String getPlayerTeam(UUID playerId) {
        return config.getString("players." + playerId + ".team");
    }

    /**
     * 获取队伍信息
     * @param teamName 队伍名称
     * @return 包含队伍所有信息的Map
     */
    public Map<String, Object> getTeamInfo(String teamName) {
        Map<String, Object> info = new HashMap<>();
        // 队伍显示名称
        info.put("display-name", config.getString("teams." + teamName + ".display-name"));
        // 队伍颜色代码
        info.put("color", config.getString("teams." + teamName + ".color"));
        // 队伍当前分数
        info.put("score", config.getInt("teams." + teamName + ".score", 0));
        // 队伍中的玩家UUID列表（字符串格式）
        info.put("players", config.getStringList("teams." + teamName + ".players"));
        // 队伍当前人数
        info.put("player-count", config.getStringList("teams." + teamName + ".players").size());
        // 队伍最大人数
        info.put("max-players", config.getInt("teams." + teamName + ".max-players", 10));
        // 队伍是否有人
        info.put("has-players", config.getStringList("teams." + teamName + ".players").size() > 0);
        return info;
    }

    /**
     * 获取所有队伍信息
     * @return Map<队伍名称, 队伍信息Map>
     */
    public Map<String, Map<String, Object>> getAllTeamsInfo() {
        Map<String, Map<String, Object>> allTeams = new HashMap<>();
        for (String teamName : teamNameToColor.keySet()) {
            allTeams.put(teamName, getTeamInfo(teamName));
        }
        return allTeams;
    }

    /**
     * 增加队伍分数
     * @param teamName 队伍名称
     * @param points 要增加的分数
     */
    public void addTeamScore(String teamName, int points) {
        int currentScore = config.getInt("teams." + teamName + ".score", 0);
        config.set("teams." + teamName + ".score", currentScore + points);
        saveConfig();
    }

    /**
     * 设置队伍分数
     * @param teamName 队伍名称
     * @param score 新的分数
     */
    public void setTeamScore(String teamName, int score) {
        config.set("teams." + teamName + ".score", score);
        saveConfig();
    }

    /**
     * 根据队伍编号获取队伍显示名称（带颜色）
     * @param teamId 队伍编号
     * @return 带颜色的队伍名称
     */
    public String getTeamDisplayName(int teamId) {
        String teamName = teamIdToName.get(teamId);
        if (teamName == null) return "未知队伍";

        String displayName = config.getString("teams." + teamName + ".display-name");
        String color = config.getString("teams." + teamName + ".color");
        return color + displayName;
    }

    /**
     * 获取队伍人数
     * @param teamName 队伍名称
     * @return 队伍当前人数
     */
    public int getTeamPlayerCount(String teamName) {
        return config.getStringList("teams." + teamName + ".players").size();
    }

    /**
     * 检查队伍是否有人
     * @param teamName 队伍名称
     * @return 队伍是否至少有一名玩家
     */
    public boolean hasPlayers(String teamName) {
        return getTeamPlayerCount(teamName) > 0;
    }

    /**
     * 检查队伍是否已满
     * @param teamName 队伍名称
     * @return 队伍是否已满
     */
    public boolean isTeamFull(String teamName) {
        int current = getTeamPlayerCount(teamName);
        int max = config.getInt("teams." + teamName + ".max-players", 10);
        return current >= max;
    }

    /**
     * 根据队伍编号获取队伍名称
     * @param teamId 队伍编号
     * @return 队伍名称
     */
    public String getTeamNameById(int teamId) {
        return teamIdToName.get(teamId);
    }

    /**
     * 获取配置实例（用于直接操作）
     * @return YAML配置对象
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * 方法一：获取所有队伍中所有在线的玩家实例
     * 这个方法遍历所有队伍，获取所有玩家的UUID，然后转换为在线Player对象
     *
     * 注意：这个方法只返回在线玩家，离线玩家不会被包含在内
     *
     * @return List<Player> 包含所有在线队伍玩家的列表
     */
    public List<Player> getAllOnlineTeamPlayers() {
        List<Player> onlinePlayers = new ArrayList<>();

        // 遍历所有队伍
        for (String teamName : teamNameToColor.keySet()) {
            // 获取队伍的玩家UUID列表
            List<String> playerUUIDs = config.getStringList("teams." + teamName + ".players");

            // 遍历每个UUID，尝试获取在线玩家
            for (String uuidStr : playerUUIDs) {
                try {
                    // 将字符串转换为UUID对象
                    UUID playerUUID = UUID.fromString(uuidStr);
                    // 通过UUID获取在线玩家
                    Player player = Bukkit.getPlayer(playerUUID);

                    // 如果玩家在线且不为null，添加到列表
                    if (player != null && player.isOnline()) {
                        onlinePlayers.add(player);
                    }
                } catch (IllegalArgumentException e) {
                    // UUID格式无效，跳过
                    plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                }
            }
        }

        return onlinePlayers;
    }

    /**
     * 方法二：返回一个字典，键为队伍名称，值为该队伍中所有在线的玩家列表
     * 这个方法按队伍组织玩家，便于按队伍进行操作
     *
     * @return Map<String, List<Player>> 队伍名称到在线玩家列表的映射
     */
    public Map<String, List<Player>> getTeamOnlinePlayersMap() {
        Map<String, List<Player>> teamPlayersMap = new HashMap<>();

        // 遍历所有队伍
        for (String teamName : teamNameToColor.keySet()) {
            // 为每个队伍创建一个空的玩家列表
            List<Player> teamPlayers = new ArrayList<>();

            // 获取队伍的玩家UUID列表
            List<String> playerUUIDs = config.getStringList("teams." + teamName + ".players");

            // 遍历每个UUID，尝试获取在线玩家
            for (String uuidStr : playerUUIDs) {
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    Player player = Bukkit.getPlayer(playerUUID);

                    // 如果玩家在线且不为null，添加到队伍列表
                    if (player != null && player.isOnline()) {
                        teamPlayers.add(player);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                }
            }

            // 将队伍和对应的玩家列表放入映射
            teamPlayersMap.put(teamName, teamPlayers);
        }

        return teamPlayersMap;
    }

    /**
     * 获取所有加入队伍的玩家（包括离线和在线）
     * 这个方法返回所有在配置中记录的玩家的UUID
     *
     * @return List<UUID> 包含所有队伍玩家的UUID列表
     */
    public List<UUID> getAllTeamPlayersUUIDs() {
        List<UUID> allUUIDs = new ArrayList<>();

        // 遍历所有队伍
        for (String teamName : teamNameToColor.keySet()) {
            // 获取队伍的玩家UUID字符串列表
            List<String> playerUUIDs = config.getStringList("teams." + teamName + ".players");

            // 将字符串转换为UUID对象
            for (String uuidStr : playerUUIDs) {
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    allUUIDs.add(playerUUID);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                }
            }
        }

        return allUUIDs;
    }

    /**
     * 获取特定队伍的在线玩家列表
     *
     * @param teamName 队伍名称
     * @return 该队伍的在线玩家列表
     */
    public List<Player> getOnlinePlayersByTeam(String teamName) {
        List<Player> teamPlayers = new ArrayList<>();

        // 获取队伍的玩家UUID列表
        List<String> playerUUIDs = config.getStringList("teams." + teamName + ".players");

        // 遍历每个UUID，尝试获取在线玩家
        for (String uuidStr : playerUUIDs) {
            try {
                UUID playerUUID = UUID.fromString(uuidStr);
                Player player = Bukkit.getPlayer(playerUUID);

                // 如果玩家在线且不为null，添加到列表
                if (player != null && player.isOnline()) {
                    teamPlayers.add(player);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
            }
        }

        return teamPlayers;
    }
}
