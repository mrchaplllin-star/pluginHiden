package com.yourname.hiden.gui;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuListener implements Listener {
    private final Hiden plugin;
    private final MenuManager menuManager;

    public MenuListener(Hiden plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof MenuHolder menuHolder)) {
            return;
        }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }
        MenuSession session = menuHolder.getSession();
        switch (session.getType()) {
            case MAIN -> handleMainMenu(player, event.getSlot());
            case PLAYER -> handlePlayerMenu(player, event.getSlot());
            case ARENA_LIST -> handleArenaList(player, session, event.getSlot());
            case ARENA_SETTINGS -> handleArenaSettings(player, session, event.getSlot());
            case TIMERS -> handleTimers(player, session, event.getSlot(), event.isLeftClick(), event.isRightClick(), event.isShiftClick());
            default -> {
            }
        }
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 4 -> menuManager.openArenaList(player, ArenaListMode.VIEW, 0);
            case 20 -> menuManager.openArenaList(player, ArenaListMode.JOIN_HIDER, 0);
            case 22 -> menuManager.openArenaList(player, ArenaListMode.JOIN_SEEKER, 0);
            case 24 -> menuManager.openArenaList(player, ArenaListMode.SPECTATE, 0);
            case 38 -> {
                Bukkit.dispatchCommand(player, "hiden stats");
                Bukkit.dispatchCommand(player, "hiden leaderboard total");
            }
            case 49 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handlePlayerMenu(Player player, int slot) {
        switch (slot) {
            case 20 -> menuManager.openArenaList(player, ArenaListMode.JOIN_HIDER, 0);
            case 22 -> menuManager.openArenaList(player, ArenaListMode.JOIN_SEEKER, 0);
            case 24 -> menuManager.openArenaList(player, ArenaListMode.SPECTATE, 0);
            case 38 -> Bukkit.dispatchCommand(player, "hiden stats");
            case 49 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void handleArenaList(Player player, MenuSession session, int slot) {
        if (slot == 45) {
            int prev = Math.max(0, session.getPage() - 1);
            menuManager.openArenaList(player, session.getListMode(), prev);
            return;
        }
        if (slot == 53) {
            int next = session.getPage() + 1;
            menuManager.openArenaList(player, session.getListMode(), next);
            return;
        }
        if (slot == 49) {
            openRootMenu(player);
            return;
        }
        List<Arena> arenas = plugin.getArenaManager().getArenas().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).toList();
        int index = session.getPage() * 45 + slot;
        if (index < 0 || index >= arenas.size() || slot > 44) {
            return;
        }
        Arena arena = arenas.get(index);
        switch (session.getListMode()) {
            case VIEW -> {
                if (!player.hasPermission("hiden.admin")) {
                    Msg.send(player, "&cНемає прав.");
                    return;
                }
                menuManager.openArenaSettings(player, arena);
            }
            case JOIN_HIDER -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "hiden join " + arena.getName() + " team hiden");
            }
            case JOIN_SEEKER -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "hiden join " + arena.getName() + " team speaker");
            }
            case SPECTATE -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "hiden spectate " + arena.getName());
            }
            default -> {
            }
        }
    }

    private void handleArenaSettings(Player player, MenuSession session, int slot) {
        if (!player.hasPermission("hiden.admin")) {
            Msg.send(player, "&cНемає прав.");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(session.getArenaName());
        if (arena == null) {
            Msg.send(player, "&cАрена не існує!");
            player.closeInventory();
            return;
        }
        switch (slot) {
            case 10 -> Bukkit.dispatchCommand(player, "hiden tp " + arena.getName() + " waiting");
            case 12 -> Bukkit.dispatchCommand(player, "hiden tp " + arena.getName() + " speaker");
            case 14 -> Bukkit.dispatchCommand(player, "hiden tp " + arena.getName() + " hiden");
            case 28 -> Bukkit.dispatchCommand(player, "hiden arena " + arena.getName() + " setwarp waiting");
            case 30 -> Bukkit.dispatchCommand(player, "hiden arena " + arena.getName() + " setwarp speaker");
            case 32 -> Bukkit.dispatchCommand(player, "hiden arena " + arena.getName() + " setwarp hiden");
            case 22 -> menuManager.openTimersMenu(player, arena);
            case 24 -> {
                menuManager.setPendingInput(player, new PendingInput(InputType.SEEKERS_PERCENT, arena.getName()));
                player.closeInventory();
                Msg.send(player, "&eВведи новий відсоток шукачів у чат.");
                Msg.send(player, "&7Напиши &ccancel&7 для скасування.");
            }
            case 40 -> {
                menuManager.setPendingInput(player, new PendingInput(InputType.DISPLAY_NAME, arena.getName()));
                player.closeInventory();
                Msg.send(player, "&eВведи нову назву арени в чат.");
                Msg.send(player, "&7Напиши &ccancel&7 для скасування.");
            }
            case 45 -> Bukkit.dispatchCommand(player, "hiden start " + arena.getName());
            case 49 -> menuManager.openArenaList(player, ArenaListMode.VIEW, 0);
            case 53 -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "hiden remove arena " + arena.getName());
            }
            default -> {
            }
        }
    }

    private void handleTimers(Player player, MenuSession session, int slot, boolean left, boolean right, boolean shift) {
        if (!player.hasPermission("hiden.admin")) {
            Msg.send(player, "&cНемає прав.");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(session.getArenaName());
        if (arena == null) {
            Msg.send(player, "&cАрена не існує!");
            player.closeInventory();
            return;
        }
        if (slot == 22) {
            menuManager.openArenaSettings(player, arena);
            return;
        }
        int delta = 0;
        if (left && shift) {
            delta = 1000;
        } else if (right && shift) {
            delta = -1000;
        } else if (left) {
            delta = 100;
        } else if (right) {
            delta = -100;
        }
        if (delta == 0) {
            return;
        }
        switch (slot) {
            case 10 -> arena.setPrepTime(Math.max(0, arena.getPrepTime() + delta));
            case 12 -> arena.setTimeWaiting(Math.max(0, arena.getTimeWaiting() + delta));
            case 14 -> arena.setTimeGames(Math.max(0, arena.getTimeGames() + delta));
            case 16 -> arena.setTimeSeek(Math.max(0, arena.getTimeSeek() + delta));
            default -> {
                return;
            }
        }
        plugin.getArenaManager().save();
        menuManager.openTimersMenu(player, arena);
    }

    private void openRootMenu(Player player) {
        if (player.hasPermission("hiden.admin")) {
            menuManager.openMainMenu(player);
        } else {
            menuManager.openPlayerMenu(player);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!menuManager.hasPendingInput(player)) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> menuManager.handleChatInput(player, message));
    }

}
