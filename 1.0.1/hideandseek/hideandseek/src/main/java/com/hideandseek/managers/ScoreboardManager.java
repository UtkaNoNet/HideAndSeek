package com.hideandseek.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * Отвечает за сайдбар (справа на экране) со временем и статусом игры.
 * Отдельно от VisibilityManager: тут только цифры/текст, не ники.
 */
public class ScoreboardManager {

    private Scoreboard board;
    private Objective objective;

    public void setup() {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("hideandseek", "dummy", "§e§lHIDE & SEEK");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    /** Обновляет строку с оставшимся временем (вызывается каждую секунду из GameManager). */
    public void updateTime(int secondsLeft, int aliveHiders) {
        if (objective == null) return;
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        String time = String.format("%02d:%02d", minutes, seconds);

        objective.getScore("§7Осталось времени:").setScore(3);
        objective.getScore("§f" + time).setScore(2);
        objective.getScore("§7Прячущихся осталось: §a" + aliveHiders).setScore(1);
    }

    public void clear() {
        if (board == null) return;
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
    }

    /** Сбрасывает всем игрокам скорборд по умолчанию (после конца игры). */
    public void resetAll() {
        clear();
        Scoreboard empty = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(empty);
        }
    }
}
