package com.hideandseek.listeners;

import com.hideandseek.GameManager;
import com.hideandseek.GameState;
import com.hideandseek.managers.VoteManager;
import com.hideandseek.model.GamePlayer;
import com.hideandseek.model.GameRole;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Предметное голосование: игрок кликает ПКМ/ЛКМ по предмету карты в инвентаре —
 * это и есть голосование за карту. Реализуется через VoteManager.
 */
public class InventoryListener implements Listener {

    private final GameManager gameManager;

    public InventoryListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Обработка голосования
        if (gameManager.getState() == GameState.VOTING) {
            ItemStack item = event.getItem();
            if (item == null) return;

            String mapId = VoteManager.mapIdForMaterial(item.getType());
            if (mapId == null) return;

            Player player = event.getPlayer();
            event.setCancelled(true); // не даём использовать предмет как обычно
            gameManager.getVoteManager().vote(player, mapId);
        }
        
        // Разрешаем открытие дверей для прячущихся во время игры
        if ((gameManager.getState() == GameState.HIDING || gameManager.getState() == GameState.SEARCHING) &&
            event.getClickedBlock() != null) {
            Player player = event.getPlayer();
            GamePlayer gp = gameManager.getPlayers().get(player.getUniqueId());
            if (gp != null && gp.getRole() == GameRole.HIDER) {
                Material material = event.getClickedBlock().getType();
                if (material.name().contains("DOOR") || 
                    material == Material.LEVER ) {
                    event.setCancelled(false); // Разрешаем взаимодействие
                    return;
                }
            }
        }
    }
}
