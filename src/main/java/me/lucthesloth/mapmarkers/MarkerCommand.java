package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarkerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("icons")) {
            sender.sendMessage(Msg.of(Msg.PREFIX).append(
                    Msg.link("§dClick for available Icons", "https://dominionserver.net/markers/", "Click to open link")));
            return true;
        }
        if (!sender.hasPermission("mapmarkers.marker")) return true;
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Msg.of("§cOnly players can use this command"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Msg.of("§cUsage: /marker <add|remove|edit|nearby|exit|help|icons> <args...>"));
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "remove" -> followRemoveChain(player, args);
            case "exit" -> followExitChain(player);
            case "add" -> followAddChain(player, args);
            case "edit" -> followEditChain(player, args);
            case "i" -> followInternalChain(player, args);
            case "nearby" -> followNearbyChain(player, args);
            case "help" -> followHelpChain(player);
            default -> false;
        };
    }

    private boolean isLayerValid(@NotNull String layer) {
        return MarkerUtils.markersMap.containsKey(layer);
    }

    private boolean inLayerWorld(@NotNull Player player, @NotNull String layer) {
        String world = MapMarkers.instance.getConfig().getString("layers." + layer + ".world_name");
        return world != null && player.getWorld().getName().equalsIgnoreCase(world);
    }

    private boolean followExitChain(@NotNull Player player) {
        if (InteractiveMarkerProcess.processes.remove(player.getUniqueId()) == null) {
            player.sendMessage(Msg.of("§cYou are not in a marker creation process"));
            return true;
        }
        player.sendMessage(Msg.of("§aYou have exited the marker creation process"));
        return true;
    }

    private boolean followAddChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (InteractiveMarkerProcess.processes.containsKey(player.getUniqueId())) {
            player.sendMessage(Msg.of("§cYou are already in a marker creation process"));
            return true;
        }
        if (args.length != 2 || !isLayerValid(args[1])) {
            player.sendMessage(Msg.of("§cUsage: /marker add <layerName>"));
            return true;
        }
        if (!inLayerWorld(player, args[1])) {
            player.sendMessage(Msg.of("§cYou are not in the same dimension as this layer!"));
            return true;
        }
        InteractiveMarkerProcess.processes.put(player.getUniqueId(), new InteractiveMarkerProcess(player, args[1]));
        return true;
    }

    private boolean followRemoveChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length < 3 || !isLayerValid(args[1])) {
            player.sendMessage(Msg.of("§cUsage: /marker remove <layer> <id>"));
            return true;
        }
        String id = MarkerUtils.normalize(args[2]);
        Marker found = MarkerUtils.markerExists(id, args[1]);
        if (found == null) {
            player.sendMessage(Msg.of("§cMarker with id §6" + id + " §cdoes not exist in layer " + args[1]));
            return true;
        }
        String resolvedId = found.getId();
        if (args.length == 3) {
            player.sendMessage(Msg.prefixed("").append(Msg.runCmd("§3Are you sure you want to delete marker §6" + resolvedId + "§3?",
                    "/marker remove " + args[1] + " " + resolvedId + " confirm", "Click to confirm")));
            return true;
        }
        if (args.length == 4 && args[3].equalsIgnoreCase("confirm")) {
            if (MarkerUtils.removeMarker(resolvedId, args[1])) {
                player.sendMessage(Msg.prefixed("§3Marker §6" + resolvedId + " §3has been removed"));
                MarkerUtils.saveMarkersAsync();
            } else {
                player.sendMessage(Msg.of("§c§l[MapMarkers] §r§cMarker §6" + resolvedId + " §ccould not be removed"));
            }
        }
        return true;
    }

    private boolean followEditChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length < 3 || !isLayerValid(args[1])) {
            player.sendMessage(Msg.of("§cUsage: /marker edit <layer> <id>"));
            return true;
        }
        if (!inLayerWorld(player, args[1])) {
            player.sendMessage(Msg.of("§cYou are not in the same dimension as this layer!"));
            return true;
        }
        String id = MarkerUtils.normalize(args[2]);
        Marker marker = MarkerUtils.markerExists(id, args[1]);
        if (marker == null) {
            player.sendMessage(Msg.of("§cMarker with id §6" + id + " §cdoes not exist in layer " + args[1]));
            return true;
        }
        InteractiveMarkerProcess.processes.put(player.getUniqueId(), new InteractiveMarkerProcess(player, marker, args[1]));
        return true;
    }

    private boolean followNearbyChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        // /marker nearby                  -> default radius, all layers in this world
        // /marker nearby <radius>         -> custom radius, all layers in this world
        // /marker nearby <layer> <radius> -> custom radius, single layer
        if (args.length <= 2) {
            int radius;
            try {
                radius = args.length == 2 ? Integer.parseInt(args[1]) : MapMarkers.instance.getConfig().getInt("global.nearbyRadius", 10);
            } catch (NumberFormatException ex) {
                player.sendMessage(Msg.of(String.format("§c%s §eis not a number!", args[1])));
                return true;
            }
            MarkerUtils.markersMap.keySet().stream()
                    .filter(layer -> inLayerWorld(player, layer))
                    .forEach(layer -> sendNearbyMarkers(player, radius, layer));
            return true;
        }
        if (args.length != 3 || !isLayerValid(args[1])) {
            player.sendMessage(Msg.of("§cUsage: /marker nearby [layer] [radius]"));
            return true;
        }
        if (!inLayerWorld(player, args[1])) {
            player.sendMessage(Msg.of("§cYou are not in the same dimension as this layer!"));
            return true;
        }
        int radius;
        try {
            radius = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage(Msg.of("§cUsage: /marker nearby [layer] [radius]"));
            return true;
        }
        sendNearbyMarkers(player, radius, args[1]);
        return true;
    }

    private void sendNearbyMarkers(Player player, int radius, String layer) {
        player.sendMessage(Msg.prefixed("§3Nearby markers in layer §9" + layer + "§3:"));
        for (Marker marker : MarkerUtils.nearbyMarkers(player, layer, radius)) {
            TextComponent.@NotNull Builder builder = Component.text();
            builder.append(Msg.prefixed("§6" + marker.getName() + " §3(" + marker.getId() + ") "));
            builder.append(Msg.runCmd(" §d[§cEdit§d] ", "/marker edit " + layer + " " + marker.getId(), "Click to edit"));
            builder.append(Msg.runCmd(" §d[§cRemove§d] ", "/marker remove " + layer + " " + marker.getId(), "Click to remove"));
            player.sendMessage(builder.build());
        }
    }

    private boolean followInternalChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        if (args.length <= 1) return true;
        InteractiveMarkerProcess process = InteractiveMarkerProcess.processes.get(player.getUniqueId());
        if (process == null) {
            player.sendMessage(Msg.of("§cYou are not in a marker creation process"));
            return true;
        }
        String sub = args[1].toLowerCase();
        if (sub.equals("confirm")) {
            process.handleMessage(player, "", InteractiveMarkerProcess.Command.CONFIRM);
            return true;
        }
        if (sub.equals("pos")) {
            process.handleMessage(player, "", InteractiveMarkerProcess.Command.POSITION);
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(Msg.of("§cUsage: /marker i <command> <...args>"));
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) builder.append(' ');
            builder.append(args[i]);
        }
        String built = builder.toString().trim();
        if (built.isEmpty()) {
            player.sendMessage(Msg.of("§cUsage: /marker i <command> <...args>"));
            return true;
        }
        switch (sub) {
            case "name" -> process.handleMessage(player, built, InteractiveMarkerProcess.Command.NAME);
            case "desc" -> process.handleMessage(player, built, InteractiveMarkerProcess.Command.DESCRIPTION);
            case "icon" -> process.handleMessage(player, built, InteractiveMarkerProcess.Command.ICON);
            default -> player.sendMessage(Msg.of("§cUnknown sub-command: " + sub));
        }
        return true;
    }

    private boolean followHelpChain(@NotNull Player player) {
        player.sendMessage(Msg.prefixed("§3MapMarker Commands:"));
        player.sendMessage(Msg.of("§6/marker add <layer> §r§3- §r§6Creates a marker"));
        player.sendMessage(Msg.of("§6/marker remove <layer> <id> §r§3- §r§6Removes a marker"));
        player.sendMessage(Msg.of("§6/marker edit <layer> <id> §r§3- §r§6Edits a marker"));
        player.sendMessage(Msg.of("§6/marker nearby [layer] [radius] §r§3- §r§6Lists nearby markers"));
        player.sendMessage(Msg.of("§6/marker exit §r§3- §r§6Exit interactive process"));
        player.sendMessage(Msg.of("§6/marker icons §r§3- §r§6List of available icons"));
        return true;
    }
}
