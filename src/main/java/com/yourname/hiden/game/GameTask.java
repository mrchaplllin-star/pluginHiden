package com.yourname.hiden.game;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.util.Msg;
import com.yourname.hiden.util.SafeTeleport;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameTask extends BukkitRunnable {
    private final Hiden plugin;
    private final GameManager manager;
    private final Arena arena;
    private Phase phase;
    private int remainingTicks;
    private long elapsedPlayingTicks;
    private boolean firstKill;
    private final Map<UUID, Boolean> hiderGlow;
    private int chickenSoundTicks;

    private enum Phase {
        PREP,
        PLAYING
    }

    public GameTask(Hiden plugin, GameManager manager, Arena arena) {
        this.plugin = plugin;
        this.manager = manager;
        this.arena = arena;
        this.phase = Phase.PREP;
        this.remainingTicks = Math.max(0, arena.getPrepTime());
        this.hiderGlow = new HashMap<>();
        this.chickenSoundTicks = 0;
        startPrep();
    }

    private void startPrep() {
        arena.setState(GameState.PREP);
        BossBar bossBar = arena.getBossBar();
        bossBar.setTitle(Msg.colorizeText("&eПідготовка"));
        bossBar.setProgress(1.0);
        for (UUID uuid : arena.getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                bossBar.addPlayer(player);
            }
        }
        manager.broadcastToArena(arena, "&eПочаток підготовки! Шукачі заморожені.");
    }

    private void startPlaying() {
        phase = Phase.PLAYING;
        remainingTicks = Math.max(0, arena.getTimeGames());
        arena.setState(GameState.PLAYING);
        elapsedPlayingTicks = 0;
        for (UUID uuid : arena.getSeekers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(arena.getSpeakerLoc());
                Msg.send(player, "&cТи шукач! Шукаєш ховальників.");
            }
        }
        for (UUID uuid : arena.getHiders()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                Location safe = SafeTeleport.findSafe(arena.getHidenLoc());
                if (safe != null) {
                    player.teleport(safe);
                }
                Msg.send(player, "&aТи ховальник! Ховайся.");
                hiderGlow.put(uuid, false);
            }
        }
        manager.broadcastToArena(arena, "&aГра почалась!");
    }

    @Override
    public void run() {
        if (phase == Phase.PREP) {
            handlePrep();
            return;
        }
        handlePlaying();
    }

    private void handlePrep() {
        BossBar bossBar = arena.getBossBar();
        double progress = remainingTicks <= 0 ? 0 : Math.min(1.0, remainingTicks / (double) Math.max(1, arena.getPrepTime()));
        bossBar.setTitle(Msg.colorizeText("&eПідготовка: &f" + (remainingTicks / 20) + "с"));
        bossBar.setProgress(progress);
        for (UUID uuid : arena.getSeekers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 255, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 255, false, false, false));
            }
        }
        remainingTicks -= 20;
        if (remainingTicks <= 0) {
            startPlaying();
        }
    }

    private void handlePlaying() {
        BossBar bossBar = arena.getBossBar();
        double progress = remainingTicks <= 0 ? 0 : Math.min(1.0, remainingTicks / (double) Math.max(1, arena.getTimeGames()));
        bossBar.setTitle(Msg.colorizeText("&aГра: &f" + (remainingTicks / 20) + "с"));
        bossBar.setProgress(progress);

        elapsedPlayingTicks += 20;
        chickenSoundTicks += 20;

        int timeSeek = arena.getTimeSeek();
        if (timeSeek > 0 && elapsedPlayingTicks % timeSeek == 0) {
            for (UUID uuid : arena.getHiders()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0, false, false, false));
                    hiderGlow.put(uuid, true);
                }
            }
        }

        if (chickenSoundTicks >= 900) {
            chickenSoundTicks = 0;
            for (UUID seekerId : arena.getSeekers()) {
                Player seeker = Bukkit.getPlayer(seekerId);
                if (seeker == null) {
                    continue;
                }
                for (UUID hiderId : arena.getHiders()) {
                    Player hider = Bukkit.getPlayer(hiderId);
                    if (hider != null) {
                        seeker.playSound(hider.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1f, 1f);
                    }
                }
            }
        }

        remainingTicks -= 20;
        if (remainingTicks <= 0) {
            boolean seekersWin = arena.getHiders().isEmpty();
            manager.endGame(arena, seekersWin);
        }
    }

    public boolean isFirstKill() {
        return firstKill;
    }

    public void setFirstKill(boolean firstKill) {
        this.firstKill = firstKill;
    }

    public long getElapsedPlayingTicks() {
        return elapsedPlayingTicks;
    }

    public boolean wasHiderGlowed(UUID uuid) {
        return hiderGlow.getOrDefault(uuid, false);
    }
}
