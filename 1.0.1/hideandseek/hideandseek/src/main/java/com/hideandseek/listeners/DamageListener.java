package com.hideandseek.listeners;

import com.hideandseek.GameManager;
import com.hideandseek.GameState;
import com.hideandseek.model.GamePlayer;
import com.hideandseek.model.GameRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * У прячущегося всего 1 сердечко (2 HP), поэтому любой урон убивает.
 * Его не "убивает" в классическом смысле, а переводит в режим зрителей (GameManager.catchHider).
 * 
 * Так работает и урон от кактусов/лавы, и от стрел искателя.
 * Искатель может умирать от урона - в этом случае победа засчитывается прячущимся.
 */
public class DamageListener implements Listener {

    private final GameManager gameManager;

    public DamageListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (gameManager.getState() != GameState.SEARCHING && gameManager.getState() != GameState.HIDING) return;
        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
        if (gp == null) return;

        // Если это искатель и он получает смертельный урон
        if (gp.getRole() == GameRole.SEEKER) {
            if (event.getFinalDamage() >= player.getHealth()) {
                event.setCancelled(true);
                gameManager.handleSeekerDeath(player);
            }
            return;
        }

        // Если это прячущийся
        if (gp.getRole() == GameRole.HIDER && gp.isAlive()) {
            if (event.getFinalDamage() >= player.getHealth()) {
                event.setCancelled(true);
                gameManager.catchHider(player);
            }
        }
    }

    // Разрешаем открытие дверей для прячущихся
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (gameManager.getState() != GameState.HIDING && gameManager.getState() != GameState.SEARCHING) return;
        
        Player player = event.getPlayer();
        GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
        if (gp == null || gp.getRole() != GameRole.HIDER) return;
        
        // Разрешаем взаимодействие с дверями
        if (event.getClickedBlock() != null) {
            Material material = event.getClickedBlock().getType();
            if (material.name().contains("DOOR") || 
                material == Material.LEVER) {
                event.setCancelled(false); // Разрешаем взаимодействие
                return;
            }
        }
        
        // Для остальных блоков - отменяем взаимодействие (чтобы не ставили блоки)
        if (event.getClickedBlock() != null) {
            event.setCancelled(true);
        }
    }
}
