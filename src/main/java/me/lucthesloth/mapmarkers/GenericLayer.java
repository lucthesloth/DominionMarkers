package me.lucthesloth.mapmarkers;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.WorldLayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenericLayer extends WorldLayer {
    String genericLayer;
    public GenericLayer(String layerName) {
        super(MapMarkers.instance.getConfig().getString("layers." + layerName + ".key", "DEF_LAYER_KEY"),
                Objects.requireNonNull(Pl3xMap.api().getWorldRegistry()
                        .get(MapMarkers.instance.getConfig().getString("layers." + layerName + ".world_name", "world"))),
                () -> MapMarkers.instance.getConfig().getString("layers." + layerName + ".label", "DEF_LAYER_LABEL"));
        genericLayer = layerName;
        setUpdateInterval(MapMarkers.instance.getConfig().getInt("global.updateInterval", 60));
        setShowControls(MapMarkers.instance.getConfig().getBoolean("layers." + layerName + ".showControls", true));
        setDefaultHidden(MapMarkers.instance.getConfig().getBoolean("layers." + layerName + ".defaultHidden", false));
        setPriority(MapMarkers.instance.getConfig().getInt("layers." + layerName + ".priority", 100));
        setZIndex(MapMarkers.instance.getConfig().getInt("layers." + layerName + ".zIndex", 100));
    }
    @Override
    public @NotNull Collection<net.pl3x.map.core.markers.marker.Marker<?>> getMarkers() {
        return MarkerUtils.markersMap.getOrDefault(genericLayer, Collections.emptyList()).stream().map(me.lucthesloth.mapmarkers.Marker::getIcon).collect(Collectors.toList());
    }
}
