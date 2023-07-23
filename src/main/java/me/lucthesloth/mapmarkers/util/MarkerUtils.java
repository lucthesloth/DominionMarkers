package me.lucthesloth.mapmarkers.util;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.lucthesloth.mapmarkers.MapMarkers;
import me.lucthesloth.mapmarkers.pl3x.MarkerLayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MarkerUtils {
    public static Map<String, MarkerLayer> markerLayers;
    public static void LoadLayers() throws IOException {
        markerLayers = new LinkedHashMap<>();
        for (String t : MapMarkers.config.get().getStringList("layers.enabled", Collections.emptyList())){
            Section s;
            if ((s = MapMarkers.config.get().getSection("layers." + t)) == null){
                Log("Layer " + t + " does not exist. Skipping..");
                continue;
            }
            markerLayers.put(t, new MarkerLayer(s));
        }
    }
    public static void SaveLayers(){
        for (MarkerLayer layer : markerLayers.values()){
            layer.saveMarkers();
        }
    }
    public static String normalize(String string){
        return string.toLowerCase().trim().replaceAll("[^A-Za-z0-9]", "");
    }
    public static int distance(double x0, double z0, int x1, int z1){
        return (int) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(z1 - z0, 2));
    }
    public static void forceRegisterAllIcons(){
        File file = Pl3xMap.api().getIconRegistry().getDir().toFile().getParentFile();
        Log("Registering icons from " + file.getAbsolutePath());
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File iconFile : files) {
                    if (iconFile.isFile() && iconFile.getName().endsWith(".png")) {
                        try {
                            Log("Registering icon " + iconFile.getName());
                            if (!Pl3xMap.api().getIconRegistry().has(iconFile.getName().replace(".png", "")))
                                Pl3xMap.api().getIconRegistry().register(new IconImage(iconFile.getName().replace(".png", ""), ImageIO.read(iconFile), "png"));
                        } catch (IOException e) {
                            Log("Failed to register icon " + iconFile.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("deprecation")
    public static void Log(String message){
        Bukkit.getServer().getConsoleSender().sendMessage("§3[§9MapMarkers§3]§r " + ChatColor.translateAlternateColorCodes('&', message));
    }
    public static String rainbowText(String text){
        return rainbowText(text, null);
    }
    public static String rainbowText(String t, @Nullable String[] colorCodes){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (char c : t.toCharArray()) {
            if (c == ' ' || c == '§' || c == '&') {
                sb.append(c);
                continue;
            }
            sb.append(colorCodes == null ? rainbowColors[i] : colorCodes[i]).append(c);
            i++;
            if (i >= (colorCodes == null ? rainbowColors.length : colorCodes.length))
                i = 0;
        }
        return sb.toString();
    }
    public static final String[] rainbowColors = new String[]{"§4", "§c", "§6", "§e", "§2", "§a", "§b", "§3", "§1", "§9", "§d", "§5"};

    public static final TextComponent prefix = Component.text("§3[§9MapMarkers§3]§r ");
}
