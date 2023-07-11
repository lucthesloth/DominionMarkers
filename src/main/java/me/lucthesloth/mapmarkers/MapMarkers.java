package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.option.Options;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class MapMarkers extends JavaPlugin {
    public static MapMarkers instance;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        MarkerUtils.markerGson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().setLenient().create();
        try {
            MarkerUtils.initializeLayer();
        } catch (IOException e) {
            this.getLogger().warning("Failed to read/create marker file");
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }
        if (getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            getLogger().info("Found Pl3xMap. Hooking into plugin.");
            new Pl3xMapListener();
        } else {
            getLogger().info("Could not find Pl3xMap. Disabling..");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        MarkerUtils.loadMarkers();
    }
    @Override
    public void onDisable() {
        MarkerUtils.saveMarkers();
    }

}
