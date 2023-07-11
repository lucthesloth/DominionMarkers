package me.lucthesloth.mapmarkers;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;


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
        Objects.requireNonNull(getCommand("mapmarkers")).setExecutor(new MarkerCommand());
    }
    @Override
    public void onDisable() {
        MarkerUtils.saveMarkers();
    }

}
