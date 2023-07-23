package me.lucthesloth.mapmarkers.pl3x;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.lucthesloth.mapmarkers.MapMarkers;
import me.lucthesloth.mapmarkers.util.MarkerUtils;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static me.lucthesloth.mapmarkers.util.MarkerUtils.Log;
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class MarkerLayer extends WorldLayer {
    private static final Gson markerGson = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final File dataFolder = new File(MapMarkers.instance.getDataFolder(), "markers");
    private final File markerFile;
    private List<me.lucthesloth.mapmarkers.pl3x.Marker> markers;
    private Section layerConfiguration;
    public MarkerLayer(Section section) throws IOException {
        super(section.getString("key", "DEF_LAYER_KEY"),
                Objects.requireNonNull(Pl3xMap.api().getWorldRegistry()
                        .get(section.getString("world", "world"))),
                () -> section.getString("label", "DEFAULT_LAYER_LABEL"));
        setUpdateInterval(section.getInt("updateInterval", 60));
        setShowControls(section.getBoolean("showControls", true));
        setDefaultHidden(section.getBoolean("defaultHidden", false));
        setPriority(section.getInt("priority", 100));
        setZIndex(section.getInt("zIndex", 100));

        layerConfiguration = section;
        markers = new ArrayList<>();

        markerFile = new File(dataFolder, MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY") + ".json");
        markerFile.mkdirs();
        markerFile.createNewFile();
    }
    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return this.markers.stream().map(me.lucthesloth.mapmarkers.pl3x.Marker::getIcon).collect(Collectors.toList());
    }
    public List<me.lucthesloth.mapmarkers.pl3x.Marker> getMarkerList(){
        return markers;
    }

    public void LoadMarkers(){
        try {
            markers = new ArrayList<>();
            me.lucthesloth.mapmarkers.pl3x.Marker[] markerArray = markerGson.fromJson(new FileReader(markerFile), me.lucthesloth.mapmarkers.pl3x.Marker[].class);
            if (markerArray != null)
                Collections.addAll(markers, markerArray);
            markers.forEach(k -> {
                k.setId(MarkerUtils.normalize(k.getId()));
                k.setName(k.getName().toUpperCase());
            });
        } catch (IOException e) {
            Log("&cFailed to load markers for layer " + getLabel());
            e.printStackTrace();
        }
    }

    public boolean addMarker(me.lucthesloth.mapmarkers.pl3x.Marker marker){
        if (findMarker(marker.getId()) != null)
            return false;
        markers.add(marker);
        saveMarkers();
        return true;
    }
    public boolean removeMarker(me.lucthesloth.mapmarkers.pl3x.Marker marker){
        boolean t = markers.removeIf(k -> k.getId().equalsIgnoreCase(marker.getId()));
        if (t) saveMarkers();
        return t;
    }
    public void saveMarkers(){
        FileWriter writer;
        try {
            writer = new FileWriter(markerFile, false);
            markerGson.toJson(markers, writer);
            writer.close();
        } catch (IOException e) {
            Log("&cFailed to save markers for layer " + getLabel());
            e.printStackTrace();
        }
    }
    public @Nullable me.lucthesloth.mapmarkers.pl3x.Marker findMarker(String id){
        List<me.lucthesloth.mapmarkers.pl3x.Marker> t =  markers.stream().filter(marker -> marker.getId().contains(id.toLowerCase())).toList();
        me.lucthesloth.mapmarkers.pl3x.Marker k = t.stream().filter(marker -> marker.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
        if (k == null)
            k = t.stream().findFirst().orElse(null);
        return k;
    }
    public List<me.lucthesloth.mapmarkers.pl3x.Marker> nearbyMarkers(Player p, int r){
        if (p.getWorld().getName().equalsIgnoreCase(getWorld().getName())){
            return markers.stream().filter(marker -> MarkerUtils.distance(marker.getX(), marker.getZ(), p.getLocation().getBlockX(), p.getLocation().getBlockZ()) <= r).toList();
        }
        return Collections.emptyList();
    }
}
