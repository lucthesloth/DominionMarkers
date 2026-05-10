package me.lucthesloth.mapmarkers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MarkerListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLeave(PlayerQuitEvent event) {
        InteractiveMarkerProcess.processes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDimensionSwitch(PlayerChangedWorldEvent event) {
        if (InteractiveMarkerProcess.processes.remove(event.getPlayer().getUniqueId()) != null) {
            event.getPlayer().sendMessage(Msg.of("§cRemoving you from marker creation due to dimension change."));
        }
    }
}
