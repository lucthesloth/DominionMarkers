package me.lucthesloth.mapmarkers;

import net.pl3x.map.core.Pl3xMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MarkerCommandCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission("mapmarkers.marker") || !(commandSender instanceof Player))
            return null;
        if (strings.length <= 1)
            return Stream.of("add", "remove", "edit", "exit", "nearby", "icons", "help").filter(string -> string.startsWith(strings[0])).toList();
        if (strings.length == 2 && (strings[0].equalsIgnoreCase("add") || strings[0].equalsIgnoreCase("remove")
        || strings[0].equalsIgnoreCase("edit") || strings[0].equalsIgnoreCase("nearby"))){
            List<String> e = new ArrayList<>();
            MarkerUtils.markersMap.keySet().forEach(t -> {
                if (((Player) commandSender).getWorld().getName().equalsIgnoreCase(MapMarkers.instance.getConfig().getString("layers." + t + ".world_name")))
                    e.add(t);
            });
            return e;
        }
        if (strings.length == 3 && strings[0].equalsIgnoreCase("remove"))
            return MarkerUtils.markersMap.getOrDefault(strings[1], Collections.emptyList()).stream().map(Marker::getId).filter(id -> id.startsWith(strings[2])).toList();
        if (strings.length == 4 && strings[0].equalsIgnoreCase("remove"))
            return List.of("confirm");
        if (strings.length == 3 && strings[0].equalsIgnoreCase("edit"))
            return MarkerUtils.markersMap.getOrDefault(strings[1], Collections.emptyList()).stream().map(Marker::getId).filter(k -> k.contains(strings[2])).toList();
        if (strings.length == 3 && strings[0].equalsIgnoreCase("nearby"))
            return Stream.of("10", "20", "30", "40", "50", "60", "70", "80", "90", "100").filter(string -> string.startsWith(strings[2])).toList();
        if (strings.length == 2 && strings[0].equalsIgnoreCase("i"))
            return Stream.of("name", "icon", "desc", "pos", "confirm").filter(string -> string.startsWith(strings[1])).toList();
        if (strings.length == 3 && strings[0].equalsIgnoreCase("i") && strings[1].equalsIgnoreCase("icon"))
            return Pl3xMap.api().getIconRegistry().entrySet().stream().map(Map.Entry::getKey).filter(string -> string.contains(strings[2])).toList();
        return Collections.emptyList();
    }
}
