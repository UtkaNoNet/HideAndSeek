package com.hideandseek;

import com.hideandseek.managers.*;
import com.hideandseek.model.GameMap;
import com.hideandseek.model.GamePlayer;
import com.hideandseek.model.GameRole;
import com.hideandseek.utils.ConfigUtil;
import com.hideandseek.utils.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Центральный класс плагина. Хранит текущее состояние игры (GameState)
 * и последовательно ведёт её по шагам 1-18 из ТЗ:
 * хаб -> /start -> роли -> голосование -> прятки -> поиск -> конец -> сброс.
 *
 * Все остальные менеджеры (Vote/Timer/Role/Map/Scoreboard/Visibility)
 * вызываются отсюда, сами по себе они ничего не решают без вызова из GameManager.
 */
public class GameManager {

    private final Main plugin;
    private final ConfigUtil configUtil;
    private final MapManager mapManager;
    private final RoleManager roleManager;
    private final VoteManager voteManager;
    private final TimerManager timerManager;
    private final ScoreboardManager scoreboardManager;
    private final VisibilityManager visibilityManager;

    private GameState state = GameState.IDLE;
    private final Map<UUID, GamePlayer> players = new HashMap<>();
    private List<GameMap> roundMaps = new ArrayList<>();
    private GameMap chosenMap;
    private UUID seekerUuid;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        this.configUtil = plugin.getConfigUtil();
        this.mapManager = new MapManager(configUtil);
        this.roleManager = new RoleManager(configUtil);
        this.voteManager = new VoteManager();
        this.timerManager = new TimerManager(plugin);
        this.scoreboardManager = new ScoreboardManager();
        this.visibilityManager = new VisibilityManager();
    }

    public GameState getState() {
        return state;
    }

    public Main getPluginInstance() {
        return plugin;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    public Map<UUID, GamePlayer> getPlayers() {
        return players;
    }

    // =========================================================
    //  Шаг 2-4: /start
    // =========================================================
    public void startGame(CommandSender sender) {
        if (state != GameState.IDLE) {
            sender.sendMessage(ChatColor.RED + "Игра уже идет.");
            return;
        }
        if (!configUtil.isHubSet()) {
            sender.sendMessage(ChatColor.RED + "Хаб не настроен.");
            return;
        }
        if (!mapManager.hasAnyMap()) {
            sender.sendMessage(ChatColor.RED + "Нет доступных карт.");
            return;
        }

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.size() < configUtil.getMinPlayers()) {
            sender.sendMessage(ChatColor.RED + "Невозможно начать игру.");
            return;
        }

        // Инициализация игрового состояния для всех текущих онлайн игроков
        players.clear();
        for (Player p : online) {
            players.put(p.getUniqueId(), new GamePlayer(p.getUniqueId()));
        }

        roundMaps = mapManager.getAvailableMaps();

        setState(GameState.ROLE_ASSIGN);
        assignRolesPhase(online);
    }

    // =========================================================
    //  Шаг 4-7: деление на роли + тайтлы + предметы
    //  (сначала выбираем случайного искателя, затем показываем роли и выдаем стартовые предметы)
    // =========================================================
    private void assignRolesPhase(List<Player> online) {
        roleManager.assignRoles(players, online);

        for (Player p : online) {
            GameRole role = players.get(p.getUniqueId()).getRole();
            if (role == GameRole.SEEKER) seekerUuid = p.getUniqueId();

            roleManager.announceRole(p, role);
            // Убираем выдачу предметов здесь - будет после голосования
        }

        scoreboardManager.setup();
        visibilityManager.hideAllNameTags();

        // Небольшая пауза перед голосованием, чтобы игроки увидели свои роли
        Bukkit.getScheduler().runTaskLater(plugin, this::startVotingPhase, 40L); // 2 сек
    }

    // =========================================================
    //  Шаг 8-9: голосование за карту
    // =========================================================
    private void startVotingPhase() {
        setState(GameState.VOTING);
        voteManager.reset();

        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.getInventory().clear();
            voteManager.giveVotingItems(p, roundMaps);
            MessageUtil.send(p, "Выбери карту, кликнув по предмету в инвентаре!");
        }

        timerManager.start(configUtil.getVoteTime(),
                secondsLeft -> { /* можно показывать таймер голосования в actionbar при желании */ },
                this::finishVoting);
    }

    private void finishVoting() {
        String winnerId = voteManager.decideWinner(roundMaps);
        chosenMap = mapManager.getMap(winnerId);
        MessageUtil.debug("Победила карта: " + chosenMap.getName());
        MessageUtil.broadcast("Победила карта: " + ChatColor.AQUA + chosenMap.getName());
        
        // Выдаем роли и предметы ПОСЛЕ голосования
        assignRolesAndItemsAfterVoting();
        
        startHidingPhase();
    }

    // Выдача ролей и предметов после голосования
    private void assignRolesAndItemsAfterVoting() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        // Перераспределяем роли (на случай, если игроки вышли/зашли во время голосования)
        roleManager.assignRoles(players, online);
        
        for (Player p : online) {
            GamePlayer gp = players.get(p.getUniqueId());
            if (gp == null) continue;
            
            GameRole role = gp.getRole();
            if (role == GameRole.SEEKER) {
                seekerUuid = p.getUniqueId();
            }
            
            roleManager.announceRole(p, role);
            
            // Устанавливаем режим приключения по стандарту
            p.setGameMode(GameMode.ADVENTURE);
            
            // Для прячущихся: устанавливаем 1 ХП (максимальное и текущее)
            if (role == GameRole.HIDER) {
                p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(1.0);
                p.setHealth(1.0);
                
                // Добавляем тег "speed_used" всем прячущимся
                p.setMetadata("speed_used", new FixedMetadataValue(plugin, true));
            }
        }
        
        // Выдаем предметы искателю ПОСЛЕ голосования
        Player seeker = Bukkit.getPlayer(seekerUuid);
        if (seeker != null) {
            roleManager.giveStartingKit(seeker, GameRole.SEEKER);
        }
    }

    // =========================================================
    //  Шаг 10-12: телепорт прячущихся, 60 сек на прятки, телепорт искателя
    // =========================================================
    private void startHidingPhase() {
        setState(GameState.HIDING);

        for (Map.Entry<UUID, GamePlayer> entry : players.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;
            p.getInventory().clear(); // убираем предметы голосования
            
            if (entry.getValue().getRole() == GameRole.HIDER) {
                p.teleport(chosenMap.getLocation());
                
                // Устанавливаем погоду и время для карты
                applyMapWeatherAndTime(chosenMap.getId());
            }
            // искатель пока остаётся в хабе
        }

        // Запускаем таймер на время пряток с оповещениями
        timerManager.start(configUtil.getHidingTime(),
                secondsLeft -> {
                    if (secondsLeft == configUtil.getHidingTime()) {
                        MessageUtil.debug("GameState -> HIDING (" + configUtil.getHidingTime() + " сек. на укрытие)");
                    }
                    
                    // Оповещения о оставшемся времени (каждую минуту и последние 10 секунд)
                    if (secondsLeft % 60 == 0 || secondsLeft <= 10) {
                        int minutesLeft = secondsLeft / 60;
                        int secsLeft = secondsLeft % 60;
                        String timeMessage;
                        if (secondsLeft <= 10) {
                            timeMessage = ChatColor.YELLOW + "Осталось: " + secondsLeft + " сек.";
                        } else {
                            timeMessage = ChatColor.YELLOW + "Осталось: " + minutesLeft + " мин. " + secsLeft + " сек.";
                        }
                        MessageUtil.broadcast(timeMessage);
                    }
                },
                this::startSearchingPhase);
    }

    // Применяем погоду и время в зависимости от карты
    private void applyMapWeatherAndTime(String mapId) {
        World world = chosenMap.getLocation().getWorld();
        if (world == null) return;
        
        switch (mapId) {
            case "swamp":
                // На болоте: ночь и дождь
                world.setTime(18000); // Ночь
                world.setStorm(true);
                world.setThundering(false);
                break;
            case "desert":
                // На песках: солнце в зените без осадков
                world.setTime(6000); // Полдень
                world.setStorm(false);
                world.setThundering(false);
                break;
            case "forest":
                // На лесах: закат, без осадков
                world.setTime(12000); // Закат
                world.setStorm(false);
                world.setThundering(false);
                break;
            default:
                // По умолчанию: день
                world.setTime(1000); // Утро
                world.setStorm(false);
                world.setThundering(false);
                break;
        }
    }

    // =========================================================
    //  Шаг 13: основная фаза игры (7 минут)
    // =========================================================
    private void startSearchingPhase() {
        setState(GameState.SEARCHING);

        Player seeker = Bukkit.getPlayer(seekerUuid);
        if (seeker != null) {
            seeker.teleport(chosenMap.getLocation());
            MessageUtil.broadcast(ChatColor.RED + seeker.getName() + ChatColor.RESET + " телепортирован на карту. Игра началась!");
        }

        int totalTime = configUtil.getGameTime();
        timerManager.start(totalTime, secondsLeft -> onSearchingTick(secondsLeft, totalTime), this::onTimeUp);
    }

    private void onSearchingTick(int secondsLeft, int totalTime) {
        scoreboardManager.updateTime(secondsLeft, countAliveHiders());

        // Раз в минуту — свечение прячущихся на 0.5 сек + напоминание о времени
        if (secondsLeft % 60 == 0) {
            applyGlowToHiders();
            int minutesLeft = secondsLeft / 60;
            MessageUtil.broadcast("Осталось времени: " + minutesLeft + " мин.");
        }

        // Напоминания на 30 секунд и последние 10 секунд
        if (secondsLeft == 30) {
            MessageUtil.broadcast(ChatColor.YELLOW + "Осталось 30 секунд!");
        } else if (secondsLeft <= 10 && secondsLeft >= 1) {
            MessageUtil.broadcast(ChatColor.YELLOW + "" + secondsLeft);
        }
    }

    private void applyGlowToHiders() {
        for (Map.Entry<UUID, GamePlayer> entry : players.entrySet()) {
            if (entry.getValue().getRole() == GameRole.HIDER && entry.getValue().isAlive()) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    // 0.5 секунд = 10 тиков
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10, 0, false, false));
                }
            }
        }
    }

    private int countAliveHiders() {
        int count = 0;
        for (GamePlayer gp : players.values()) {
            if (gp.getRole() == GameRole.HIDER && gp.isAlive()) count++;
        }
        return count;
    }

    // =========================================================
    //  Поимка прячущегося (вызывается из DamageListener/QuitListener)
    // =========================================================
    public void catchHider(Player hider) {
        GamePlayer gp = players.get(hider.getUniqueId());
        if (gp == null || gp.getRole() != GameRole.HIDER || !gp.isAlive()) return;

        gp.setAlive(false);
        gp.setRole(GameRole.SPECTATOR);
        hider.setGameMode(GameMode.SPECTATOR);
        hider.setHealth(hider.getAttribute(Attribute.MAX_HEALTH).getValue());

        MessageUtil.broadcastTitle("Пойман!", ChatColor.GRAY + hider.getName(), ChatColor.DARK_RED);
        MessageUtil.debug("Осталось прячущихся: " + countAliveHiders());

        if (countAliveHiders() == 0) {
            endGame(true); // победа искателя
        }
    }

    // =========================================================
    //  Шаг 14-15: угловня времени поиска
    // =========================================================
    private void onTimeUp() {
        // Если дошли до конца — значит выжили хоть один прячущийся
        endGame(false); // победа прячущихся
    }

    private void endGame(boolean seekerWon) {
        setState(GameState.ENDING);
        timerManager.cancel();

        if (seekerWon) {
            Player seeker = Bukkit.getPlayer(seekerUuid);
            String name = seeker != null ? seeker.getName() : "Искатель";
            MessageUtil.broadcastTitle("Победа искателя!", ChatColor.RED + name, ChatColor.GOLD);
            if (seeker != null) MessageUtil.send(seeker, "Ты поймал всех! Победа!");
        } else {
            MessageUtil.broadcastTitle("Победа прячущихся!", "", ChatColor.BLUE);
        }

        // Свечение всех выживших прячущихся + очистка инвентаря у всех
        for (Map.Entry<UUID, GamePlayer> entry : players.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;
            p.getInventory().clear();
            
            // Убираем тег "speed_used" у всех игроков
            p.removeMetadata("speed_used", plugin);
            
            if (entry.getValue().getRole() == GameRole.HIDER && entry.getValue().isAlive()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, configUtil.getEndTime() * 20, 0));
            }
        }

        timerManager.start(configUtil.getEndTime(), secondsLeft -> {}, this::resetGame);
    }

    // =========================================================
    //  Шаг 16-18: сброс времени игры -> хаб -> полный сброс
    // =========================================================
    private void resetGame() {
        // Сбрасываем погоду и время на всех мирах
        for (World world : Bukkit.getWorlds()) {
            world.setTime(1000); // Утро
            world.setStorm(false);
            world.setThundering(false);
        }

        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            
            // Сбрасываем максимальное ХП обратно на стандартное
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.getActivePotionEffects().forEach(eff -> p.removePotionEffect(eff.getType()));
            p.setHealth(p.getAttribute(Attribute.MAX_HEALTH).getValue());
            
            // Телепортируем на хаб или на дефолтные координаты
            Location spawnLocation;
            if (configUtil.isHubSet()) {
                spawnLocation = configUtil.getHub();
            } else {
                // Дефолтные координаты: -1997 107 -1992
                spawnLocation = new Location(
                    Bukkit.getWorlds().get(0),
                    -1997.0,
                    107.0,
                    -1992.0
                );
            }
            p.teleport(spawnLocation);
            
            // Убираем тег "speed_used" на всякий случай
            p.removeMetadata("speed_used", plugin);
        }

        players.clear();
        seekerUuid = null;
        chosenMap = null;
        scoreboardManager.resetAll();
        visibilityManager.restoreNameTags();

        setState(GameState.IDLE);
    }

    // =========================================================
    //  Принудительная остановка (/stop) — напротив, выигрыш искателя, если игра не в IDLE
    // =========================================================
    public void stopGame(String reason) {
        if (state == GameState.IDLE) return;
        timerManager.cancel();
        MessageUtil.broadcast(ChatColor.RED + "Игра остановлена: " + reason);
        resetGame();
    }

    // =========================================================
    //  Обработка выхода игрока (см. QuitListener)
    // =========================================================
    public void handleQuit(Player player) {
        if (state == GameState.IDLE) return;
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return;

        if (gp.getRole() == GameRole.SEEKER) {
            // Если выходит искатель - победа прячущимся
            endGame(false);
            return;
        }

        if (gp.getRole() == GameRole.HIDER && gp.isAlive()) {
            // Прячущийся вышел - считается пойманным
            gp.setAlive(false);
            gp.setRole(GameRole.SPECTATOR);
            MessageUtil.debug("Осталось прячущихся: " + countAliveHiders());
            if (countAliveHiders() == 0 && (state == GameState.SEARCHING || state == GameState.HIDING)) {
                endGame(true); // победа искателя
            }
        }
    }

    // =========================================================
    //  Обработка смерти искателя
    // =========================================================
    public void handleSeekerDeath(Player seeker) {
        if (state != GameState.SEARCHING) return;
        if (seekerUuid == null || !seekerUuid.equals(seeker.getUniqueId())) return;
        
        // Если умер искатель - победа прячущимся
        endGame(false);
    }

    private void setState(GameState newState) {
        this.state = newState;
        MessageUtil.debug("GameState -> " + newState);
    }
}
