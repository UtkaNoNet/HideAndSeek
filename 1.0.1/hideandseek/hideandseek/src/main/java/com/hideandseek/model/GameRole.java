package com.hideandseek.model;

/**
 * Роли игрока в текущей игре.
 * NONE       — игрок не участвует (в хабе вне игры).
 * SEEKER     — искатель (красный ник, лук).
 * HIDER      — прячущийся (синий ник, 1 сердце).
 * SPECTATOR  — прячущийся, которого поймали (режим наблюдателя).
 */
public enum GameRole {
    NONE,
    SEEKER,
    HIDER,
    SPECTATOR
}
