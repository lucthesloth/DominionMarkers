package me.lucthesloth.mapmarkers;

import com.google.gson.GsonBuilder;
import me.lucthesloth.mapmarkers.commands.MarkerCommand;
import me.lucthesloth.mapmarkers.commands.MarkerCommandCompleter;
import me.lucthesloth.mapmarkers.commands.MigrateCommand;
import me.lucthesloth.mapmarkers.listeners.Pl3xMapListener;
import me.lucthesloth.mapmarkers.util.MarkerUtils;
import net.kyori.adventure.text.Component;
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
            MarkerUtils.Log("Failed to read/create marker file");
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);

        }
        if (getServer().getPluginManager().isPluginEnabled("Pl3xMap")) {
            MarkerUtils.Log("Found Pl3xMap. Hooking into plugin.");
            new Pl3xMapListener();
        } else {
            MarkerUtils.Log("Could not find Pl3xMap. Disabling..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        MarkerUtils.loadMarkers();
        Objects.requireNonNull(getCommand("mapmarkers")).setExecutor(new MarkerCommand());
        Objects.requireNonNull(getCommand("mapmarkers")).setTabCompleter(new MarkerCommandCompleter());
        Objects.requireNonNull(getCommand("markersmigrate")).setExecutor(new MigrateCommand());
        Objects.requireNonNull(getCommand("markersmigrate")).setTabCompleter(new MigrateCommand.MigrateCommandCompleter());
        MarkerUtils.Log(MarkerUtils.rainbowText("This text should become a rainbow text! :)"));
    }
    @Override
    public void onDisable() {
        MarkerUtils.saveMarkers();
    }

}
