package com.yourname.hiden.commands;

import com.yourname.hiden.Hiden;
import com.yourname.hiden.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HidenTabCompleter implements TabCompleter {
    private final Hiden plugin;

    public HidenTabCompleter(Hiden plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.add("join");
            suggestions.add("leave");
            suggestions.add("stats");
            suggestions.add("spectate");
            suggestions.add("achievements");
            suggestions.add("leaderboard");
            if (sender.hasPermission("hiden.admin")) {
                suggestions.add("create");
                suggestions.add("remove");
                suggestions.add("arena");
                suggestions.add("list");
                suggestions.add("tp");
                suggestions.add("start");
                suggestions.add("ach");
            }
            return suggestions;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("join") || sub.equals("spectate") || sub.equals("tp") || sub.equals("start")) {
                for (Arena arena : plugin.getArenaManager().getArenas()) {
                    suggestions.add(arena.getName());
                }
            }
            if (sub.equals("leaderboard")) {
                suggestions.add("hider");
                suggestions.add("seeker");
                suggestions.add("total");
            }
            if (sub.equals("create") || sub.equals("remove") || sub.equals("list")) {
                suggestions.add("arena");
            }
            if (sub.equals("arena")) {
                for (Arena arena : plugin.getArenaManager().getArenas()) {
                    suggestions.add(arena.getName());
                }
            }
            if (sub.equals("ach")) {
                suggestions.add("firstblood");
                suggestions.add("survivor");
                suggestions.add("hunter");
                suggestions.add("speedrunner");
                suggestions.add("ghost");
            }
            return suggestions;
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("join")) {
                suggestions.add("team");
            }
            if (sub.equals("arena")) {
                suggestions.add("setwarp");
                suggestions.add("removewarp");
                suggestions.add("setting");
                suggestions.add("setname");
            }
            if (sub.equals("tp")) {
                suggestions.add("hiden");
                suggestions.add("speaker");
                suggestions.add("waiting");
            }
        }
        if (args.length == 4) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("join") && args[2].equalsIgnoreCase("team")) {
                suggestions.add("hiden");
                suggestions.add("speaker");
            }
            if (args[0].equalsIgnoreCase("arena")) {
                String action = args[2].toLowerCase(Locale.ROOT);
                if (action.equals("setwarp") || action.equals("removewarp")) {
                    suggestions.add("hiden");
                    suggestions.add("speaker");
                    suggestions.add("waiting");
                }
            }
        }
        return suggestions;
    }
}
