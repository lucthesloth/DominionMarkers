package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class MarkerUtils {
    public static Gson markerGson;
    public static File dataFolder;
    public static final Map<String, List<Marker>> markersMap = new ConcurrentHashMap<>();

    private MarkerUtils() {}

    static void initializeLayer() throws IOException {
        dataFolder = new File(MapMarkers.instance.getDataFolder(), "markers");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Could not create marker data folder: " + dataFolder.getAbsolutePath());
        }
        for (String layerKey : configuredLayerKeys()) {
            File f = layerFile(layerKey);
            if (f == null) {
                MapMarkers.instance.getLogger().warning("Layer '" + layerKey + "' missing 'key' in config; skipping init.");
                continue;
            }
            if (!f.exists() && !f.createNewFile()) {
                throw new IOException("Could not create marker file: " + f.getAbsolutePath());
            }
        }
    }

    public static List<String> configuredLayerKeys() {
        return MapMarkers.instance.getConfig().getStringList("layers.keys");
    }

    public static @Nullable File layerFile(String layerKey) {
        String fileKey = MapMarkers.instance.getConfig().getString("layers." + layerKey + ".key");
        if (fileKey == null || fileKey.isBlank()) return null;
        return new File(dataFolder, fileKey + ".json");
    }

    public static void loadMarkers() {
        markersMap.clear();
        for (String key : configuredLayerKeys()) {
            File temp = layerFile(key);
            if (temp == null || !temp.exists()) {
                MapMarkers.instance.getLogger().warning("Layer '" + key + "' has no readable file; loading empty.");
                markersMap.put(key, new CopyOnWriteArrayList<>());
                continue;
            }
            try (Reader reader = Files.newBufferedReader(temp.toPath(), StandardCharsets.UTF_8)) {
                Marker[] arr = markerGson.fromJson(reader, Marker[].class);
                List<Marker> list = new CopyOnWriteArrayList<>();
                if (arr != null) {
                    for (Marker m : arr) {
                        m.setId(normalize(m.getId()));
                        if (m.getName() != null) m.setName(m.getName().toUpperCase());
                        list.add(m);
                    }
                }
                markersMap.put(key, list);
            } catch (IOException e) {
                MapMarkers.instance.getLogger().log(Level.WARNING, "Failed to load markers for layer " + key, e);
                markersMap.put(key, new CopyOnWriteArrayList<>());
            }
        }
    }

    public static void saveMarkers() {
        markersMap.forEach((key, list) -> {
            File temp = layerFile(key);
            if (temp == null) return;
            try (Writer writer = Files.newBufferedWriter(temp.toPath(), StandardCharsets.UTF_8)) {
                markerGson.toJson(new ArrayList<>(list), writer);
            } catch (IOException e) {
                MapMarkers.instance.getLogger().log(Level.WARNING, "Failed to save markers for layer " + key, e);
            }
        });
    }

    /** Best-effort search: exact id first, then unique substring fallback. */
    static @Nullable Marker markerExists(String id, String key) {
        List<Marker> markers = markersMap.getOrDefault(key, Collections.emptyList());
        String normalized = normalize(id);
        Optional<Marker> exact = markers.stream().filter(m -> m.getId().equalsIgnoreCase(normalized)).findFirst();
        if (exact.isPresent()) return exact.get();
        return markers.stream().filter(m -> m.getId().contains(normalized)).findFirst().orElse(null);
    }

    static @Nullable Marker markerExistsEqual(String id, String key) {
        List<Marker> markers = markersMap.getOrDefault(key, Collections.emptyList());
        String normalized = normalize(id);
        return markers.stream().filter(m -> m.getId().equalsIgnoreCase(normalized)).findFirst().orElse(null);
    }

    public static boolean addMarker(Marker marker, String key) {
        List<Marker> list = markersMap.get(key);
        if (list == null) return false;
        if (markerExistsEqual(marker.getId(), key) != null) return false;
        list.add(marker);
        return true;
    }

    /** Removes by exact id. Returns true if a marker was removed. */
    static boolean removeMarker(String id, String key) {
        List<Marker> list = markersMap.get(key);
        if (list == null) return false;
        String normalized = normalize(id);
        return list.removeIf(m -> m.getId().equalsIgnoreCase(normalized));
    }

    public static String normalize(String string) {
        if (string == null) return "";
        return string.toLowerCase().trim().replaceAll("[^a-z0-9]", "");
    }

    public static List<Marker> nearbyMarkers(Player p, String key, @Nullable Integer radius) {
        String layerWorld = MapMarkers.instance.getConfig().getString("layers." + key + ".world_name", "world");
        if (!p.getWorld().getName().equalsIgnoreCase(layerWorld)) {
            return Collections.emptyList();
        }
        int effectiveRadius = radius != null
                ? radius
                : MapMarkers.instance.getConfig().getInt("global.nearbyRadius", 10);
        long radiusSq = (long) effectiveRadius * effectiveRadius;
        int px = p.getLocation().getBlockX();
        int pz = p.getLocation().getBlockZ();

        List<Marker> hits = new ArrayList<>();
        for (Marker marker : markersMap.getOrDefault(key, Collections.emptyList())) {
            double dx = marker.getX() - px;
            double dz = marker.getZ() - pz;
            if (dx * dx + dz * dz <= radiusSq) {
                hits.add(marker);
            }
        }
        return hits;
    }

    public static int distance(double x0, double z0, int x1, int z1) {
        return (int) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(z1 - z0, 2));
    }

    public static void forceRegisterAllIcons() {
        File file = Pl3xMap.api().getIconRegistry().getDir().toFile().getParentFile();
        MapMarkers.instance.getLogger().info("Registering icons from " + file.getAbsolutePath());
        if (!file.exists() || !file.isDirectory()) return;
        File[] files = file.listFiles();
        if (files == null) return;
        for (File iconFile : files) {
            if (!iconFile.isFile() || !iconFile.getName().endsWith(".png")) continue;
            String name = iconFile.getName().substring(0, iconFile.getName().length() - ".png".length());
            try {
                if (Pl3xMap.api().getIconRegistry().has(name)) continue;
                MapMarkers.instance.getLogger().info("Registering icon " + iconFile.getName());
                Pl3xMap.api().getIconRegistry().register(new IconImage(name, ImageIO.read(iconFile), "png"));
            } catch (IOException e) {
                MapMarkers.instance.getLogger().log(Level.WARNING, "Failed to register icon " + iconFile.getName(), e);
            }
        }
    }

    /** Async save via Folia AsyncScheduler (works on Paper too). */
    public static void saveMarkersAsync() {
        Objects.requireNonNull(MapMarkers.instance, "plugin not initialized");
        MapMarkers.instance.getServer().getAsyncScheduler().runNow(MapMarkers.instance, t -> saveMarkers());
    }
}
