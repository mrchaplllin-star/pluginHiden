package com.yourname.hiden.gui;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {
    private final Hiden plugin;
    private final Map<UUID, PendingInput> pendingInputs;

    public MenuManager(Hiden plugin) {
        this.plugin = plugin;
        this.pendingInputs = new ConcurrentHashMap<>();
    }

    public boolean hasPendingInput(Player player) {
        return pendingInputs.containsKey(player.getUniqueId());
    }

    public PendingInput getPendingInput(Player player) {
        return pendingInputs.get(player.getUniqueId());
    }

    public void clearPendingInput(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }

    public void setPendingInput(Player player, PendingInput input) {
        pendingInputs.put(player.getUniqueId(), input);
    }

    public void openMainMenu(Player player) {
        MenuSession session = new MenuSession(MenuType.MAIN, null, 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6Hiden &8| &eГоловне меню"));
        holder.setInventory(inventory);

        inventory.setItem(4, createItem(Material.NETHER_STAR, "&eАрени",
                List.of("&7Перегляд та керування аренами.", "&8Натисни, щоб відкрити список")));
        inventory.setItem(20, createItem(Material.LIME_BANNER, "&aПриєднатись як хованка",
                List.of("&7Обери арену та грай за хованку.", "&8Команда: /hiden join <arena> team hiden")));
        inventory.setItem(22, createItem(Material.RED_BANNER, "&cПриєднатись як шукач",
                List.of("&7Обери арену та грай за шукача.", "&8Команда: /hiden join <arena> team speaker")));
        inventory.setItem(24, createItem(Material.SPYGLASS, "&bСпостерігати",
                List.of("&7Спостерігай за активною грою.", "&8Тільки під час гри")));
        inventory.setItem(38, createItem(Material.BOOK, "&eСтатистика та рейтинг",
                List.of("&7Перегляд своєї статистики", "&7та топ-гравців.")));
        inventory.setItem(49, createItem(Material.BARRIER, "&cЗакрити меню",
                List.of("&7Закрити це меню")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openPlayerMenu(Player player) {
        MenuSession session = new MenuSession(MenuType.PLAYER, null, 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6Hiden &8| &eМеню гравця"));
        holder.setInventory(inventory);

        inventory.setItem(20, createItem(Material.LIME_BANNER, "&aГрати за хованку",
                List.of("&7Обери арену та грай за хованку.")));
        inventory.setItem(22, createItem(Material.RED_BANNER, "&cГрати за шукача",
                List.of("&7Обери арену та грай за шукача.")));
        inventory.setItem(24, createItem(Material.SPYGLASS, "&bСпостерігати",
                List.of("&7Спостерігай за активною грою.")));
        inventory.setItem(38, createItem(Material.BOOK, "&eМоя статистика",
                List.of("&7Перегляд своєї статистики")));
        inventory.setItem(49, createItem(Material.BARRIER, "&cЗакрити меню",
                List.of("&7Закрити це меню")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openArenaList(Player player, ArenaListMode mode, int page) {
        MenuSession session = new MenuSession(MenuType.ARENA_LIST, null, page, mode);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6Hiden &8| &eСписок арен"));
        holder.setInventory(inventory);

        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        arenas.sort(Comparator.comparing(Arena::getName));
        int start = page * 45;
        int end = Math.min(start + 45, arenas.size());
        for (int i = start; i < end; i++) {
            Arena arena = arenas.get(i);
            inventory.setItem(i - start, createArenaItem(arena));
        }

        inventory.setItem(45, createItem(Material.ARROW, "&eПопередня сторінка", List.of("&7Перейти назад")));
        inventory.setItem(49, createItem(Material.BARRIER, "&cНазад", List.of("&7Повернутись")));
        inventory.setItem(53, createItem(Material.ARROW, "&eНаступна сторінка", List.of("&7Перейти вперед")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openArenaSettings(Player player, Arena arena) {
        MenuSession session = new MenuSession(MenuType.ARENA_SETTINGS, arena.getName(), 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                Msg.colorizeText("&6Hiden &8| &eАрена: &f" + arena.getName()));
        holder.setInventory(inventory);

        inventory.setItem(10, createItem(Material.ENDER_PEARL, "&aТелепорт до очікування", List.of("&7Телепортація")));
        inventory.setItem(12, createItem(Material.ENDER_PEARL, "&cТелепорт до шукачів", List.of("&7Телепортація")));
        inventory.setItem(14, createItem(Material.ENDER_PEARL, "&eТелепорт до хованок", List.of("&7Телепортація")));

        inventory.setItem(28, createItem(Material.WHITE_WOOL, "&aВстановити точку очікування",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(30, createItem(Material.WHITE_WOOL, "&cВстановити точку шукачів",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(32, createItem(Material.WHITE_WOOL, "&eВстановити точку хованок",
                List.of("&7Зберігає твою поточну позицію")));

        inventory.setItem(22, createItem(Material.CLOCK, "&eНалаштування часу", List.of(
                "&7Підготовка: &f" + arena.getPrepTime(),
                "&7Очікування: &f" + arena.getTimeWaiting(),
                "&7Тривалість гри: &f" + arena.getTimeGames(),
                "&7Підсвітка хованок: &f" + arena.getTimeSeek(),
                " ",
                "&8Натисни для зміни"
        )));

        inventory.setItem(24, createItem(Material.COMPASS, "&eВідсоток шукачів", List.of(
                "&7Поточне значення: &f" + arena.getSeekersPercent() + "%"
        )));

        inventory.setItem(40, createItem(Material.NAME_TAG, "&eНазва арени", List.of(
                "&7Поточна: &f" + arena.getDisplayName(),
                "&8Натисни, щоб змінити"
        )));

        inventory.setItem(45, createItem(Material.EMERALD_BLOCK, "&aЗапустити гру", List.of(
                "&7Перевіряє налаштування",
                "&7та запускає арену"
        )));

        inventory.setItem(53, createItem(Material.BARRIER, "&cВидалити арену", List.of(
                "&cУВАГА: дію не можна скасувати"
        )));

        inventory.setItem(49, createItem(Material.ARROW, "&cНазад", List.of("&7Повернутись")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openTimersMenu(Player player, Arena arena) {
        MenuSession session = new MenuSession(MenuType.TIMERS, arena.getName(), 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 27, Msg.colorizeText("&6Hiden &8| &eЧасові налаштування"));
        holder.setInventory(inventory);

        inventory.setItem(10, createTimerItem("&eЧас підготовки", arena.getPrepTime()));
        inventory.setItem(12, createTimerItem("&eЧас очікування", arena.getTimeWaiting()));
        inventory.setItem(14, createTimerItem("&eТривалість гри", arena.getTimeGames()));
        inventory.setItem(16, createTimerItem("&eПідсвітка хованок", arena.getTimeSeek()));
        inventory.setItem(22, createItem(Material.ARROW, "&cНазад", List.of("&7Повернутись")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    private ItemStack createTimerItem(String name, int value) {
        return createItem(Material.CLOCK, name, List.of(
                "&7Поточне значення: &f" + value,
                "&aЛКМ: +100",
                "&cПКМ: -100",
                "&eShift + клік: ±1000"
        ));
    }

    private ItemStack createArenaItem(Arena arena) {
        String state = switch (arena.getState()) {
            case WAITING -> "Очікування";
            case PREP -> "Підготовка";
            case PLAYING -> "Гра";
            case ENDED -> "Завершено";
        };
        String wait = arena.getWaitingLoc() != null ? "&a✓" : "&c✗";
        String hiden = arena.getHidenLoc() != null ? "&a✓" : "&c✗";
        String speaker = arena.getSpeakerLoc() != null ? "&a✓" : "&c✗";

        List<String> lore = new ArrayList<>();
        lore.add("&7ID арени: &f" + arena.getName());
        lore.add("&7Світ: &f" + (arena.getWorld() == null ? "-" : arena.getWorld()));
        lore.add("&7Стан: &f" + state);
        lore.add("&7Гравців: &f" + arena.getParticipants().size());
        lore.add(" ");
        lore.add("&7Точка очікування: " + wait);
        lore.add("&7Точка хованок: " + hiden);
        lore.add("&7Точка шукачів: " + speaker);
        lore.add(" ");
        lore.add("&8Натисни для налаштувань");
        return createItem(Material.MAP, "&e" + arena.getDisplayName(), lore);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.colorize(name));
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Msg.colorize(line));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillEmpty(Inventory inventory) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "&7", List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public void handleChatInput(Player player, String message) {
        PendingInput input = pendingInputs.get(player.getUniqueId());
        if (input == null) {
            return;
        }
        if (message.equalsIgnoreCase("cancel")) {
            clearPendingInput(player);
            Msg.send(player, "&cЗміну скасовано.");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(input.getArenaName());
        if (arena == null) {
            clearPendingInput(player);
            Msg.send(player, "&cАрена не існує!");
            return;
        }
        if (input.getType() == InputType.DISPLAY_NAME) {
            arena.setDisplayName(message);
            plugin.getArenaManager().save();
            clearPendingInput(player);
            Msg.send(player, "&aНазву арени оновлено.");
            openArenaSettings(player, arena);
            return;
        }
        if (input.getType() == InputType.SEEKERS_PERCENT) {
            try {
                int value = Integer.parseInt(message);
                value = Math.max(1, Math.min(50, value));
                arena.setSeekersPercent(value);
                plugin.getArenaManager().save();
                clearPendingInput(player);
                Msg.send(player, "&aВідсоток шукачів оновлено.");
                openArenaSettings(player, arena);
            } catch (NumberFormatException e) {
                Msg.send(player, "&cВведи число від 1 до 50.");
            }
        }
    }
}
