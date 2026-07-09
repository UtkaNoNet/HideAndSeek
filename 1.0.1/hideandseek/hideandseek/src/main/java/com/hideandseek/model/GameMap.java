package com.hideandseek.model;

import org.bukkit.Location;

/**
 * Модель карты (локации).
 * Хранит человекочитаемое имя карты и точку телепортации.
 * Заполняется из config.yml менеджером MapManager.
 */
public class GameMap {

    private final String id;       // forest / desert / swamp
    private final String name;     // "Лесной приют" и т.п.
    private final Location location;

    public GameMap(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
