package com.hideandseek.utils;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Утилита для чтения/записи config.yml.
 * Инкапсулирует пути вида "maps.forest.x" и т.п.,
 * чтобы остальной код не ломался при изменении структуры конфигурации.
 */
public class ConfigUtil {

    private final JavaPlugin plugin;

    public ConfigUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration config() {
        return plugin.getConfig();
    }

    public void save() {
        plugin.saveConfig();
    }

    /** Сохраняет текущую позицию как хаб (команда /sethub). */
    public void setHub(Location loc) {
        config().set("hub.set", true);
        config().set("hub.world", loc.getWorld().getName());
        config().set("hub.x", loc.getX());
        config().set("hub.y", loc.getY());
        config().set("hub.z", loc.getZ());
        save();
    }

    public boolean isHubSet() {
        return config().getBoolean("hub.set", false);
    }

    public Location getHub() {
        return LocationUtil.fromConfig(config(), "hub");
    }

    /** Сохраняет текущую позицию как одну из карт (forest/desert/swamp). */
    public void setMap(String mapId, Location loc) {
        String path = "maps." + mapId;
        config().set(path + ".set", true);
        config().set(path + ".world", loc.getWorld().getName());
        config().set(path + ".x", loc.getX());
        config().set(path + ".y", loc.getY());
        config().set(path + ".z", loc.getZ());
        save();
    }

    public boolean isMapSet(String mapId) {
        return config().getBoolean("maps." + mapId + ".set", false);
    }

    public String getMapName(String mapId) {
        return config().getString("maps." + mapId + ".name", mapId);
    }

    public Location getMapLocation(String mapId) {
        return LocationUtil.fromConfig(config(), "maps." + mapId);
    }

    public int getMinPlayers() {
        return config().getInt("settings.min-players", 2);
    }

    public int getVoteTime() {
        return config().getInt("settings.vote-time-seconds", 15);
    }

    public int getHidingTime() {
        return config().getInt("settings.hiding-time-seconds", 60);
    }

    public int getGameTime() {
        return config().getInt("settings.game-time-seconds", 420);
    }

    public int getEndTime() {
        return config().getInt("settings.end-time-seconds", 20);
    }

    public int getSeekerBowPower() {
        return config().getInt("settings.seeker-bow-power", 5);
    }

    public int getSeekerArrows() {
        return config().getInt("settings.seeker-arrows", 1);
    }

    public int getHiderHearts() {
        return config().getInt("settings.hider-hearts", 1);
    }

    public boolean isDebug() {
        return config().getBoolean("debug", false);
    }

    public void setDebug(boolean value) {
        config().set("debug", value);
        save();
    }

    // Получаем дефолтные координаты спавна
    public Location getDefaultSpawn() {
        return LocationUtil.fromConfig(config(), "default-spawn");
    }
}
