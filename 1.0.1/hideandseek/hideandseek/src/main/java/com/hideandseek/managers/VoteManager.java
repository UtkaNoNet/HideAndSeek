package com.hideandseek.managers;

import com.hideandseek.model.GameMap;
import com.hideandseek.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Предметное голосование за карту: каждый доступный предмет в инвентаре соответствует
 * одной карте (лесной саженец = лес, песок = пустыня, кувшинка = болото).
 * Выбор фиксируется в vote(...) отсюда.
 *
 * При равном количестве голосов — случайный выбор среди лидеров.
 */
public class VoteManager {

    // material -> mapId, чтобы InventoryListener мог понять, за что голосовать
    private static final Map<Material, String> VOTE_ITEMS = new HashMap<>();
    static {
        VOTE_ITEMS.put(Material.OAK_SAPLING, "forest");   // Лесной приют
        VOTE_ITEMS.put(Material.SAND, "desert");          // Пепельные пески
        VOTE_ITEMS.put(Material.LILY_PAD, "swamp");       // Плачущие топи
    }

    private final Map<UUID, String> votes = new HashMap<>();
    private final Random random = new Random();

    public void reset() {
        votes.clear();
    }

    public static String mapIdForMaterial(Material material) {
        return VOTE_ITEMS.get(material);
    }

    /** Выдаёт игроку предметы голосования по одной на каждую доступную карту. */
    public void giveVotingItems(Player player, List<GameMap> maps) {
        for (GameMap map : maps) {
            Material material = materialFor(map.getId());
            if (material == null) continue;
            ItemStack item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(map.getName());
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
        }
    }

    private Material materialFor(String mapId) {
        for (Map.Entry<Material, String> e : VOTE_ITEMS.entrySet()) {
            if (e.getValue().equals(mapId)) return e.getKey();
        }
        return null;
    }

    /** Фиксирует голос игрока за карту. Можно голосовать только один раз. */
    public void vote(Player player, String mapId) {
        UUID uuid = player.getUniqueId();
        
        // Проверяем, голосовал ли игрок уже
        if (votes.containsKey(uuid)) {
            MessageUtil.send(player, "Ты уже проголосовал за карту: " + votes.get(uuid));
            return;
        }
        
        votes.put(uuid, mapId);
        MessageUtil.send(player, "Ты проголосовал за карту: " + mapId);
    }

    /** Подсчитывает голоса и возвращает id победившей карты. При ничьей — случайный выбор среди лидеров. */
    public String decideWinner(List<GameMap> availableMaps) {
        Map<String, Integer> counts = new HashMap<>();
        for (GameMap m : availableMaps) counts.put(m.getId(), 0);

        for (String mapId : votes.values()) {
            counts.merge(mapId, 1, Integer::sum);
        }

        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        List<String> topMaps = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() == max) topMaps.add(e.getKey());
        }

        // Если никто не голосовал (max == 0) — выбираем случайно из всех доступных
        String winner = topMaps.get(random.nextInt(topMaps.size()));
        return winner;
    }
}
