package com.hideandseek.managers;

import com.hideandseek.model.GamePlayer;
import com.hideandseek.model.GameRole;
import com.hideandseek.utils.ConfigUtil;
import com.hideandseek.utils.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Отвечает за:
 *  - случайный выбор ровно одного игрока на роль искателя (все остальные игроки получают роль прячущегося),
 *    вызов происходит один раз в начале игры, когда известны все игроки;
 *  - показ тайтлов с ролью;
 *  - выдачу стартовых предметов/характеристик (лук искателя, 1 сердечко прячущимся).
 */
public class RoleManager {

    private final ConfigUtil configUtil;
    private final Random random = new Random();

    public RoleManager(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    /**
     * Шаг 4-5 игрового цикла: делит игроков на роли.
     * Вызывается один раз в начале игры, когда известны все игроки.
     * Распределяет роли случайным образом (каждый игрок имеет равный шанс стать искателем).
     */
    public void assignRoles(Map<java.util.UUID, GamePlayer> players, List<Player> onlinePlayers) {
        int seekerIndex = random.nextInt(onlinePlayers.size());
        Player seeker = onlinePlayers.get(seekerIndex);

        for (Player p : onlinePlayers) {
            GamePlayer gp = players.get(p.getUniqueId());
            if (p.equals(seeker)) {
                gp.setRole(GameRole.SEEKER);
            } else {
                gp.setRole(GameRole.HIDER);
            }
            gp.setAlive(true);
        }

        MessageUtil.debug("Искатель: " + seeker.getName());
    }

    /** Показывает тайтл с ролью игроку. */
    public void announceRole(Player player, GameRole role) {
        if (role == GameRole.SEEKER) {
            MessageUtil.title(player, "Ты искатель!", "Найди всех прячущихся", ChatColor.RED);
        } else if (role == GameRole.HIDER) {
            MessageUtil.title(player, "Ты прячешься!", "Спрячься получе", ChatColor.BLUE);
        }
    }

    /** Выдаёт стартовые предметы искателю (лук с чарами и стрелы). */
    public void giveStartingKit(Player player, GameRole role) {
        player.getInventory().clear();
        
        // Устанавливаем режим приключения
        player.setGameMode(GameMode.ADVENTURE);
        
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);

        if (role == GameRole.SEEKER) {
            ItemStack bow = new ItemStack(Material.BOW);
            bow.addUnsafeEnchantment(Enchantment.POWER, configUtil.getSeekerBowPower()); // Сила 5
            bow.addUnsafeEnchantment(Enchantment.INFINITY, 1); // Бесконечность
            player.getInventory().addItem(bow);
            player.getInventory().addItem(new ItemStack(Material.ARROW, configUtil.getSeekerArrows()));
        }
    }

    /** Красим ник игрока в чате в зависимости от роли (красный/синий). */
    public String colorNameForRole(String name, GameRole role) {
        if (role == GameRole.SEEKER) return ChatColor.RED + name;
        if (role == GameRole.HIDER) return ChatColor.BLUE + name;
        return name;
    }
}
