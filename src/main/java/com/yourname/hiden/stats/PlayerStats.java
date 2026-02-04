package com.yourname.hiden.stats;

import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private int hiderWins;
    private int seekerWins;
    private int hunterKills;
    private int survivorGames;
    private boolean firstBlood;
    private boolean survivor;
    private boolean hunter;
    private boolean speedrunner;
    private boolean ghost;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getHiderWins() {
        return hiderWins;
    }

    public void setHiderWins(int hiderWins) {
        this.hiderWins = hiderWins;
    }

    public int getSeekerWins() {
        return seekerWins;
    }

    public void setSeekerWins(int seekerWins) {
        this.seekerWins = seekerWins;
    }

    public int getHunterKills() {
        return hunterKills;
    }

    public void setHunterKills(int hunterKills) {
        this.hunterKills = hunterKills;
    }

    public int getSurvivorGames() {
        return survivorGames;
    }

    public void setSurvivorGames(int survivorGames) {
        this.survivorGames = survivorGames;
    }

    public boolean isFirstBlood() {
        return firstBlood;
    }

    public void setFirstBlood(boolean firstBlood) {
        this.firstBlood = firstBlood;
    }

    public boolean isSurvivor() {
        return survivor;
    }

    public void setSurvivor(boolean survivor) {
        this.survivor = survivor;
    }

    public boolean isHunter() {
        return hunter;
    }

    public void setHunter(boolean hunter) {
        this.hunter = hunter;
    }

    public boolean isSpeedrunner() {
        return speedrunner;
    }

    public void setSpeedrunner(boolean speedrunner) {
        this.speedrunner = speedrunner;
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }
}
