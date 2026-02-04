package com.yourname.hiden.gui;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6&lHiden &8| &e&lГоловне меню"));
        holder.setInventory(inventory);

        inventory.setItem(4, createItem(Material.NETHER_STAR, "&e&lАрени",
                List.of("&7Перегляд та керування аренами.", "&8Натисни, щоб відкрити список")));
        inventory.setItem(20, createItem(Material.LIME_BANNER, "&a&lПриєднатись як Hiden",
                List.of("&7Обери арену та грай за Hiden.", "&8Команда: /hiden join <arena> team hiden (Hiden)")));
        inventory.setItem(22, createItem(Material.RED_BANNER, "&c&lПриєднатись як Seek",
                List.of("&7Обери арену та грай за Seek.", "&8Команда: /hiden join <arena> team speaker (Seek)")));
        inventory.setItem(24, createItem(Material.SPYGLASS, "&b&lСпостерігати",
                List.of("&7Спостерігай за активною грою.", "&8Тільки під час гри")));
        inventory.setItem(38, createItem(Material.BOOK, "&e&lСтатистика та рейтинг",
                List.of("&7Перегляд своєї статистики", "&7та топ-гравців.")));
        inventory.setItem(49, createItem(Material.BARRIER, "&c&lЗакрити меню",
                List.of("&7Закрити це меню")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openPlayerMenu(Player player) {
        MenuSession session = new MenuSession(MenuType.PLAYER, null, 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6&lHiden &8| &e&lМеню гравця"));
        holder.setInventory(inventory);

        inventory.setItem(20, createItem(Material.LIME_BANNER, "&a&lГрати за Hiden",
                List.of("&7Обери арену та грай за Hiden.")));
        inventory.setItem(22, createItem(Material.RED_BANNER, "&c&lГрати за Seek",
                List.of("&7Обери арену та грай за Seek.")));
        inventory.setItem(24, createItem(Material.SPYGLASS, "&b&lСпостерігати",
                List.of("&7Спостерігай за активною грою.")));
        inventory.setItem(38, createItem(Material.BOOK, "&e&lМоя статистика",
                List.of("&7Перегляд своєї статистики")));
        inventory.setItem(49, createItem(Material.BARRIER, "&c&lЗакрити меню",
                List.of("&7Закрити це меню")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openArenaList(Player player, ArenaListMode mode, int page) {
        MenuSession session = new MenuSession(MenuType.ARENA_LIST, null, page, mode);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54, Msg.colorizeText("&6&lHiden &8| &e&lСписок арен"));
        holder.setInventory(inventory);

        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getArenas());
        arenas.sort(Comparator.comparing(Arena::getName));
        int start = page * 45;
        int end = Math.min(start + 45, arenas.size());
        for (int i = start; i < end; i++) {
            Arena arena = arenas.get(i);
            inventory.setItem(i - start, createArenaItem(arena));
        }

        inventory.setItem(45, createItem(Material.ARROW, "&e&lПопередня сторінка", List.of("&7Перейти назад")));
        inventory.setItem(49, createItem(Material.BARRIER, "&c&lНазад", List.of("&7Повернутись")));
        inventory.setItem(53, createItem(Material.ARROW, "&e&lНаступна сторінка", List.of("&7Перейти вперед")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openArenaSettings(Player player, Arena arena) {
        MenuSession session = new MenuSession(MenuType.ARENA_SETTINGS, arena.getName(), 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                Msg.colorizeText("&6&lHiden &8| &e&lАрена: &f" + arena.getName()));
        holder.setInventory(inventory);

        inventory.setItem(10, createItem(Material.ENDER_PEARL, "&a&lТелепорт до очікування", List.of("&7Телепортація")));
        inventory.setItem(12, createItem(Material.ENDER_PEARL, "&c&lТелепорт до Seek", List.of("&7Телепортація")));
        inventory.setItem(14, createItem(Material.ENDER_PEARL, "&e&lТелепорт до Hiden", List.of("&7Телепортація")));
        inventory.setItem(16, createItem(Material.ENDER_PEARL, "&b&lТелепорт до lobby", List.of("&7Телепортація")));

        inventory.setItem(28, createItem(Material.WHITE_WOOL, "&a&lВстановити точку очікування",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(30, createItem(Material.WHITE_WOOL, "&c&lВстановити точку Seek",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(32, createItem(Material.WHITE_WOOL, "&e&lВстановити точку Hiden",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(34, createItem(Material.WHITE_WOOL, "&b&lВстановити точку lobby",
                List.of("&7Зберігає твою поточну позицію")));
        inventory.setItem(36, createItem(Material.BARRIER, "&c&lВидалити точку lobby",
                List.of("&7Видаляє lobby варп")));

        inventory.setItem(22, createItem(Material.CLOCK, "&e&lНалаштування часу", List.of(
                "&7Очікування: &f" + Msg.formatTimeUA(arena.getTimeWaiting()),
                "&7Тривалість гри: &f" + Msg.formatTimeUA(arena.getTimeGames()),
                "&7Таймер підказки: &f" + Msg.formatTimeUA(arena.getTimeSeek()),
                " ",
                "&8Натисни для зміни"
        )));

        inventory.setItem(24, createItem(Material.ENCHANTED_BOOK, "&e&lНалаштування досягнень", List.of(
                "&7Керування досягненнями",
                "&8Натисни, щоб відкрити"
        )));

        int minPlayers = Math.max(2, Math.min(10, arena.getMinPlayers()));
        inventory.setItem(26, createItem(Material.ARMOR_STAND, "&e&lМінімальна кількість гравців", List.of(
                "&7Поточне значення: &f" + minPlayers,
                " ",
                "&aЛКМ: +1",
                "&cПКМ: -1",
                "&8Діапазон: 2 – 10"
        )));

        inventory.setItem(40, createItem(Material.NAME_TAG, "&e&lНазва арени", List.of(
                "&7Поточна: &f" + arena.getDisplayName(),
                "&8Натисни, щоб змінити"
        )));

        List<String> startLore = new ArrayList<>();
        startLore.add("&7Перевіряє налаштування");
        startLore.add("&7та запускає арену");
        if (arena.getParticipants().size() < minPlayers) {
            startLore.add(" ");
            startLore.add("&cНедостатньо гравців!");
        }
        inventory.setItem(45, createItem(Material.EMERALD_BLOCK, "&a&lЗапустити гру", startLore));

        inventory.setItem(53, createItem(Material.BARRIER, "&c&lВидалити арену", List.of(
                "&cУВАГА: дію не можна скасувати"
        )));

        inventory.setItem(49, createItem(Material.ARROW, "&c&lНазад", List.of("&7Повернутись")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openTimersMenu(Player player, Arena arena) {
        MenuSession session = new MenuSession(MenuType.TIMERS, arena.getName(), 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 27, Msg.colorizeText("&6&lHiden &8| &e&lЧасові налаштування"));
        holder.setInventory(inventory);

        inventory.setItem(10, createTimerItem("&e&lТривалість очікування", arena.getTimeWaiting()));
        inventory.setItem(13, createTimerItem("&e&lТривалість гри", arena.getTimeGames()));
        inventory.setItem(16, createTimerItem("&e&lТаймер підказки", arena.getTimeSeek()));
        inventory.setItem(22, createItem(Material.ARROW, "&c&lНазад", List.of("&7Повернутись")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    public void openAchievementsMenu(Player player, Arena arena) {
        MenuSession session = new MenuSession(MenuType.ACHIEVEMENTS, arena.getName(), 0, null);
        MenuHolder holder = new MenuHolder(session);
        Inventory inventory = Bukkit.createInventory(holder, 27, Msg.colorizeText("&6&lHiden &8| &e&lНалаштування досягнень"));
        holder.setInventory(inventory);

        inventory.setItem(11, createItem(Material.PAPER, "&e&lСкоро", List.of(
                "&7Налаштування буде додано",
                "&7у наступних оновленнях."
        )));
        inventory.setItem(13, createItem(Material.BOOK, "&e&lІнформація", List.of(
                "&7Це меню поки що",
                "&7не впливає на гру."
        )));
        inventory.setItem(15, createItem(Material.BOOKSHELF, "&e&lПараметри", List.of(
                "&7Доступно найближчим часом."
        )));
        inventory.setItem(22, createItem(Material.ARROW, "&c&lНазад", List.of("&7Повернутись")));

        fillEmpty(inventory);
        player.openInventory(inventory);
    }

    private ItemStack createTimerItem(String name, int value) {
        return createItem(Material.CLOCK, name, List.of(
                "&7Поточне значення: &f" + Msg.formatTimeUA(value),
                "&aЛКМ: +10с",
                "&cПКМ: -10с",
                "&eShift + клік: ±30с"
        ));
    }

    private ItemStack createArenaItem(Arena arena) {
        String state = switch (arena.getState()) {
            case WAITING -> "Очікування";
            case PREP -> "Очікування";
            case PLAYING -> "Гра";
            case ENDED -> "Завершено";
        };
        String wait = arena.getWaitingLoc() != null ? "&a✓" : "&c✗";
        String hiden = arena.getHidenLoc() != null ? "&a✓" : "&c✗";
        String speaker = arena.getSpeakerLoc() != null ? "&a✓" : "&c✗";
        String lobby = arena.getLobbyLoc() != null ? "&a✓" : "&c✗";

        List<String> lore = new ArrayList<>();
        lore.add("&7ID арени: &f" + arena.getName());
        lore.add("&7Світ: &f" + (arena.getWorld() == null ? "-" : arena.getWorld()));
        lore.add("&7Стан: &f" + state);
        lore.add("&7Гравців: &f" + arena.getParticipants().size());
        lore.add(" ");
        lore.add("&7Точка очікування: " + wait);
        lore.add("&7Точка Hiden: " + hiden);
        lore.add("&7Точка Seek: " + speaker);
        lore.add("&7Точка lobby: " + lobby);
        lore.add(" ");
        lore.add("&8Натисни для налаштувань");
        return createItem(Material.MAP, "&e&l" + arena.getDisplayName(), lore);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Msg.colorize(name).decoration(TextDecoration.ITALIC, false));
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(Msg.colorize(line).decoration(TextDecoration.ITALIC, false));
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
        }
    }
}
