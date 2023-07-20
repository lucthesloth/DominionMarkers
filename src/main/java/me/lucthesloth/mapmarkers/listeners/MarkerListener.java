package me.lucthesloth.mapmarkers.listeners;

import me.lucthesloth.mapmarkers.util.InteractiveMarkerProcess;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MarkerListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerLeave(PlayerQuitEvent event) {
        InteractiveMarkerProcess.processes.remove(event.getPlayer());
    }
}
