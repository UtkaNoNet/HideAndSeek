package com.hideandseek.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Вспомогательные методы для сборки Location из секции конфига вида:
 * path.world / path.x / path.y / path.z
 */
public class LocationUtil {

    public static Location fromConfig(FileConfiguration config, String path) {
        String worldName = config.getString(path + ".world", "world");
        World world = Bukkit.getWorld(worldName);
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        return new Location(world, x, y, z);
    }
}
