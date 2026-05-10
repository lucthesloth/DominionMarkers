package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class MigrateCommand implements CommandExecutor {
    public static File dataFolder;

    public MigrateCommand() {
        dataFolder = new File(MapMarkers.instance.getDataFolder(), "migrate");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            MapMarkers.instance.getLogger().warning("Could not create migrate folder: " + dataFolder.getAbsolutePath());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mapmarkers.migrate")) return true;
        if (args.length != 2) {
            sender.sendMessage(Msg.of("§cUsage: /migrate <filename> <layerName>"));
            return true;
        }
        if (!MarkerUtils.markersMap.containsKey(args[1])) {
            sender.sendMessage(Msg.of("§cUnknown layer: " + args[1]));
            return true;
        }
        String[] entries = dataFolder.list();
        if (entries == null || Arrays.stream(entries).noneMatch(t -> t.equalsIgnoreCase(args[0]))) {
            sender.sendMessage(Msg.of("§cFile not found"));
            return true;
        }

        File file = new File(dataFolder, args[0]);
        int success = 0;
        List<String> failed = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            MigrateHelper helper = new Gson().fromJson(reader, MigrateHelper.class);
            if (helper == null || helper.markers == null) {
                sender.sendMessage(Msg.of("§cFile is empty or malformed"));
                return true;
            }
            for (a entry : helper.markers) {
                String key = safeKey(entry);
                try {
                    String content = entry.options.tooltip.content;
                    String[] descSplit = content.split("<i>", 2);
                    String description = descSplit.length > 1 ? descSplit[1].replace("</i>", "").replace("</center>", "").trim() : "";
                    String name = content.length() > 11
                            ? content.substring(11).split("</b>", 2)[0]
                            : entry.data.key;
                    Marker marker = new Marker(entry.data.key, entry.data.point.x, entry.data.point.z,
                            entry.data.image, description, name);
                    if (MarkerUtils.addMarker(marker, args[1])) success++;
                    else failed.add(key);
                } catch (RuntimeException ex) {
                    MapMarkers.instance.getLogger().log(Level.WARNING, "Failed to migrate marker " + key, ex);
                    failed.add(key);
                }
            }
            sender.sendMessage(Msg.of("§6Successfully migrated " + success + " markers"));
            if (!failed.isEmpty()) {
                sender.sendMessage(Msg.of("§cFailed to migrate " + failed.size() + " markers"));
                failed.forEach(t -> sender.sendMessage(Msg.of("§c" + t)));
            }
            MarkerUtils.saveMarkersAsync();
        } catch (IOException | JsonSyntaxException e) {
            MapMarkers.instance.getLogger().log(Level.WARNING, "Migration failed", e);
            sender.sendMessage(Msg.of("§cEXCEPTION THROWN. CHECK CONSOLE"));
        }
        return true;
    }

    private static String safeKey(a entry) {
        try {
            return entry.data.key;
        } catch (NullPointerException ex) {
            return "<unknown>";
        }
    }

    public static class MigrateCommandCompleter implements TabCompleter {
        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            if (!sender.hasPermission("mapmarkers.migrate")) return Collections.emptyList();
            if (args.length <= 1) {
                String[] entries = dataFolder == null ? null : dataFolder.list();
                if (entries == null) return Collections.emptyList();
                return Arrays.stream(entries).filter(t -> t.contains(args[0])).toList();
            }
            if (args.length == 2) {
                return MarkerUtils.markersMap.keySet().stream().filter(t -> t.startsWith(args[1])).toList();
            }
            return Collections.emptyList();
        }
    }
}
