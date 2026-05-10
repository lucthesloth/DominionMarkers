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

import java.util.concurrent.TimeUnit;

public class Pl3xMapListener implements EventListener {
    public Pl3xMapListener() {
        Pl3xMap.api().getEventRegistry().register(this);
    }

    @EventHandler
    public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {
        // Folia/Paper compatible delayed task; replaces Bukkit.getScheduler().runTaskLater.
        MapMarkers.instance.getServer().getGlobalRegionScheduler().runDelayed(MapMarkers.instance, t -> {
            MapMarkers.instance.getServer().getConsoleSender().sendMessage(
                    Msg.prefixed("§cRe-registering pl3x hooks. (Has pl3x reloaded?)"));
            MarkerUtils.forceRegisterAllIcons();
            Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
        }, 100L);
    }

    @EventHandler
    public void onServerLoaded(@NotNull ServerLoadedEvent event) {
        MarkerUtils.forceRegisterAllIcons();
        Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
    }

    @EventHandler
    public void onWorldLoaded(@NotNull WorldLoadedEvent event) {
        registerWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(@NotNull WorldUnloadedEvent event) {
        unregisterWorld(event.getWorld());
    }

    private void registerWorld(@NotNull World world) {
        MarkerUtils.markersMap.keySet().forEach(layerKey -> {
            String configWorld = MapMarkers.instance.getConfig().getString("layers." + layerKey + ".world_name", "world");
            if (!world.getName().equalsIgnoreCase(configWorld)) return;
            String pl3xKey = MapMarkers.instance.getConfig().getString("layers." + layerKey + ".key", "DEF_LAYER_KEY");
            if (world.getLayerRegistry().has(pl3xKey)) return;
            try {
                world.getLayerRegistry().register(new GenericLayer(layerKey));
            } catch (Throwable t) {
                MapMarkers.instance.getLogger().warning("Failed to register layer " + layerKey + " on world " + world.getName() + ": " + t.getMessage());
            }
        });
    }

    private void unregisterWorld(@NotNull World world) {
        MarkerUtils.markersMap.keySet().forEach(layerKey -> {
            String pl3xKey = MapMarkers.instance.getConfig().getString("layers." + layerKey + ".key");
            if (pl3xKey == null) return;
            try {
                if (world.getLayerRegistry().has(pl3xKey)) {
                    world.getLayerRegistry().unregister(pl3xKey);
                }
            } catch (Throwable ignored) {
            }
        });
    }

    public static void shutdown() {
        Pl3xMap.api().getWorldRegistry().forEach(world ->
                MarkerUtils.markersMap.keySet().forEach(layerKey -> {
                    String pl3xKey = MapMarkers.instance.getConfig().getString("layers." + layerKey + ".key");
                    if (pl3xKey == null) return;
                    try {
                        if (world.getLayerRegistry().has(pl3xKey)) {
                            world.getLayerRegistry().unregister(pl3xKey);
                        }
                    } catch (Throwable ignored) {
                    }
                })
        );
    }

    @SuppressWarnings("unused")
    private static long secondsToTicks(long seconds) {
        return TimeUnit.SECONDS.toMillis(seconds) / 50L;
    }
}
