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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mapmarkers.marker") || !(sender instanceof Player player)) return null;
        if (args.length <= 1) {
            return Stream.of("add", "remove", "edit", "exit", "nearby", "icons", "help")
                    .filter(string -> string.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2 && (sub.equals("add") || sub.equals("remove") || sub.equals("edit") || sub.equals("nearby"))) {
            List<String> result = new ArrayList<>();
            MarkerUtils.markersMap.keySet().forEach(layer -> {
                String world = MapMarkers.instance.getConfig().getString("layers." + layer + ".world_name");
                if (world != null && player.getWorld().getName().equalsIgnoreCase(world)) {
                    result.add(layer);
                }
            });
            // /marker nearby also accepts a bare radius as arg1
            if (sub.equals("nearby")) {
                Stream.of("10", "20", "30", "40", "50").forEach(result::add);
            }
            return result.stream().filter(v -> v.startsWith(args[1])).toList();
        }
        if (args.length == 3 && (sub.equals("remove") || sub.equals("edit"))) {
            return MarkerUtils.markersMap.getOrDefault(args[1], Collections.emptyList()).stream()
                    .map(Marker::getId).filter(id -> id.startsWith(args[2])).toList();
        }
        if (args.length == 4 && sub.equals("remove")) {
            return List.of("confirm");
        }
        if (args.length == 3 && sub.equals("nearby")) {
            return Stream.of("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
                    .filter(string -> string.startsWith(args[2])).toList();
        }
        if (args.length == 2 && sub.equals("i")) {
            return Stream.of("name", "icon", "desc", "pos", "confirm")
                    .filter(string -> string.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 3 && sub.equals("i") && args[1].equalsIgnoreCase("icon")) {
            return Pl3xMap.api().getIconRegistry().entrySet().stream()
                    .map(Map.Entry::getKey).filter(string -> string.contains(args[2])).toList();
        }
        return Collections.emptyList();
    }
}
