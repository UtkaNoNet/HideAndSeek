package com.hideandseek.commands;

import com.hideandseek.utils.ConfigUtil;
import com.hideandseek.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /debug on|off — включает вывод отладочных [DEBUG] сообщений.
 * Их видят только игроки с правом hideandseek.admin (см. MessageUtil.debug()).
 */
public class DebugCommand implements CommandExecutor {

    private final ConfigUtil configUtil;

    public DebugCommand(ConfigUtil configUtil) {
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off"))) {
            sender.sendMessage("Использование: /debug <on|off>");
            return true;
        }

        boolean enabled = args[0].equalsIgnoreCase("on");
        configUtil.setDebug(enabled);
        MessageUtil.setDebugEnabled(enabled);
        sender.sendMessage("Debug mode: " + (enabled ? "включён" : "выключен"));
        return true;
    }
}
