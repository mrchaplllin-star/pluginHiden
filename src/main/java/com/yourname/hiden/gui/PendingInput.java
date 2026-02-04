package com.yourname.hiden.gui;

public class PendingInput {
    private final InputType type;
    private final String arenaName;

    public PendingInput(InputType type, String arenaName) {
        this.type = type;
        this.arenaName = arenaName;
    }

    public InputType getType() {
        return type;
    }

    public String getArenaName() {
        return arenaName;
    }
}
