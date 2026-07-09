package com.hideandseek.commands;

import com.hideandseek.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** /stop — принудительно останавливает текущую игру и сбрасывает всё в исходное состояние. */
public class StopCommand implements CommandExecutor {

    private final GameManager gameManager;

    public StopCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        gameManager.stopGame("остановлено администратором.");
        sender.sendMessage("Игра остановлена.");
        return true;
    }
}
