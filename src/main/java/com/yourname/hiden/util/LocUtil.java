package com.yourname.hiden.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocUtil {
    public static String serialize(Location location) {
        if (location == null) {
            return null;
        }
        return "world=" + location.getWorld().getName()
                + ";x=" + location.getX()
                + ";y=" + location.getY()
                + ";z=" + location.getZ()
                + ";yaw=" + location.getYaw()
                + ";pitch=" + location.getPitch();
    }

    public static Location deserialize(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] parts = value.split(";");
        String worldName = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "world" -> worldName = kv[1];
                case "x" -> x = Double.parseDouble(kv[1]);
                case "y" -> y = Double.parseDouble(kv[1]);
                case "z" -> z = Double.parseDouble(kv[1]);
                case "yaw" -> yaw = Float.parseFloat(kv[1]);
                case "pitch" -> pitch = Float.parseFloat(kv[1]);
                default -> {
                }
            }
        }
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}
