package me.lucthesloth.mapmarkers.listeners;

import me.lucthesloth.mapmarkers.MapMarkers;
import me.lucthesloth.mapmarkers.util.InteractiveMarkerProcess;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MarkerListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerLeave(PlayerQuitEvent event) {
        InteractiveMarkerProcess.processes.remove(event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event){
        if (event.getPlayer().hasPermission("mapmarkers.admin") && MapMarkers.update.isUpdateAvailable())
            event.getPlayer().sendMessage(MapMarkers.update.getUpdateMessage());
    }
}
