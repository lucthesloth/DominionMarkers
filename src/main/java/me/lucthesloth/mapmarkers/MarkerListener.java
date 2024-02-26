package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MarkerListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerLeave(PlayerQuitEvent event) {
        InteractiveMarkerProcess.processes.remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onDimensionSwitch(PlayerChangedWorldEvent event) {
        if (InteractiveMarkerProcess.processes.containsKey(event.getPlayer())) {
            InteractiveMarkerProcess.processes.remove(event.getPlayer());
            event.getPlayer().sendMessage(Component.text("§cRemoving you from marker creation due to dimension change."));
        }
    }
}
