package me.lucthesloth.mapmarkers;

import me.lucthesloth.mapmarkers.commands.MarkerCommand;
import me.lucthesloth.mapmarkers.commands.MarkerCommandCompleter;
import me.lucthesloth.mapmarkers.commands.MigrateCommand;
import me.lucthesloth.mapmarkers.listeners.Pl3xMapListener;
import me.lucthesloth.mapmarkers.util.MarkerUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;


public final class MapMarkers extends JavaPlugin {
    public static MapMarkers instance;
    public static Update update;
    public static Configuration config;
    @Override
    public void onEnable() {
        instance = this;
        config = new Configuration(this);
        try {
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (config.get().getBoolean("check-updates", true)){
            try {
                update = new Update(this);
                String t = update.getUpdateMessageStr();
                if (t != null) {
                    MarkerUtils.Log(t);
                }
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }
        }
        try {
            MarkerUtils.LoadLayers();
        } catch (IOException e) {
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

        Objects.requireNonNull(getCommand("mapmarkers")).setExecutor(new MarkerCommand());
        Objects.requireNonNull(getCommand("mapmarkers")).setTabCompleter(new MarkerCommandCompleter());
        Objects.requireNonNull(getCommand("markersmigrate")).setExecutor(new MigrateCommand());
        Objects.requireNonNull(getCommand("markersmigrate")).setTabCompleter(new MigrateCommand.MigrateCommandCompleter());
        MarkerUtils.Log(MarkerUtils.rainbowText("This text should become a rainbow text! :)"));
    }
    @Override
    public void onDisable() {
        MarkerUtils.SaveLayers();
    }

}
