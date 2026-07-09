package com.hideandseek.model;

import java.util.UUID;

/**
 * Игровое состояние одного участника: его роль и статус "жив/выбыл".
 * Реальный Bukkit Player достаётся по UUID через Bukkit.getPlayer(uuid),
 * тут не хранится ссылка на Player, чтобы не было утечек памяти между играми.
 */
public class GamePlayer {

    private final UUID uuid;
    private GameRole role;
    private boolean alive;
    private String votedMapId; // id карты, за которую проголосовал (null пока не проголосовал)

    public GamePlayer(UUID uuid) {
        this.uuid = uuid;
        this.role = GameRole.NONE;
        this.alive = true;
        this.votedMapId = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameRole getRole() {
        return role;
    }

    public void setRole(GameRole role) {
        this.role = role;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getVotedMapId() {
        return votedMapId;
    }

    public void setVotedMapId(String votedMapId) {
        this.votedMapId = votedMapId;
    }
}
