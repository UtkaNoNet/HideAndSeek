package com.hideandseek.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Централизованные методы вывода сообщений:
 * - тайтлы по центру экрана (роль игрока, победа и т.д.)
 * - обычные сообщения в чат
 * - debug-сообщения, которые видит ТОЛЬКО игрок с правом hideandseek.admin
 *   и только если /debug on включён.
 */
public class MessageUtil {

    private static boolean debugEnabled = false;

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /** Показать тайтл конкретному игроку (роль искатель/прячущийся). */
    public static void title(Player player, String title, String subtitle, ChatColor color) {
        player.sendTitle(color + title, subtitle, 10, 60, 10);
    }

    /** Показать тайтл всем онлайн игрокам (например, объявление победителя). */
    public static void broadcastTitle(String title, String subtitle, ChatColor color) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(color + title, subtitle, 10, 70, 20);
        }
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[HideAndSeek] " + ChatColor.RESET + message);
    }

    public static void send(Player player, String message) {
        player.sendMessage(ChatColor.GOLD + "[HideAndSeek] " + ChatColor.RESET + message);
    }

    /**
     * Debug-лог. Вызывается из любого места кода когда меняется важное состояние.
     * Виден только игрокам с правом hideandseek.admin, и только если debug включён.
     */
    public static void debug(String message) {
        if (!debugEnabled) return;
        String line = ChatColor.GRAY + "[DEBUG] " + ChatColor.YELLOW + message;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("hideandseek.admin")) {
                p.sendMessage(line);
            }
        }
        Bukkit.getLogger().info("[HideAndSeek DEBUG] " + message);
    }
}
