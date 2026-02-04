package com.yourname.hiden.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Arena {
    private final String name;
    private String world;
    private String displayName;
    private Location waitingLoc;
    private Location hidenLoc;
    private Location speakerLoc;
    private int timeWaiting;
    private int timeGames;
    private int timeSeek;
    private int hidersMin;
    private int seekersMin;
    private int minPlayers;
    private Location lobbyLoc;
    private GameState state;
    private final List<UUID> participants;
    private final Set<UUID> seekers;
    private final Set<UUID> hiders;
    private BossBar bossBar;

    public Arena(String name) {
        this.name = name;
        this.participants = new ArrayList<>();
        this.seekers = new HashSet<>();
        this.hiders = new HashSet<>();
        this.state = GameState.WAITING;
        this.displayName = name;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public World resolveWorld() {
        return world == null ? null : Bukkit.getWorld(world);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getWaitingLoc() {
        return waitingLoc;
    }

    public void setWaitingLoc(Location waitingLoc) {
        this.waitingLoc = waitingLoc;
    }

    public Location getHidenLoc() {
        return hidenLoc;
    }

    public void setHidenLoc(Location hidenLoc) {
        this.hidenLoc = hidenLoc;
    }

    public Location getSpeakerLoc() {
        return speakerLoc;
    }

    public void setSpeakerLoc(Location speakerLoc) {
        this.speakerLoc = speakerLoc;
    }

    public int getTimeWaiting() {
        return timeWaiting;
    }

    public void setTimeWaiting(int timeWaiting) {
        this.timeWaiting = timeWaiting;
    }

    public int getTimeGames() {
        return timeGames;
    }

    public void setTimeGames(int timeGames) {
        this.timeGames = timeGames;
    }

    public int getTimeSeek() {
        return timeSeek;
    }

    public void setTimeSeek(int timeSeek) {
        this.timeSeek = timeSeek;
    }

    public int getHidersMin() {
        return hidersMin;
    }

    public void setHidersMin(int hidersMin) {
        this.hidersMin = hidersMin;
    }

    public int getSeekersMin() {
        return seekersMin;
    }

    public void setSeekersMin(int seekersMin) {
        this.seekersMin = seekersMin;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Location getLobbyLoc() {
        return lobbyLoc;
    }

    public void setLobbyLoc(Location lobbyLoc) {
        this.lobbyLoc = lobbyLoc;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public List<UUID> getParticipants() {
        return participants;
    }

    public Set<UUID> getSeekers() {
        return seekers;
    }

    public Set<UUID> getHiders() {
        return hiders;
    }

    public BossBar getBossBar() {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
        }
        return bossBar;
    }

    public void clearBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
}
