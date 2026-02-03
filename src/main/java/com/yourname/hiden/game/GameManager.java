package com.yourname.hiden.game;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.ArenaManager;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.stats.PlayerStats;
import com.yourname.hiden.stats.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import com.yourname.hiden.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameManager {
    private final Hiden plugin;
    private final ArenaManager arenaManager;
    private final StatsManager statsManager;
    private final Map<String, GameTask> tasks;
    private final Map<String, BukkitTask> taskHandles;
    private final Map<String, Team> hidenTeams;
    private final Map<String, Team> speakerTeams;
    private String lastError;

    public GameManager(Hiden plugin, ArenaManager arenaManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.statsManager = statsManager;
        this.tasks = new HashMap<>();
        this.taskHandles = new HashMap<>();
        this.hidenTeams = new HashMap<>();
        this.speakerTeams = new HashMap<>();
    }

    public Arena getArenaByPlayer(UUID uuid) {
        for (Arena arena : arenaManager.getArenas()) {
            if (arena.getParticipants().contains(uuid)) {
                return arena;
            }
        }
        return null;
    }

    public boolean startGame(Arena arena) {
        lastError = null;
        if (arena == null) {
            lastError = "&cАрена не знайдена.";
            return false;
        }
        int requiredHiders = arena.getHidersMin() > 0 ? arena.getHidersMin() : plugin.getConfig().getInt("hiders_min");
        int requiredSeekers = arena.getSeekersMin() > 0 ? arena.getSeekersMin() : plugin.getConfig().getInt("seekers_min");
        if (arena.getState() == GameState.PREP || arena.getState() == GameState.PLAYING) {
            lastError = "&cГра вже запущена.";
            return false;
        }
        if (arena.getWaitingLoc() == null || arena.getHidenLoc() == null || arena.getSpeakerLoc() == null) {
            lastError = "&cНе всі варпи налаштовані!";
            return false;
        }
        int minPlayers = arena.getMinPlayers() > 0 ? arena.getMinPlayers() : plugin.getConfig().getInt("default_min_players", 2);
        minPlayers = Math.max(2, Math.min(10, minPlayers));
        if (arena.getParticipants().size() < minPlayers) {
            lastError = "&cНедостатньо гравців для старту гри!\n&7Мінімум: &f" + minPlayers
                    + "\n&7Зараз: &f" + arena.getParticipants().size();
            return false;
        }
        if (arena.getParticipants().size() < requiredHiders) {
            lastError = "&cНедостатньо гравців для старту.";
            return false;
        }
        List<UUID> shuffled = new ArrayList<>(arena.getParticipants());
        java.util.Collections.shuffle(shuffled);
        int seekersCount = Math.max(requiredSeekers,
                (int) Math.ceil(shuffled.size() * (arena.getSeekersPercent() / 100.0)));
        seekersCount = Math.min(seekersCount, shuffled.size() - 1);
        boolean manualTeams = !arena.getSeekers().isEmpty() || !arena.getHiders().isEmpty();
        Set<UUID> presetSeekers = new java.util.HashSet<>(arena.getSeekers());
        Set<UUID> presetHiders = new java.util.HashSet<>(arena.getHiders());
        presetSeekers.retainAll(arena.getParticipants());
        presetHiders.retainAll(arena.getParticipants());
        presetHiders.removeAll(presetSeekers);

        arena.getSeekers().clear();
        arena.getHiders().clear();
        arena.getSeekers().addAll(presetSeekers);
        arena.getHiders().addAll(presetHiders);

        List<UUID> unassigned = new ArrayList<>(shuffled);
        unassigned.removeAll(arena.getSeekers());
        unassigned.removeAll(arena.getHiders());

        if (manualTeams) {
            if (arena.getHiders().size() < requiredHiders) {
                lastError = "&cНедостатньо гравців Hiden для старту.";
                return false;
            }
            if (arena.getSeekers().size() < requiredSeekers) {
                lastError = "&cНедостатньо гравців Seek для старту.";
                return false;
            }
            arena.getHiders().addAll(unassigned);
        } else {
            while (arena.getSeekers().size() < seekersCount && !unassigned.isEmpty()) {
                arena.getSeekers().add(unassigned.remove(0));
            }
            arena.getHiders().addAll(unassigned);
        }
        syncTeams(arena);
        for (UUID uuid : arena.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(arena.getWaitingLoc());
            }
        }
        GameTask task = new GameTask(plugin, this, arena);
        BukkitTask handle = task.runTaskTimer(plugin, 0L, 20L);
        tasks.put(arena.getName().toLowerCase(), task);
        taskHandles.put(arena.getName().toLowerCase(), handle);
        return true;
    }

    public String getLastError() {
        return lastError;
    }

    public void stopGame(Arena arena) {
        if (arena == null) {
            return;
        }
        String key = arena.getName().toLowerCase();
        BukkitTask handle = taskHandles.remove(key);
        if (handle != null) {
            handle.cancel();
        }
        tasks.remove(key);
    }

    public GameTask getTask(Arena arena) {
        if (arena == null) {
            return null;
        }
        return tasks.get(arena.getName().toLowerCase());
    }

    public void handleKill(Player seeker, Player hider, Arena arena) {
        if (seeker == null || hider == null || arena == null) {
            return;
        }
        GameTask task = getTask(arena);
        if (task != null && !task.isFirstKill()) {
            task.setFirstKill(true);
            PlayerStats stats = statsManager.getStats(seeker.getUniqueId());
            stats.setFirstBlood(true);
            Msg.send(seeker, "&aДосягнення &eFirstBlood&a отримано!");
        }
        PlayerStats seekerStats = statsManager.getStats(seeker.getUniqueId());
        seekerStats.setHunterKills(seekerStats.getHunterKills() + 1);
        if (seekerStats.getHunterKills() >= 50) {
            seekerStats.setHunter(true);
        }
        arena.getHiders().remove(hider.getUniqueId());
        hider.setGameMode(GameMode.SPECTATOR);
        hider.teleport(arena.getWaitingLoc());
        hider.playSound(hider.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        hider.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION, hider.getLocation(), 10, 0.2, 0.2, 0.2, 0.01);
        checkWin(arena);
    }

    public void handleHiderDeath(Player hider, Arena arena) {
        if (hider == null || arena == null) {
            return;
        }
        arena.getHiders().remove(hider.getUniqueId());
        hider.setGameMode(GameMode.SPECTATOR);
        hider.teleport(arena.getWaitingLoc());
        checkWin(arena);
    }

    public void checkWin(Arena arena) {
        if (arena == null) {
            return;
        }
        if (arena.getState() != GameState.PLAYING) {
            return;
        }
        if (arena.getHiders().isEmpty()) {
            endGame(arena, true);
        }
    }

    public void endGame(Arena arena, boolean seekersWin) {
        if (arena == null) {
            return;
        }
        GameTask task = getTask(arena);
        long elapsedTicks = task == null ? 0 : task.getElapsedPlayingTicks();
        stopGame(arena);
        arena.setState(GameState.ENDED);
        arena.clearBossBar();
        Location waiting = arena.getWaitingLoc();

        String title = seekersWin ? "&cПеремогли Seek!" : "&aПеремогли Hiden!";
        broadcastToArena(arena, title + " &7(" + arena.getDisplayName() + ")");

        if (waiting != null) {
            for (int i = 0; i < 3; i++) {
                Firework firework = waiting.getWorld().spawn(waiting, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(1);
                firework.setFireworkMeta(meta);
            }
        }
        for (UUID uuid : arena.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }

        if (seekersWin) {
            for (UUID uuid : arena.getSeekers()) {
                PlayerStats stats = statsManager.getStats(uuid);
                stats.setSeekerWins(stats.getSeekerWins() + 1);
                if (elapsedTicks > 0 && elapsedTicks <= 1800) {
                    stats.setSpeedrunner(true);
                }
            }
        } else {
            for (UUID uuid : arena.getHiders()) {
                PlayerStats stats = statsManager.getStats(uuid);
                stats.setHiderWins(stats.getHiderWins() + 1);
                stats.setSurvivorGames(stats.getSurvivorGames() + 1);
                if (stats.getSurvivorGames() >= 10) {
                    stats.setSurvivor(true);
                }
                if (elapsedTicks > 0 && elapsedTicks <= 1800) {
                    stats.setSpeedrunner(true);
                }
                if (task != null && !task.wasHiderGlowed(uuid)) {
                    stats.setGhost(true);
                }
            }
        }

        for (UUID uuid : arena.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                player.setGameMode(GameMode.SURVIVAL);
                if (waiting != null) {
                    player.teleport(waiting);
                }
            }
        }

        clearTeams(arena);
        arena.getSeekers().clear();
        arena.getHiders().clear();
        arena.getParticipants().clear();
        arena.setState(GameState.WAITING);
        statsManager.save();
    }

    public void broadcastToArena(Arena arena, String message) {
        if (arena == null) {
            return;
        }
        for (UUID uuid : arena.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Msg.send(player, message);
            }
        }
    }

    public void updateSpectatorVisibility(Player spectator, Arena arena) {
        if (spectator == null || arena == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (arena.getParticipants().contains(online.getUniqueId())) {
                spectator.showPlayer(plugin, online);
            } else {
                spectator.hidePlayer(plugin, online);
            }
        }
    }

    public void resetVisibility(Player player) {
        if (player == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            player.showPlayer(plugin, online);
        }
    }

    public void assignHider(Arena arena, Player player) {
        if (arena == null || player == null) {
            return;
        }
        arena.getParticipants().add(player.getUniqueId());
        arena.getHiders().add(player.getUniqueId());
        arena.getSeekers().remove(player.getUniqueId());
        Team hidenTeam = getOrCreateTeam(arena, true);
        Team speakerTeam = getOrCreateTeam(arena, false);
        if (speakerTeam != null) {
            speakerTeam.removePlayer(player);
        }
        if (hidenTeam != null) {
            hidenTeam.addPlayer(player);
        }
    }

    public void assignSeeker(Arena arena, Player player) {
        if (arena == null || player == null) {
            return;
        }
        arena.getParticipants().add(player.getUniqueId());
        arena.getSeekers().add(player.getUniqueId());
        arena.getHiders().remove(player.getUniqueId());
        Team hidenTeam = getOrCreateTeam(arena, true);
        Team speakerTeam = getOrCreateTeam(arena, false);
        if (hidenTeam != null) {
            hidenTeam.removePlayer(player);
        }
        if (speakerTeam != null) {
            speakerTeam.addPlayer(player);
        }
    }

    public void removeFromTeams(Arena arena, Player player) {
        if (arena == null || player == null) {
            return;
        }
        Team hidenTeam = getOrCreateTeam(arena, true);
        Team speakerTeam = getOrCreateTeam(arena, false);
        if (hidenTeam != null) {
            hidenTeam.removePlayer(player);
        }
        if (speakerTeam != null) {
            speakerTeam.removePlayer(player);
        }
    }

    public void clearTeams(Arena arena) {
        if (arena == null) {
            return;
        }
        Team hidenTeam = getOrCreateTeam(arena, true);
        Team speakerTeam = getOrCreateTeam(arena, false);
        if (hidenTeam != null) {
            new java.util.HashSet<>(hidenTeam.getEntries()).forEach(hidenTeam::removeEntry);
        }
        if (speakerTeam != null) {
            new java.util.HashSet<>(speakerTeam.getEntries()).forEach(speakerTeam::removeEntry);
        }
    }

    public void clearAllTeams() {
        for (Arena arena : arenaManager.getArenas()) {
            clearTeams(arena);
        }
    }

    public void syncTeams(Arena arena) {
        if (arena == null) {
            return;
        }
        clearTeams(arena);
        Team hidenTeam = getOrCreateTeam(arena, true);
        Team speakerTeam = getOrCreateTeam(arena, false);
        for (UUID uuid : arena.getHiders()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && hidenTeam != null) {
                hidenTeam.addPlayer(player);
            }
        }
        for (UUID uuid : arena.getSeekers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && speakerTeam != null) {
                speakerTeam.addPlayer(player);
            }
        }
    }

    private Team getOrCreateTeam(Arena arena, boolean hiden) {
        if (arena == null) {
            return null;
        }
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String key = arena.getName().toLowerCase();
        Map<String, Team> map = hiden ? hidenTeams : speakerTeams;
        Team team = map.get(key);
        if (team == null) {
            String name = buildTeamName(arena.getName(), hiden ? "hiden" : "speaker");
            team = scoreboard.getTeam(name);
            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }
            if (hiden) {
                team.prefix(Component.text("[HIDEN] ").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
                team.color(NamedTextColor.GREEN);
            } else {
                team.prefix(Component.text("[SEEK] ").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                team.color(NamedTextColor.RED);
            }
            map.put(key, team);
        }
        return team;
    }

    private String buildTeamName(String arenaName, String type) {
        String base = type + "_" + arenaName.toLowerCase();
        if (base.length() <= 16) {
            return base;
        }
        int hash = Math.abs(arenaName.toLowerCase().hashCode());
        String trimmed = type + "_" + Integer.toString(hash, 36);
        return trimmed.substring(0, Math.min(16, trimmed.length()));
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public Hiden getPlugin() {
        return plugin;
    }
}
