package com.yourname.hiden.stats;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, PlayerStats> stats;

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data/players.yml");
        this.stats = new HashMap<>();
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("data/players.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        stats.clear();
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            ConfigurationSection playerSection = section.getConfigurationSection(key);
            if (playerSection == null) {
                continue;
            }
            PlayerStats playerStats = new PlayerStats(uuid);
            playerStats.setHiderWins(playerSection.getInt("hiderWins"));
            playerStats.setSeekerWins(playerSection.getInt("seekerWins"));
            playerStats.setHunterKills(playerSection.getInt("hunterKills"));
            playerStats.setSurvivorGames(playerSection.getInt("survivorGames"));
            playerStats.setFirstBlood(playerSection.getBoolean("firstBlood"));
            playerStats.setSurvivor(playerSection.getBoolean("survivor"));
            playerStats.setHunter(playerSection.getBoolean("hunter"));
            playerStats.setSpeedrunner(playerSection.getBoolean("speedrunner"));
            playerStats.setGhost(playerSection.getBoolean("ghost"));
            stats.put(uuid, playerStats);
        }
    }

    public void save() {
        config.set("players", null);
        for (PlayerStats playerStats : stats.values()) {
            String path = "players." + playerStats.getUuid();
            config.set(path + ".hiderWins", playerStats.getHiderWins());
            config.set(path + ".seekerWins", playerStats.getSeekerWins());
            config.set(path + ".hunterKills", playerStats.getHunterKills());
            config.set(path + ".survivorGames", playerStats.getSurvivorGames());
            config.set(path + ".firstBlood", playerStats.isFirstBlood());
            config.set(path + ".survivor", playerStats.isSurvivor());
            config.set(path + ".hunter", playerStats.isHunter());
            config.set(path + ".speedrunner", playerStats.isSpeedrunner());
            config.set(path + ".ghost", playerStats.isGhost());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не вдалося зберегти players.yml");
        }
    }

    public PlayerStats getStats(UUID uuid) {
        return stats.computeIfAbsent(uuid, PlayerStats::new);
    }

    public Map<UUID, PlayerStats> getAllStats() {
        return new HashMap<>(stats);
    }
}
