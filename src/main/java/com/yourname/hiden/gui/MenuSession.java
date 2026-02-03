package com.yourname.hiden.gui;

public class MenuSession {
    private final MenuType type;
    private final String arenaName;
    private final int page;
    private final ArenaListMode listMode;

    public MenuSession(MenuType type, String arenaName, int page, ArenaListMode listMode) {
        this.type = type;
        this.arenaName = arenaName;
        this.page = page;
        this.listMode = listMode;
    }

    public MenuType getType() {
        return type;
    }

    public String getArenaName() {
        return arenaName;
    }

    public int getPage() {
        return page;
    }

    public ArenaListMode getListMode() {
        return listMode;
    }
}
