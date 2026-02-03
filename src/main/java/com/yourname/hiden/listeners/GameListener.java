package com.yourname.hiden.listeners;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.game.GameManager;
import com.yourname.hiden.util.Msg;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameListener implements Listener {
    private final Hiden plugin;
    private final Map<UUID, XrayPeek> xrayData;

    public GameListener(Hiden plugin) {
        this.plugin = plugin;
        this.xrayData = new HashMap<>();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player damager = null;
        if (event.getDamager() instanceof Player player) {
            damager = player;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        }
        if (damager == null) {
            return;
        }
        Arena arena = plugin.getGameManager().getArenaByPlayer(victim.getUniqueId());
        if (arena == null || arena.getState() != GameState.PLAYING) {
            return;
        }
        if (!arena.getSeekers().contains(damager.getUniqueId())) {
            return;
        }
        if (!arena.getHiders().contains(victim.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        plugin.getGameManager().handleKill(damager, victim, arena);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != GameState.PLAYING) {
            return;
        }
        if (arena.getHiders().contains(player.getUniqueId())) {
            plugin.getGameManager().handleHiderDeath(player, arena);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena != null && arena.getWaitingLoc() != null) {
            event.setRespawnLocation(arena.getWaitingLoc());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager manager = plugin.getGameManager();
        Arena arena = manager.getArenaByPlayer(player.getUniqueId());
        if (arena == null) {
            return;
        }
        arena.getParticipants().remove(player.getUniqueId());
        arena.getSeekers().remove(player.getUniqueId());
        arena.getHiders().remove(player.getUniqueId());
        manager.checkWin(arena);
        plugin.getArenaManager().save();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null) {
            return;
        }
        if (arena.getState() != GameState.PREP && arena.getState() != GameState.PLAYING) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && from.getWorld().equals(to.getWorld())) {
            double distance = from.distance(to);
            if (distance > 0.8 && shouldCheckSpeed(player)) {
                player.kick(Msg.colorize("&cAnti-cheat: швидкість занадто висока"));
                return;
            }
        }
        handleXrayPeek(player);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null) {
            return;
        }
        if (arena.getState() != GameState.PREP && arena.getState() != GameState.PLAYING) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        event.setCancelled(true);
        player.setAllowFlight(false);
        Msg.send(player, "&cПоліт заборонено під час гри.");
    }

    private boolean shouldCheckSpeed(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        if (player.getAllowFlight()) {
            return false;
        }
        return !player.hasPermission("hiden.admin");
    }

    private void handleXrayPeek(Player player) {
        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() == Material.AIR || !target.getType().isSolid()) {
            xrayData.remove(player.getUniqueId());
            return;
        }
        XrayPeek peek = xrayData.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (peek == null || !peek.isSame(target.getLocation())) {
            xrayData.put(player.getUniqueId(), new XrayPeek(target.getLocation(), now));
            return;
        }
        if (!peek.warned && now - peek.startTime >= 5000) {
            Msg.send(player, "&eПопередження: не зазирай у блоки.");
            peek.warned = true;
        }
    }

    private static class XrayPeek {
        private final int x;
        private final int y;
        private final int z;
        private final String world;
        private final long startTime;
        private boolean warned;

        private XrayPeek(Location location, long startTime) {
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
            this.world = location.getWorld().getName();
            this.startTime = startTime;
        }

        private boolean isSame(Location location) {
            return location.getWorld().getName().equals(world)
                    && location.getBlockX() == x
                    && location.getBlockY() == y
                    && location.getBlockZ() == z;
        }
    }
}
