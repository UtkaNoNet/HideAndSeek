package com.hideandseek.commands;

import com.hideandseek.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /start — запускает игру. Вся проверка условий и логика — в GameManager.startGame().
 * Эта команда — просто "переходник" от Bukkit к игровой логике.
 */
public class StartCommand implements CommandExecutor {

    private final GameManager gameManager;

    public StartCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        gameManager.startGame(sender);
        return true;
    }
}
