package com.hideandseek.commands;

import com.hideandseek.utils.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Один класс обслуживает все три команды (/setforest, /setdesert, /setswamp).
 * В Main.java при регистрации в конструктор передаётся id карты,
 * так что не нужно дублировать этот класс три раза.
 */
public class SetMapCommand implements CommandExecutor {

    private final ConfigUtil configUtil;
    private final String mapId;

    public SetMapCommand(ConfigUtil configUtil, String mapId) {
        this.configUtil = configUtil;
        this.mapId = mapId;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эту команду можно выполнить только в игре.");
            return true;
        }
        configUtil.setMap(mapId, player.getLocation());
        sender.sendMessage("Карта \"" + configUtil.getMapName(mapId) + "\" установлена в твоей текущей позиции.");
        return true;
    }
}
