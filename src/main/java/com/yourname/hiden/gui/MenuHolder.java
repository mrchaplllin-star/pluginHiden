package com.yourname.hiden.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {
    private final MenuSession session;
    private Inventory inventory;

    public MenuHolder(MenuSession session) {
        this.session = session;
    }

    public MenuSession getSession() {
        return session;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
