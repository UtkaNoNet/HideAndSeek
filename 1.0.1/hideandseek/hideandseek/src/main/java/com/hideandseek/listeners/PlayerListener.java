package com.hideandseek.listeners;

import com.hideandseek.GameManager;
import com.hideandseek.GameState;
import com.hideandseek.model.GamePlayer;
import com.hideandseek.model.GameRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Общие правила поведения во время игры, не связанные с уроном/выходом:
 *  - прячущиеся не теряют голод (чтобы не тратить время на еду);
 *  - при смерти прячущегося он не теряет предметы и не показывает сообщение о смерти.
 */
public class PlayerListener implements Listener {

    private final GameManager gameManager;

    public PlayerListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (gameManager.getState() == GameState.IDLE) return;
        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
        if (gp != null && gp.getRole() != GameRole.NONE) {
            event.setCancelled(true); // голод не важен в мини-игре
        }
    }

    /**
     * На смерть прячущегося: не показываем сообщение, не роняем предметы, переводим в режим зрителей.
     * Искатель при смерти: игра заканчивается победой прячущихся.
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
        if (gp == null) return;

        if (gp.getRole() == GameRole.HIDER) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
            gameManager.catchHider(player);

            // Принудительно респавним игрока
            Bukkit.getScheduler().runTask(gameManager.getPluginInstance(), () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                }
            });
        } else if (gp.getRole() == GameRole.SEEKER) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
            gameManager.handleSeekerDeath(player);
            
            // Принудительно респавним игрока
            Bukkit.getScheduler().runTask(gameManager.getPluginInstance(), () -> {
                if (player.isOnline()) {
                    player.spigot().respawn();
                }
            });
        }
    }

    // Разрешаем открытие дверей для прячущихся
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (gameManager.getState() != GameState.HIDING && gameManager.getState() != GameState.SEARCHING) return;
        
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
        if (gp == null || gp.getRole() != GameRole.HIDER) return;
        
        Material material = event.getClickedBlock().getType();
        if (material.name().contains("DOOR") || 
            material == Material.LEVER ||
            material == Material.IRON_DOOR) {
            event.setCancelled(false); // Разрешаем взаимодействие
            return;
        }
        
        // Для остальных блоков - отменяем взаимодействие
        event.setCancelled(true);
    }
}
