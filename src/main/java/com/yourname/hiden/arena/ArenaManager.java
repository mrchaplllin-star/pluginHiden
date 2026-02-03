package com.yourname.hiden.arena;

import com.yourname.hiden.util.LocUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ArenaManager {
    private final JavaPlugin plugin;
    private final Map<String, Arena> arenas;
    private final File file;
    private FileConfiguration config;

    public ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.file = new File(plugin.getDataFolder(), "data/arenas.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("data/arenas.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        arenas.clear();
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection arenaSection = section.getConfigurationSection(key);
            if (arenaSection == null) {
                continue;
            }
            Arena arena = new Arena(key);
            arena.setWorld(arenaSection.getString("world"));
            arena.setDisplayName(arenaSection.getString("displayName", key));
            arena.setTimeWaiting(arenaSection.getInt("timeWaiting"));
            arena.setPrepTime(arenaSection.getInt("prepTime"));
            arena.setTimeGames(arenaSection.getInt("timeGames"));
            arena.setTimeSeek(arenaSection.getInt("timeSeek"));
            arena.setSeekersPercent(arenaSection.getInt("seekersPercent"));
            arena.setHidersMin(arenaSection.getInt("hidersMin"));
            arena.setSeekersMin(arenaSection.getInt("seekersMin"));
            Location waiting = LocUtil.deserialize(arenaSection.getString("waitingLoc"));
            Location hiden = LocUtil.deserialize(arenaSection.getString("hidenLoc"));
            Location speaker = LocUtil.deserialize(arenaSection.getString("speakerLoc"));
            arena.setWaitingLoc(waiting);
            arena.setHidenLoc(hiden);
            arena.setSpeakerLoc(speaker);
            arenas.put(key.toLowerCase(), arena);
        }
    }

    public void save() {
        config.set("arenas", null);
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            config.set(path + ".world", arena.getWorld());
            config.set(path + ".displayName", arena.getDisplayName());
            config.set(path + ".timeWaiting", arena.getTimeWaiting());
            config.set(path + ".prepTime", arena.getPrepTime());
            config.set(path + ".timeGames", arena.getTimeGames());
            config.set(path + ".timeSeek", arena.getTimeSeek());
            config.set(path + ".seekersPercent", arena.getSeekersPercent());
            config.set(path + ".hidersMin", arena.getHidersMin());
            config.set(path + ".seekersMin", arena.getSeekersMin());
            config.set(path + ".waitingLoc", LocUtil.serialize(arena.getWaitingLoc()));
            config.set(path + ".hidenLoc", LocUtil.serialize(arena.getHidenLoc()));
            config.set(path + ".speakerLoc", LocUtil.serialize(arena.getSpeakerLoc()));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не вдалося зберегти arenas.yml");
        }
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    public void removeArena(String name) {
        arenas.remove(name.toLowerCase());
    }

    public Arena getArena(String name) {
        if (name == null) {
            return null;
        }
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getArenas() {
        return new ArrayList<>(arenas.values());
    }
}
