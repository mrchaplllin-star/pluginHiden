package com.yourname.hiden.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

public class SafeTeleport {
    public static Location findSafe(Location base) {
        if (base == null) {
            return null;
        }
        World world = base.getWorld();
        if (world == null) {
            return base;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 20; i++) {
            double offsetX = random.nextDouble(-20, 21);
            double offsetZ = random.nextDouble(-20, 21);
            int x = (int) Math.floor(base.getX() + offsetX);
            int z = (int) Math.floor(base.getZ() + offsetZ);
            int y = world.getHighestBlockYAt(x, z) + 1;
            Location candidate = new Location(world, x + 0.5, y, z + 0.5, base.getYaw(), base.getPitch());
            if (isSafe(candidate)) {
                return candidate;
            }
        }
        return base;
    }

    private static boolean isSafe(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        Block block = world.getBlockAt(location);
        Block blockAbove = world.getBlockAt(location.clone().add(0, 1, 0));
        Block blockBelow = world.getBlockAt(location.clone().add(0, -1, 0));
        if (!block.isEmpty() || !blockAbove.isEmpty()) {
            return false;
        }
        if (blockBelow.getType() == Material.LAVA || blockBelow.getType() == Material.FIRE) {
            return false;
        }
        return blockBelow.getType().isSolid();
    }
}
