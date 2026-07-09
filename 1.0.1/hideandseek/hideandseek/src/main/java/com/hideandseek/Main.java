package com.hideandseek;

import com.hideandseek.commands.*;
import com.hideandseek.listeners.*;
import com.hideandseek.utils.ConfigUtil;
import com.hideandseek.utils.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Точка входа плагина. Тут и только тут делается:
 *  - saveDefaultConfig()
 *  - создание GameManager (единственный на весь плагин, хранит состояние игры)
 *  - регистрация команд (executor'ов) и слушателей событий
 *
 * Ничего игрового тут не происходит — вся логика в GameManager/managers/listeners.
 */
public class Main extends JavaPlugin {

    private GameManager gameManager;
    private ConfigUtil configUtil;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configUtil = new ConfigUtil(this);
        MessageUtil.setDebugEnabled(configUtil.isDebug());

        this.gameManager = new GameManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("HideAndSeek включен.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null && gameManager.getState() != GameState.IDLE) {
            gameManager.stopGame("плагин выключается.");
        }
        getLogger().info("HideAndSeek выключен.");
    }

    private void registerCommands() {
        getCommand("start").setExecutor(new StartCommand(gameManager));
        getCommand("stop").setExecutor(new StopCommand(gameManager));
        getCommand("sethub").setExecutor(new SetHubCommand(configUtil));
        getCommand("setforest").setExecutor(new SetMapCommand(configUtil, "forest"));
        getCommand("setdesert").setExecutor(new SetMapCommand(configUtil, "desert"));
        getCommand("setswamp").setExecutor(new SetMapCommand(configUtil, "swamp"));
        getCommand("debug").setExecutor(new DebugCommand(configUtil));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new DamageListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new QuitListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(gameManager), this);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }
}
