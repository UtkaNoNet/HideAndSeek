package com.hideandseek.managers;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * Универсальный обратный отсчёт (используется и для 60 сек. пряток,
 * и для 7-минутного основного таймера, и для 20 сек. свободного времени).
 *
 * onTick вызывается каждую секунду с оставшимся временем в секундах —
 * именно там GameManager решает, показывать ли напоминание (30/10..1)
 * или наложить эффект свечения раз в минуту.
 * onFinish вызывается один раз, когда время истекло.
 */
public class TimerManager {

    private final JavaPlugin plugin;
    private BukkitTask task;

    public TimerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(int totalSeconds, Consumer<Integer> onTick, Runnable onFinish) {
        cancel(); // на всякий случай не даём двум таймерам работать одновременно

        task = new BukkitRunnable() {
            int secondsLeft = totalSeconds;

            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    cancel();
                    onFinish.run();
                    return;
                }
                onTick.accept(secondsLeft);
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20 тиков = 1 секунда
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = null;
    }
}
