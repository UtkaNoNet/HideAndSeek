package com.hideandseek.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Скрывает подписи над головами игроков (nametag), чтобы во время игры
 * нельзя было опознать конкретного человека, просто посмотрев на экран.
 * Ник в тайтле роли ("Ты искатель!") всё равно виден только самому игроку.
 *
 * ВАЖНО (ограничение vanilla API): полностью подменить имя в tab-листе
 * и над головой на "случайное" стандартными средствами Bukkit нельзя —
 * это упирается в протокол. То, что сделано ниже (NameTagVisibility.NEVER),
 * убирает подпись над головой полностью, чего обычно достаточно для игры
 * в прятки. Если нужна ещё и подмена имени в tab-листе — потребуется
 * ProtocolLib (пакетная подмена PlayerInfo), это отдельная доработка.
 */
public class VisibilityManager {

    private static final String TEAM_NAME = "hs_hidden";
    private Scoreboard board;

    public void hideAllNameTags() {
        board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(TEAM_NAME);
        if (team == null) {
            team = board.registerNewTeam(TEAM_NAME);
        }
        team.setNameTagVisibility(NameTagVisibility.NEVER);

        for (Player p : Bukkit.getOnlinePlayers()) {
            team.addEntry(p.getName());
        }
    }

    public void restoreNameTags() {
        if (board == null) return;
        Team team = board.getTeam(TEAM_NAME);
        if (team != null) {
            team.unregister();
        }
    }

    /** Добавить нового игрока (например, зашёл во время подготовки к игре). */
    public void addPlayer(Player player) {
        if (board == null) return;
        Team team = board.getTeam(TEAM_NAME);
        if (team != null) {
            team.addEntry(player.getName());
        }
    }
}
