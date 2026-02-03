package com.yourname.hiden.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Msg {
    public static final String PREFIX = "&6[Hiden]&r ";
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .build();

    public static Component colorize(String text) {
        return SERIALIZER.deserialize(text);
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + message));
    }

    public static void actionBar(Player player, String message) {
        player.sendActionBar(colorize(PREFIX + message));
    }
}
