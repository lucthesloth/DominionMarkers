package me.lucthesloth.mapmarkers;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;


public class Pl3xMapListener implements EventListener {
    public Pl3xMapListener() {
        Pl3xMap.api().getEventRegistry().register(this);
    }

    @EventHandler
    public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {

    }

    @EventHandler
    public void onServerLoaded(@NotNull ServerLoadedEvent event) {
        Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
    }

    @EventHandler
    public void onWorldLoaded(@NotNull WorldLoadedEvent event) {
        registerWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(@NotNull WorldUnloadedEvent event) {
        try {
            event.getWorld().getLayerRegistry().unregister(MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY"));
        } catch (Throwable ignore) {
        }
    }

    private void registerWorld(@NotNull World world) {
        if (world.getName().equalsIgnoreCase(MapMarkers.instance.getConfig().getString("layer.world_name", "world"))) {
            world.getLayerRegistry().register(new ShopsLayer());
        }
    }

    public static void shutdown() {
        Pl3xMap.api().getWorldRegistry().forEach(world -> {
            try {
                if (world.getLayerRegistry().has(MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY"))) {
                    world.getLayerRegistry().unregister(MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY"));
                }
            } catch (Throwable ignore) {
            }
        });
    }
}
