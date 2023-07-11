package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.pl3x.map.core.markers.marker.Icon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MigrateCommand implements CommandExecutor {
    public static File dataFolder;
    public MigrateCommand(){
        dataFolder = new File(MapMarkers.instance.getDataFolder(), "migrate");
        dataFolder.mkdirs();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("mapmarkers.migrate"))
            return true;
        if (strings.length == 0) {
            commandSender.sendMessage(Component.text("§cUsage: /migrate <filename>"));
            return true;
        }
        if (Arrays.stream(dataFolder.list()).anyMatch(t->t.equalsIgnoreCase(strings[0]))) {
            File file = new File(dataFolder, strings[0]);
            AtomicInteger success = new AtomicInteger(0);
            List<String> failed = Collections.synchronizedList(Arrays.asList(new String[0]));
            try {
                MigrateHelper helper = new Gson().fromJson(new FileReader(file), MigrateHelper.class);
                helper.markers.forEach(marker -> {
                    if (MarkerUtils.addMarker(new Marker(marker.data.key, (double)marker.data.point.x(), (double)marker.data.point.z(), marker.data.image,
                            marker.options.tooltip.content.split("<i>", 2)[1],
                            marker.options.tooltip.content.substring(11).split("</b>")[0])))
                        success.getAndIncrement();
                    else
                        failed.add(marker.data.key);
                });
                commandSender.sendMessage(Component.text("§6Successfully migrated " + success.get() + " markers"));
                if (failed.size() > 0)
                    commandSender.sendMessage(Component.text("§cFailed to migrate " + failed.size() + " markers"));
                failed.forEach(t->commandSender.sendMessage(Component.text("§c" + t)));
            } catch (FileNotFoundException e) {
                commandSender.sendMessage(Component.text("§cEXCEPTION THROWN. CHECK CONSOLE"));
            }
            return false;
        } else {
            commandSender.sendMessage(Component.text("§cFile not found"));
            return true;
        }
    }
    public static class MigrateCommandCompleter implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            if (!commandSender.hasPermission("mapmarkers.migrate"))
                return Collections.EMPTY_LIST;
            if (strings.length <= 1)
                return Arrays.stream(dataFolder.list()).filter(t->t.contains(strings[0])).toList();
            return null;
        }
    }

}
