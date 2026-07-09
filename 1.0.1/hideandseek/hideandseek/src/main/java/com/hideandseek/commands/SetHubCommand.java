package com.hideandseek.commands;

import com.hideandseek.utils.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /sethub — сохраняет текущую позицию игрока как точку хаба в config.yml. */
public class SetHubCommand implements CommandExecutor {

    private final ConfigUtil configUtil;

    public SetHubCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эту команду можно выполнить только в игре.");
            return true;
        }
        configUtil.setHub(player.getLocation());
        sender.sendMessage("Хаб установлен в твоей текущей позиции.");
        return true;
    }
}
