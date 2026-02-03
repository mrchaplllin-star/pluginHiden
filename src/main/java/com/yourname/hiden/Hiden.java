package com.yourname.hiden;

import com.yourname.hiden.arena.ArenaManager;
import com.yourname.hiden.commands.HidenCommand;
import com.yourname.hiden.commands.HidenTabCompleter;
import com.yourname.hiden.game.GameManager;
import com.yourname.hiden.listeners.GameListener;
import com.yourname.hiden.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Hiden extends JavaPlugin {
    private ArenaManager arenaManager;
    private StatsManager statsManager;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Не вдалося створити папку даних.");
        }
        saveResource("data/arenas.yml", false);
        saveResource("data/players.yml", false);

        arenaManager = new ArenaManager(this);
        statsManager = new StatsManager(this);
        gameManager = new GameManager(this, arenaManager, statsManager);

        HidenCommand command = new HidenCommand(this);
        if (getCommand("hiden") != null) {
            getCommand("hiden").setExecutor(command);
            getCommand("hiden").setTabCompleter(new HidenTabCompleter(this));
        }

        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        long autosaveTicks = getConfig().getLong("autosave_ticks", 6000);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            arenaManager.save();
            statsManager.save();
        }, autosaveTicks, autosaveTicks);
    }

    @Override
    public void onDisable() {
        arenaManager.save();
        statsManager.save();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
