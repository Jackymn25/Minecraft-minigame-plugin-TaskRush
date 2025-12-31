package me.jacky.taskMaster.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages team-related configuration and player-team assignments.
 */
public class TeamConfigManager {

    /** Default max players per team. */
    private static final int DEFAULT_MAX_PLAYERS = 10;

    /** Team ids used by the GUI (0-7). */
    private static final int TEAM_ID_GREEN = 0;
    private static final int TEAM_ID_YELLOW = 1;
    private static final int TEAM_ID_RED = 2;
    private static final int TEAM_ID_BLUE = 3;
    private static final int TEAM_ID_PURPLE = 4;
    private static final int TEAM_ID_CYAN = 5;
    private static final int TEAM_ID_PINK = 6;
    private static final int TEAM_ID_WHITE = 7;

    private final JavaPlugin plugin;

    private File configFile;
    private YamlConfiguration config;

    /** Team id -> team name mapping. */
    private final Map<Integer, String> teamIdToName = new HashMap<>();

    /** Team name -> color code mapping. */
    private final Map<String, String> teamNameToColor = new HashMap<>();

    /**
     * Creates a team config manager.
     *
     * @param owningPlugin the plugin instance
     */
    public TeamConfigManager(final JavaPlugin owningPlugin) {
        this.plugin = owningPlugin;
        setupMappings();
        loadConfig();
    }

    /**
     * Sets up team id and color mappings.
     */
    private void setupMappings() {
        teamIdToName.put(TEAM_ID_GREEN, "green");
        teamIdToName.put(TEAM_ID_YELLOW, "yellow");
        teamIdToName.put(TEAM_ID_RED, "red");
        teamIdToName.put(TEAM_ID_BLUE, "blue");
        teamIdToName.put(TEAM_ID_PURPLE, "purple");
        teamIdToName.put(TEAM_ID_CYAN, "cyan");
        teamIdToName.put(TEAM_ID_PINK, "pink");
        teamIdToName.put(TEAM_ID_WHITE, "white");

        teamNameToColor.put("green", "§a");
        teamNameToColor.put("yellow", "§e");
        teamNameToColor.put("red", "§c");
        teamNameToColor.put("blue", "§9");
        teamNameToColor.put("purple", "§5");
        teamNameToColor.put("cyan", "§b");
        teamNameToColor.put("pink", "§d");
        teamNameToColor.put("white", "§f");
    }

    /**
     * Loads configuration file from disk (or copies defaults if missing).
     */
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "in-game-info.yml");

        if (!configFile.exists()) {
            plugin.saveResource("in-game-info.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        initializeDefaultTeams();
    }

    /**
     * Initializes default team sections when missing.
     */
    private void initializeDefaultTeams() {
        for (String teamName : teamNameToColor.keySet()) {
            String teamPath = "teams." + teamName;

            if (!config.contains(teamPath)) {
                String displayName =
                        teamName.substring(0, 1).toUpperCase() + teamName.substring(1);

                config.set(teamPath + ".display-name", displayName);
                config.set(teamPath + ".color", teamNameToColor.get(teamName));
                config.set(teamPath + ".score", 0);
                config.set(teamPath + ".players", new ArrayList<String>());
                config.set(teamPath + ".max-players", DEFAULT_MAX_PLAYERS);
            }
        }

        saveConfig();
    }

    /**
     * Saves configuration to disk.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe(
                    "无法保存队伍配置文件: " + e.getMessage()
            );
        }
    }

    /**
     * Adds a player into a team by id.
     *
     * @param playerId the player UUID
     * @param playerName the player name
     * @param teamId the team id
     * @return true if joined successfully
     */
    public boolean joinTeam(final UUID playerId, final String playerName, final int teamId) {
        String teamName = teamIdToName.get(teamId);
        if (teamName == null) {
            return false;
        }

        removePlayerFromAllTeams(playerId);

        String playersPath = "teams." + teamName + ".players";
        List<String> players = config.getStringList(playersPath);

        String maxPath = "teams." + teamName + ".max-players";
        int maxPlayers = config.getInt(maxPath, DEFAULT_MAX_PLAYERS);
        if (players.size() >= maxPlayers) {
            return false;
        }

        players.add(playerId.toString());
        config.set(playersPath, players);

        String playerBase = "players." + playerId;
        config.set(playerBase + ".name", playerName);
        config.set(playerBase + ".team", teamName);
        config.set(playerBase + ".join-time", System.currentTimeMillis());

        saveConfig();
        return true;
    }

    /**
     * Removes a player from all teams and clears the player's section.
     *
     * @param playerId the player UUID
     */
    public void removePlayerFromAllTeams(final UUID playerId) {
        for (String teamName : teamNameToColor.keySet()) {
            String playersPath = "teams." + teamName + ".players";
            List<String> players = config.getStringList(playersPath);

            if (players.remove(playerId.toString())) {
                config.set(playersPath, players);
            }
        }

        config.set("players." + playerId, null);
    }

    /**
     * Gets the player's team name.
     *
     * @param playerId the player UUID
     * @return team name or null if not assigned
     */
    public String getPlayerTeam(final UUID playerId) {
        return config.getString("players." + playerId + ".team");
    }

    /**
     * Gets team info as a map.
     *
     * @param teamName the team name
     * @return info map
     */
    public Map<String, Object> getTeamInfo(final String teamName) {
        Map<String, Object> info = new HashMap<>();

        String base = "teams." + teamName;
        List<String> players = config.getStringList(base + ".players");
        int playerCount = players.size();
        int maxPlayers = config.getInt(base + ".max-players", DEFAULT_MAX_PLAYERS);

        info.put("display-name", config.getString(base + ".display-name"));
        info.put("color", config.getString(base + ".color"));
        info.put("score", config.getInt(base + ".score", 0));
        info.put("players", players);
        info.put("player-count", playerCount);
        info.put("max-players", maxPlayers);
        info.put("has-players", playerCount > 0);

        return info;
    }

    /**
     * Gets info for all teams.
     *
     * @return map of teamName -> info map
     */
    public Map<String, Map<String, Object>> getAllTeamsInfo() {
        Map<String, Map<String, Object>> allTeams = new HashMap<>();

        for (String teamName : teamNameToColor.keySet()) {
            allTeams.put(teamName, getTeamInfo(teamName));
        }

        return allTeams;
    }

    /**
     * Gets the colored display name by team id.
     *
     * @param teamId team id
     * @return colored display name, or "未知队伍" if invalid
     */
    public String getTeamDisplayName(final int teamId) {
        String teamName = teamIdToName.get(teamId);
        if (teamName == null) {
            return "未知队伍";
        }

        String base = "teams." + teamName;
        String displayName = config.getString(base + ".display-name");
        String color = config.getString(base + ".color");
        return color + displayName;
    }

    /**
     * Gets player count for a team.
     *
     * @param teamName team name
     * @return player count
     */
    public int getTeamPlayerCount(final String teamName) {
        return config.getStringList("teams." + teamName + ".players").size();
    }

    /**
     * Checks if a team is full.
     *
     * @param teamName team name
     * @return true if full
     */
    public boolean isTeamFull(final String teamName) {
        int current = getTeamPlayerCount(teamName);
        int max = config.getInt(
                "teams." + teamName + ".max-players",
                DEFAULT_MAX_PLAYERS
        );
        return current >= max;
    }

    /**
     * Gets team name by id.
     *
     * @param teamId team id
     * @return team name or null if invalid
     */
    public String getTeamNameById(final int teamId) {
        return teamIdToName.get(teamId);
    }

    /**
     * Gets the underlying YAML config.
     *
     * @return YAML configuration
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * Returns all online players that are currently in any team.
     *
     * @return list of online players in teams
     */
    public List<Player> getAllOnlineTeamPlayers() {
        List<Player> onlinePlayers = new ArrayList<>();

        for (String teamName : teamNameToColor.keySet()) {
            List<String> playerUUIDs =
                    config.getStringList("teams." + teamName + ".players");

            for (String uuidStr : playerUUIDs) {
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    Player player = Bukkit.getPlayer(playerUUID);

                    if (player != null && player.isOnline()) {
                        onlinePlayers.add(player);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                }
            }
        }

        return onlinePlayers;
    }

    /**
     * Returns a mapping of team name to its online players.
     *
     * @return map of team -> online players list
     */
    public Map<String, List<Player>> getTeamOnlinePlayersMap() {
        Map<String, List<Player>> teamPlayersMap = new HashMap<>();

        for (String teamName : teamNameToColor.keySet()) {
            List<Player> teamPlayers = new ArrayList<>();
            List<String> playerUUIDs =
                    config.getStringList("teams." + teamName + ".players");

            for (String uuidStr : playerUUIDs) {
                try {
                    UUID playerUUID = UUID.fromString(uuidStr);
                    Player player = Bukkit.getPlayer(playerUUID);

                    if (player != null && player.isOnline()) {
                        teamPlayers.add(player);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                }
            }

            teamPlayersMap.put(teamName, teamPlayers);
        }

        return teamPlayersMap;
    }

    /**
     * Returns online players for a specific team.
     *
     * @param teamName team name
     * @return list of online players
     */
    public List<Player> getOnlinePlayersByTeam(final String teamName) {
        List<Player> teamPlayers = new ArrayList<>();
        List<String> playerUUIDs =
                config.getStringList("teams." + teamName + ".players");

        for (String uuidStr : playerUUIDs) {
            try {
                UUID playerUUID = UUID.fromString(uuidStr);
                Player player = Bukkit.getPlayer(playerUUID);

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
