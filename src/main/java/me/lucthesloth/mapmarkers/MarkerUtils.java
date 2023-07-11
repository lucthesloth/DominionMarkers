package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkerUtils {
    public static Gson markerGson;
    public static File dataFolder;
    public static File markerFile;
    public static ArrayList<Marker> markers = new ArrayList<>();
    public static void loadMarkers(){
        try {
            markers = new ArrayList<>();
            Marker[] markerArray = markerGson.fromJson(new FileReader(markerFile), Marker[].class);
            for (Marker marker : markerArray) {
                markers.add(marker);
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to load markers");
            e.printStackTrace();
        }
    }
    public static void saveMarkers(){
        FileWriter writer;
        try {
            writer = new FileWriter(markerFile, false);
            markerGson.toJson(markers, writer);
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to save markers");
            e.printStackTrace();
        }
    }
    static void initializeLayer() throws IOException {
        dataFolder = new File(MapMarkers.instance.getDataFolder(), "markers");
        dataFolder.mkdirs();
        markerFile = new File(dataFolder, MapMarkers.instance.getConfig().getString("layer.key", "DEF_LAYER_KEY") + ".json");
        markerFile.createNewFile();
    }
    static @Nullable Marker markerExists(String id){
        return markers.stream().filter(marker -> marker.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
    static boolean removeMarker(String id) {
        if (markers.removeIf(marker -> marker.getId().equalsIgnoreCase(id))) {
            saveMarkers();
            return true;
        }
        return false;
    }
    public static String normalize(String string){
        return string.toLowerCase().trim().replaceAll("[^A-Za-z0-9]", "");
    }
    public static List<Marker> nearbyMarkers(Player p, @Nullable Integer radius) {
        if (!p.getWorld().getName().equalsIgnoreCase(MapMarkers.instance.getConfig().getString("layer.world_name", "world")))
            return Collections.EMPTY_LIST;
        List<Marker> nearbyMarkers = new ArrayList<>();
        for (Marker marker : markers) {
            if (distance(marker.getX(), marker.getZ(), p.getLocation().getBlockX(), p.getLocation().getBlockZ()) <= (radius != null ? radius : MapMarkers.instance.getConfig().getInt("layer.nearbyRadius", 10))) {
                nearbyMarkers.add(marker);
            }
        }
        return nearbyMarkers;
    }
    public static int distance(double x0, double z0, int x1, int z1){
        return (int) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(z1 - z0, 2));
    }
}
