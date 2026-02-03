package com.yourname.hiden.commands;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import com.yourname.hiden.arena.GameState;
import com.yourname.hiden.game.GameManager;
import com.yourname.hiden.stats.PlayerStats;
import com.yourname.hiden.stats.StatsManager;
import com.yourname.hiden.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HidenCommand implements CommandExecutor {
    private final Hiden plugin;

    public HidenCommand(Hiden plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "arena" -> handleArena(sender, args);
            case "list" -> handleList(sender, args);
            case "tp" -> handleTp(sender, args);
            case "start" -> handleStart(sender, args);
            case "join" -> handleJoin(sender, args);
            case "leave" -> handleLeave(sender, args);
            case "stats" -> handleStats(sender, args);
            case "spectate" -> handleSpectate(sender, args);
            case "achievements" -> handleAchievements(sender, args);
            case "leaderboard" -> handleLeaderboard(sender, args);
            case "ach" -> handleGrantAchievement(sender, args);
            case "menu" -> handleMenu(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        Msg.send(sender, "&e/hiden join <arena> [team <hiden|speaker>] &7- приєднатись (Hiden/Seek)");
        Msg.send(sender, "&e/hiden leave &7- вийти");
        Msg.send(sender, "&e/hiden stats [гравець] &7- статистика");
        Msg.send(sender, "&e/hiden achievements &7- досягнення");
        Msg.send(sender, "&e/hiden leaderboard [hider|seeker|total] &7- топ-10");
        Msg.send(sender, "&e/hiden menu &7- меню");
        if (sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&6Адмін: create/remove/arena/start/list/tp/ach");
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 3 || !"arena".equalsIgnoreCase(args[1])) {
            Msg.send(sender, "&cВикористання: /hiden create arena <name>");
            return;
        }
        String name = args[2];
        if (plugin.getArenaManager().getArena(name) != null) {
            Msg.send(sender, "&cАрена вже існує.");
            return;
        }
        Arena arena = plugin.getArenaManager().createArena(name);
        arena.setTimeWaiting(plugin.getConfig().getInt("default_time_waiting"));
        arena.setTimeGames(plugin.getConfig().getInt("default_time_games"));
        arena.setPrepTime(plugin.getConfig().getInt("default_prep_time"));
        arena.setTimeSeek(plugin.getConfig().getInt("default_timeseek"));
        arena.setSeekersPercent(plugin.getConfig().getInt("default_seekers_percent"));
        arena.setHidersMin(plugin.getConfig().getInt("hiders_min"));
        arena.setSeekersMin(plugin.getConfig().getInt("seekers_min"));
        arena.setMinPlayers(plugin.getConfig().getInt("default_min_players", 2));
        plugin.getArenaManager().save();
        Msg.send(sender, "&aАрена створена: &f" + name);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 3 || !"arena".equalsIgnoreCase(args[1])) {
            Msg.send(sender, "&cВикористання: /hiden remove arena <name>");
            return;
        }
        String name = args[2];
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            Msg.send(sender, "&cАрена не знайдена.");
            return;
        }
        plugin.getArenaManager().removeArena(name);
        plugin.getArenaManager().save();
        Msg.send(sender, "&aАрена видалена.");
    }

    private void handleArena(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 3) {
            Msg.send(sender, "&cВикористання: /hiden arena <name> ...");
            return;
        }
        String name = args[1];
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            Msg.send(sender, "&cАрена не знайдена.");
            return;
        }
        String action = args[2].toLowerCase(Locale.ROOT);
        switch (action) {
            case "setwarp" -> handleSetWarp(sender, arena, args);
            case "removewarp" -> handleRemoveWarp(sender, arena, args);
            case "setting" -> handleSettings(sender, arena, args);
            case "setname" -> handleSetName(sender, arena, args);
            default -> Msg.send(sender, "&cНевідома дія.");
        }
    }

    private void handleSetWarp(CommandSender sender, Arena arena, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (args.length < 4) {
            Msg.send(sender, "&cВикористання: /hiden arena <name> setwarp <hiden|speaker|waiting>");
            return;
        }
        String type = args[3].toLowerCase(Locale.ROOT);
        Location loc = player.getLocation();
        arena.setWorld(loc.getWorld().getName());
        switch (type) {
            case "hiden" -> arena.setHidenLoc(loc);
            case "speaker" -> arena.setSpeakerLoc(loc);
            case "waiting" -> arena.setWaitingLoc(loc);
            default -> {
                Msg.send(sender, "&cНевірний тип варпу.");
                return;
            }
        }
        plugin.getArenaManager().save();
        Msg.send(sender, "&aВарп встановлено.");
    }

    private void handleRemoveWarp(CommandSender sender, Arena arena, String[] args) {
        if (args.length < 4) {
            Msg.send(sender, "&cВикористання: /hiden arena <name> removewarp <hiden|speaker|waiting>");
            return;
        }
        String type = args[3].toLowerCase(Locale.ROOT);
        switch (type) {
            case "hiden" -> arena.setHidenLoc(null);
            case "speaker" -> arena.setSpeakerLoc(null);
            case "waiting" -> arena.setWaitingLoc(null);
            default -> {
                Msg.send(sender, "&cНевірний тип варпу.");
                return;
            }
        }
        plugin.getArenaManager().save();
        Msg.send(sender, "&aВарп видалено.");
    }

    private void handleSettings(CommandSender sender, Arena arena, String[] args) {
        if (args.length < 4) {
            Msg.send(sender, "&cВикористання: /hiden arena <name> setting prep_time=<ticks> timegames=<ticks> timewaiting=<ticks> seekers_percent=<1-50> timeseek=<ticks> hiders_min=<n> seekers_min=<n>");
            return;
        }
        for (int i = 3; i < args.length; i++) {
            String[] pair = args[i].split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].toLowerCase(Locale.ROOT);
            String value = pair[1];
            try {
                int intValue = Integer.parseInt(value);
                switch (key) {
                    case "prep_time" -> arena.setPrepTime(intValue);
                    case "timegames" -> arena.setTimeGames(intValue);
                    case "timewaiting" -> arena.setTimeWaiting(intValue);
                    case "seekers_percent" -> arena.setSeekersPercent(Math.max(1, Math.min(50, intValue)));
                    case "timeseek" -> arena.setTimeSeek(intValue);
                    case "hiders_min" -> arena.setHidersMin(Math.max(1, intValue));
                    case "seekers_min" -> arena.setSeekersMin(Math.max(1, intValue));
                    default -> {
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        plugin.getArenaManager().save();
        Msg.send(sender, "&aНалаштування оновлено.");
    }

    private void handleSetName(CommandSender sender, Arena arena, String[] args) {
        if (args.length < 4) {
            Msg.send(sender, "&cВикористання: /hiden arena <name> setname <назва>");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (i > 3) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        String name = builder.toString().replace("\"", "");
        arena.setDisplayName(name);
        plugin.getArenaManager().save();
        Msg.send(sender, "&aНазву встановлено.");
    }

    private void handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 2 || !"arena".equalsIgnoreCase(args[1])) {
            Msg.send(sender, "&cВикористання: /hiden list arena");
            return;
        }
        StringBuilder builder = new StringBuilder("&eАрени: ");
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            builder.append("&f").append(arena.getName()).append("&7, ");
        }
        Msg.send(sender, builder.toString());
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&cВикористання: /hiden tp <name> [hiden|speaker|waiting]");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            Msg.send(sender, "&cАрена не знайдена.");
            return;
        }
        Location target = arena.getWaitingLoc();
        if (args.length >= 3) {
            String warp = args[2].toLowerCase(Locale.ROOT);
            if (warp.equals("hiden")) {
                target = arena.getHidenLoc();
            } else if (warp.equals("speaker")) {
                target = arena.getSpeakerLoc();
            } else if (warp.equals("waiting")) {
                target = arena.getWaitingLoc();
            }
        }
        if (target == null) {
            Msg.send(sender, "&cВарп не встановлений.");
            return;
        }
        player.teleport(target);
        Msg.send(sender, "&aТелепортовано.");
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&cВикористання: /hiden start <arena>");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            Msg.send(sender, "&cАрена не знайдена.");
            return;
        }
        if (!plugin.getGameManager().startGame(arena)) {
            String error = plugin.getGameManager().getLastError();
            if (error != null) {
                Msg.send(sender, error);
            } else {
                Msg.send(sender, "&cНе вдалося стартувати гру.");
            }
            return;
        }
        Msg.send(sender, "&aГру запущено.");
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (!player.hasPermission("hiden.join") && !player.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&cВикористання: /hiden join <arena> team <hiden|speaker>");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            Msg.send(sender, "&cАрена не існує!");
            return;
        }
        if (!isPlayerInArenaWorld(player, arena) && !player.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cКоманда тільки в арені!");
            return;
        }
        if (arena.getState() == GameState.PREP || arena.getState() == GameState.PLAYING) {
            Msg.send(sender, "&cГра вже запущена!");
            return;
        }
        boolean alreadyParticipant = arena.getParticipants().contains(player.getUniqueId());
        if (!alreadyParticipant) {
            arena.getParticipants().add(player.getUniqueId());
        }
        if (args.length >= 3) {
            if (!"team".equalsIgnoreCase(args[2])) {
                Msg.send(sender, "&cВикористання: /hiden join <arena> team <hiden|speaker>");
                return;
            }
            if (args.length < 4) {
                Msg.send(sender, "&cВикористання: /hiden join <arena> team <hiden|speaker>");
                return;
            }
            String role = args[3].toLowerCase(Locale.ROOT);
            if (role.equals("hiden")) {
                plugin.getGameManager().assignHider(arena, player);
                Msg.send(sender, "&aТи граєш за &fHiden");
            } else if (role.equals("speaker")) {
                plugin.getGameManager().assignSeeker(arena, player);
                Msg.send(sender, "&cТи граєш за &fSeek");
            } else {
                Msg.send(sender, "&cВикористання: /hiden join <arena> team <hiden|speaker>");
                return;
            }
        } else if (alreadyParticipant) {
            Msg.send(sender, "&eВи вже в арені.");
            return;
        }
        plugin.getArenaManager().save();
        Msg.send(sender, "&aТи приєднався до арени &f" + arena.getDisplayName());
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (sender.hasPermission("hiden.admin")) {
            plugin.getMenuManager().openMainMenu(player);
        } else {
            plugin.getMenuManager().openPlayerMenu(player);
        }
    }

    private void handleLeave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (!player.hasPermission("hiden.join")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        Arena arena = null;
        if (args.length >= 2) {
            arena = plugin.getArenaManager().getArena(args[1]);
        }
        if (arena == null) {
            arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        }
        if (arena == null) {
            Msg.send(sender, "&cВи не в арені.");
            return;
        }
        if (!isPlayerInArenaWorld(player, arena)) {
            Msg.send(sender, "&cКоманда тільки в арені!");
            return;
        }
        arena.getParticipants().remove(player.getUniqueId());
        arena.getSeekers().remove(player.getUniqueId());
        arena.getHiders().remove(player.getUniqueId());
        plugin.getGameManager().removeFromTeams(arena, player);
        plugin.getArenaManager().save();
        player.setGameMode(GameMode.SURVIVAL);
        World world = player.getWorld();
        player.teleport(world.getSpawnLocation());
        plugin.getGameManager().resetVisibility(player);
        plugin.getGameManager().checkWin(arena);
        Msg.send(sender, "&aВи вийшли з арени.");
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.stats")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        Player target = null;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
        }
        if (target == null && sender instanceof Player player) {
            target = player;
        }
        if (target == null) {
            Msg.send(sender, "&cГравець не знайдений.");
            return;
        }
        Arena arena = plugin.getGameManager().getArenaByPlayer(target.getUniqueId());
        if (sender instanceof Player player && arena != null && !isPlayerInArenaWorld(player, arena)) {
            Msg.send(sender, "&cКоманда тільки в арені!");
            return;
        }
        PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());
        Msg.send(sender, "&eСтатистика: &f" + target.getName());
        Msg.send(sender, "&7Перемог Hiden: &f" + stats.getHiderWins());
        Msg.send(sender, "&7Перемог Seek: &f" + stats.getSeekerWins());
        Msg.send(sender, "&7Знайдено Hiden: &f" + stats.getHunterKills());
        Msg.send(sender, "&7Вижито ігор: &f" + stats.getSurvivorGames());
    }

    private void handleSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        if (!player.hasPermission("hiden.spectate")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 2) {
            Msg.send(sender, "&cВикористання: /hiden spectate <arena>");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            Msg.send(sender, "&cАрена не знайдена.");
            return;
        }
        if (arena.getState() != GameState.PREP && arena.getState() != GameState.PLAYING) {
            Msg.send(sender, "&cСпостерігати можна тільки під час гри.");
            return;
        }
        if (!isPlayerInArenaWorld(player, arena)) {
            Msg.send(sender, "&cКоманда тільки в арені!");
            return;
        }
        arena.getParticipants().add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        if (arena.getWaitingLoc() != null) {
            player.teleport(arena.getWaitingLoc());
        }
        plugin.getGameManager().updateSpectatorVisibility(player, arena);
        Msg.send(sender, "&aВи спостерігач.");
    }

    private void handleAchievements(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "&cТільки для гравця.");
            return;
        }
        Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
        if (arena != null && !isPlayerInArenaWorld(player, arena)) {
            Msg.send(sender, "&cКоманда тільки в арені!");
            return;
        }
        PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId());
        Msg.send(sender, "&eДосягнення:");
        Msg.send(sender, formatAchievement("FirstBlood", stats.isFirstBlood()));
        Msg.send(sender, formatAchievement("Survivor (" + stats.getSurvivorGames() + "/10)", stats.isSurvivor()));
        Msg.send(sender, formatAchievement("Hunter (" + stats.getHunterKills() + "/50)", stats.isHunter()));
        Msg.send(sender, formatAchievement("Speedrunner", stats.isSpeedrunner()));
        Msg.send(sender, formatAchievement("Ghost", stats.isGhost()));
    }

    private String formatAchievement(String name, boolean unlocked) {
        return (unlocked ? "&a" : "&c") + name + (unlocked ? " &7(отримано)" : " &7(немає)");
    }

    private void handleLeaderboard(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            Arena arena = plugin.getGameManager().getArenaByPlayer(player.getUniqueId());
            if (arena != null && !isPlayerInArenaWorld(player, arena)) {
                Msg.send(sender, "&cКоманда тільки в арені!");
                return;
            }
        }
        String type = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "total";
        List<Map.Entry<UUID, PlayerStats>> list = new ArrayList<>(plugin.getStatsManager().getAllStats().entrySet());
        Comparator<Map.Entry<UUID, PlayerStats>> comparator = Comparator.comparingInt(entry -> {
            PlayerStats stats = entry.getValue();
            return switch (type) {
                case "hider" -> stats.getHiderWins();
                case "seeker" -> stats.getSeekerWins();
                default -> stats.getHiderWins() + stats.getSeekerWins();
            };
        });
        list.sort(comparator.reversed());
        Msg.send(sender, "&eТоп-10: &f" + type);
        int count = 0;
        for (Map.Entry<UUID, PlayerStats> entry : list) {
            if (count >= 10) {
                break;
            }
            UUID uuid = entry.getKey();
            PlayerStats stats = entry.getValue();
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) {
                name = uuid.toString();
            }
            int value = switch (type) {
                case "hider" -> stats.getHiderWins();
                case "seeker" -> stats.getSeekerWins();
                default -> stats.getHiderWins() + stats.getSeekerWins();
            };
            Msg.send(sender, "&7" + (count + 1) + ". &f" + name + " &7- &e" + value);
            count++;
        }
    }

    private void handleGrantAchievement(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hiden.admin")) {
            Msg.send(sender, "&cНемає прав.");
            return;
        }
        if (args.length < 3) {
            Msg.send(sender, "&cВикористання: /hiden ach <id> <player>");
            return;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            Msg.send(sender, "&cГравець не знайдений.");
            return;
        }
        PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());
        switch (id) {
            case "firstblood" -> stats.setFirstBlood(true);
            case "survivor" -> stats.setSurvivor(true);
            case "hunter" -> stats.setHunter(true);
            case "speedrunner" -> stats.setSpeedrunner(true);
            case "ghost" -> stats.setGhost(true);
            default -> {
                Msg.send(sender, "&cНевідоме досягнення.");
                return;
            }
        }
        plugin.getStatsManager().save();
        Msg.send(sender, "&aДосягнення видано.");
    }

    private boolean isPlayerInArenaWorld(Player player, Arena arena) {
        if (player == null || arena == null) {
            return false;
        }
        if (arena.getWorld() == null) {
            return false;
        }
        return player.getWorld().getName().equalsIgnoreCase(arena.getWorld());
    }
}
