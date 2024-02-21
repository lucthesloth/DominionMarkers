package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class MarkerUtils {
    public static Gson markerGson;
    public static File dataFolder;
    public static File markerFile;
    public static Map<String, List<Marker>> markersMap = new HashMap<>();
    public static void loadMarkers(){
        markersMap = new HashMap<>();
        MapMarkers.instance.getConfig().getStringList("layers.keys").forEach(k -> {
            File temp = new File(dataFolder, MapMarkers.instance.getConfig().getString("layers." + k + ".key") + ".json");
            try {
                Marker[] markerArray = markerGson.fromJson(new FileReader(temp), Marker[].class);
                List<Marker> t = new ArrayList<>();
                if (markerArray != null)
                    Collections.addAll(t, markerArray);
                t.forEach(e -> {
                    e.setId(MarkerUtils.normalize(e.getId()));
                    e.setName(e.getName().toUpperCase());
                });
                markersMap.put(k, t);
            } catch (FileNotFoundException e) {
                Bukkit.getLogger().warning("Failed to load markers");
                e.printStackTrace();
            }
        });
    }
    public static void saveMarkers(){
        markersMap.forEach((key, l) -> {
            try {
                File temp = new File(dataFolder, MapMarkers.instance.getConfig().getString("layers." + key + ".key") + ".json");
                FileWriter writer = new FileWriter(temp, false);
                markerGson.toJson(l, writer);
                writer.close();
            } catch (IOException e) {
                Bukkit.getLogger().warning("Failed to save markers");
                e.printStackTrace();
            }
        });
    }
    static void initializeLayer() throws IOException {
        dataFolder = new File(MapMarkers.instance.getDataFolder(), "markers");
        dataFolder.mkdirs();
        MapMarkers.instance.getConfig().getStringList("layers.keys").forEach( t -> {
            markerFile = new File(dataFolder, MapMarkers.instance.getConfig().getString("layers." + t + ".key", "DEF_LAYER_KEY") + ".json");
            try {
                markerFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    static @Nullable Marker markerExists(String id, String key){
        List<Marker> t = markersMap.getOrDefault(key, Collections.emptyList()).stream().filter(marker -> marker.getId().contains(id.toLowerCase())).toList();
        Marker k = t.stream().filter(marker -> marker.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
        if (k == null)
            k = t.stream().findFirst().orElse(null);
        return k;
    }
    static @Nullable Marker markerExistsEqual(String id, String key){
        return markersMap.getOrDefault(key, Collections.emptyList()).stream().filter(marker -> marker.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
    public static boolean addMarker(Marker marker, String key) {
        if (!markersMap.containsKey(key)) return false;
        if (markerExistsEqual(marker.getId(), key) == null) {
            markersMap.getOrDefault(key, Collections.emptyList()).add(marker);
            return true;
        }
        return false;
    }
    static boolean removeMarker(String id, String key) {
        return markersMap.getOrDefault(key, Collections.emptyList()).removeIf(marker -> marker.getId().equalsIgnoreCase(id));
    }
    public static String normalize(String string){
        return string.toLowerCase().trim().replaceAll("[^A-Za-z0-9]", "");
    }
    public static List<Marker> nearbyMarkers(Player p, String key, @Nullable Integer radius) {
        if (!p.getWorld().getName().equalsIgnoreCase(MapMarkers.instance.getConfig().getString("layer.world_name", "world")))
            return Collections.EMPTY_LIST;
        List<Marker> nearbyMarkers = new ArrayList<>();
        for (Marker marker : markersMap.getOrDefault(key, Collections.emptyList())) {
            if (distance(marker.getX(), marker.getZ(), p.getLocation().getBlockX(), p.getLocation().getBlockZ()) <= (radius != null ? radius : MapMarkers.instance.getConfig().getInt("layer.nearbyRadius", 10))) {
                nearbyMarkers.add(marker);
            }
        }
        return nearbyMarkers;
    }
    public static int distance(double x0, double z0, int x1, int z1){
        return (int) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(z1 - z0, 2));
    }

    public static void forceRegisterAllIcons(){
        File file = Pl3xMap.api().getIconRegistry().getDir().toFile().getParentFile();
        MapMarkers.instance.getLogger().info("Registering icons from " + file.getAbsolutePath());
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File iconFile : files) {
                    if (iconFile.isFile() && iconFile.getName().endsWith(".png")) {
                        try {
                            MapMarkers.instance.getLogger().info("Registering icon " + iconFile.getName());
                            if (!Pl3xMap.api().getIconRegistry().has(iconFile.getName().replace(".png", "")))
                                Pl3xMap.api().getIconRegistry().register(new IconImage(iconFile.getName().replace(".png", ""), ImageIO.read(iconFile), "png"));
                        } catch (IOException e) {
                            MapMarkers.instance.getLogger().warning("Failed to register icon " + iconFile.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
