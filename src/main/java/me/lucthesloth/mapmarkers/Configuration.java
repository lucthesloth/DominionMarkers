package me.lucthesloth.mapmarkers;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Configuration {
    MapMarkers plugin;

    private YamlDocument config;

    public Configuration(MapMarkers plugin) {
        this.plugin = plugin;
    }

    public void load() throws IOException {
        config = YamlDocument.create(new File(plugin.getDataFolder(), "config.yml"),
                Objects.requireNonNull(plugin.getResource("config.yml")),
                GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("file_version")).build());
        this.save();
    }

    public void save() throws IOException {
        config.save();
    }
    public void reload() throws IOException {
        config.reload();
    }

    public YamlDocument get(){
        return config;
    }
}
