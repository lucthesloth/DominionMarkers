package me.lucthesloth.mapmarkers;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShopsLayer extends WorldLayer {
    public ShopsLayer() {
        super(MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY"),
                Objects.requireNonNull(Pl3xMap.api().getWorldRegistry()
                        .get(MapMarkers.instance.getConfig().getString("layer.world_name", "world"))),
                () -> MapMarkers.instance.getConfig().getString("layer.label", "DEF_LAYER_LABEL"));
        setUpdateInterval(MapMarkers.instance.getConfig().getInt("layer.updateInterval", 60));
        setShowControls(MapMarkers.instance.getConfig().getBoolean("layer.showControls", true));
        setDefaultHidden(MapMarkers.instance.getConfig().getBoolean("layer.defaultHidden", false));
        setPriority(MapMarkers.instance.getConfig().getInt("layer.priority", 100));
        setZIndex(MapMarkers.instance.getConfig().getInt("layer.zIndex", 100));
    }
    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return MarkerUtils.markers.stream().map(me.lucthesloth.mapmarkers.Marker::getIcon).collect(Collectors.toList());
    }
}
