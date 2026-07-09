package com.hideandseek.managers;

import com.hideandseek.model.GameMap;
import com.hideandseek.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Отвечает за список доступных карт (forest/desert/swamp).
 * Карта считается "доступной", только если её координаты были
 * сохранены через /setforest /setdesert /setswamp (флаг set: true в config.yml).
 */
public class MapManager {

    private static final String[] MAP_IDS = {"forest", "desert", "swamp"};

    private final ConfigUtil configUtil;

    public MapManager(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    /** Список карт, которые реально настроены админом. */
    public List<GameMap> getAvailableMaps() {
        List<GameMap> maps = new ArrayList<>();
        for (String id : MAP_IDS) {
            if (configUtil.isMapSet(id)) {
                maps.add(new GameMap(id, configUtil.getMapName(id), configUtil.getMapLocation(id)));
            }
        }
        return maps;
    }

    public boolean hasAnyMap() {
        return !getAvailableMaps().isEmpty();
    }

    public GameMap getMap(String id) {
        if (!configUtil.isMapSet(id)) return null;
        return new GameMap(id, configUtil.getMapName(id), configUtil.getMapLocation(id));
    }
}
