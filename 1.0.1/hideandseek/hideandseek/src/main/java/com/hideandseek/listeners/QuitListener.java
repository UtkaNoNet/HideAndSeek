package com.hideandseek.listeners;

import com.hideandseek.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Правила выхода из ТЗ:
 *  - прячущийся вышел -> считается пойманным/погибшим;
 *  - все прячущиеся вышли -> победа искателя;
 *  - искатель вышел -> игра сразу заканчивается, все возвращаются в хаб.
 * Вся логика — в GameManager.handleQuit(), тут только передача события.
 */
public class QuitListener implements Listener {

    private final GameManager gameManager;

    public QuitListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gameManager.handleQuit(event.getPlayer());
    }
}
