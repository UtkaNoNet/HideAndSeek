package com.hideandseek.listeners;

import com.hideandseek.GameManager;
import com.hideandseek.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Если игрок заходит на сервер, пока игра идет, он получает сообщение, что игра уже идет.
 * Если игра не идет, он телепортируется на хаб или на дефолтные координаты.
 */
public class JoinListener implements Listener {

    private final GameManager gameManager;

    public JoinListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (gameManager.getState() != GameState.IDLE) {
            event.getPlayer().sendMessage("Игра уже идет, ты сможешь присоединиться к следующему раунду.");
            
            // Телепортируем на хаб или на дефолтные координаты
            if (gameManager.getPluginInstance().getConfigUtil().isHubSet()) {
                player.teleport(gameManager.getPluginInstance().getConfigUtil().getHub());
            } else {
                // Дефолтные координаты: -1997 107 -1992
                Location defaultSpawn = new Location(
                    Bukkit.getWorlds().get(0),
                    -1997.0,
                    107.0,
                    -1992.0
                );
                player.teleport(defaultSpawn);
            }
        } else {
            // Если игра не идет, телепортируем на хаб или на дефолтные координаты
            if (gameManager.getPluginInstance().getConfigUtil().isHubSet()) {
                player.teleport(gameManager.getPluginInstance().getConfigUtil().getHub());
            } else {
                // Дефолтные координаты: -1997 107 -1992
                Location defaultSpawn = new Location(
                    Bukkit.getWorlds().get(0),
                    -1997.0,
                    107.0,
                    -1992.0
                );
                player.teleport(defaultSpawn);
            }
        }
    }
}
