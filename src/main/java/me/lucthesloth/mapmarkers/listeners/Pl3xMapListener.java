package me.lucthesloth.mapmarkers.listeners;

import me.lucthesloth.mapmarkers.MapMarkers;
import me.lucthesloth.mapmarkers.util.MarkerUtils;
import me.lucthesloth.mapmarkers.pl3x.ShopsLayer;
import net.kyori.adventure.text.Component;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.server.ServerLoadedEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;


public class Pl3xMapListener implements EventListener {
    public Pl3xMapListener() {
        Pl3xMap.api().getEventRegistry().register(this);
    }
    @EventHandler
    public void onPl3xMapEnabled(@NotNull Pl3xMapEnabledEvent event) {
        Bukkit.getScheduler().runTaskLater(MapMarkers.instance, () -> {
            MapMarkers.instance.getServer().getConsoleSender().sendMessage(Component.text("§3[§9MapMarkers§3] §r§cRe-registering pl3x hooks. (Has pl3x reloaded?)"));
            MarkerUtils.forceRegisterAllIcons();
            Pl3xMap.api().getWorldRegistry().forEach(this::registerWorld);
        }, 100);

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
    @SuppressWarnings("unused")
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
