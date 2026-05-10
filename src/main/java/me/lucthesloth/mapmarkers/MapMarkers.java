package me.lucthesloth.mapmarkers;

import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class MapMarkers extends JavaPlugin {
    public static MapMarkers instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        for (String key : getConfig().getStringList("layers.keys")) {
            getLogger().info("Configured layer: " + key);
        }

        MarkerUtils.markerGson = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .setLenient()
                .create();

        try {
            MarkerUtils.initializeLayer();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to read/create marker file", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            getLogger().info("Could not find Pl3xMap. Disabling..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Found Pl3xMap. Hooking into plugin.");
        new Pl3xMapListener();

        MarkerUtils.loadMarkers();

        Objects.requireNonNull(getCommand("mapmarkers")).setExecutor(new MarkerCommand());
        Objects.requireNonNull(getCommand("mapmarkers")).setTabCompleter(new MarkerCommandCompleter());
        Objects.requireNonNull(getCommand("markersmigrate")).setExecutor(new MigrateCommand());
        Objects.requireNonNull(getCommand("markersmigrate")).setTabCompleter(new MigrateCommand.MigrateCommandCompleter());

        getServer().getPluginManager().registerEvents(new MarkerListener(), this);

        // Periodic safety save (every 5 minutes) — async, Folia-safe.
        long fiveMinTicks = TimeUnit.MINUTES.toSeconds(5);
        getServer().getAsyncScheduler().runAtFixedRate(this,
                t -> MarkerUtils.saveMarkers(),
                fiveMinTicks, fiveMinTicks, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        try {
            Pl3xMapListener.shutdown();
        } catch (Throwable ignored) {
        }
        MarkerUtils.saveMarkers();
    }
}
