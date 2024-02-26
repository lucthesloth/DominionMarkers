package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarkerCommand implements CommandExecutor {
    public MarkerCommand() {

    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("icons")) {
            commandSender.sendMessage(Component.text("§3[§9MapMarkers§3] §r§dClick for available Icons").style(
                    Style.style().clickEvent(ClickEvent.openUrl("https://dominionserver.net/markers/"))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to open link"))).build()
            ));
            return true;
        }
        if (!commandSender.hasPermission("mapmarkers.marker"))
            return true;
        if (!(commandSender instanceof Player player)){
            commandSender.sendMessage(Component.text("§cOnly players can use this command"));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(Component.text("§cUsage: /marker <add|remove|edit|exit> <args...>"));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove"))
            return followRemoveChain(player, args);
        if (args[0].equalsIgnoreCase("exit"))
            return followExitChain(player);
        if (args[0].equalsIgnoreCase("add"))
            return followAddChain(player, args);
        if (args[0].equalsIgnoreCase("edit"))
            return followEditChain(player, args);
        if (args[0].equalsIgnoreCase("i"))
            return followInternalChain(player, args);
        if (args[0].equalsIgnoreCase("nearby"))
            return followNearbyChain(player, args);
        if (args[0].equalsIgnoreCase("help"))
            return followHelpChain(player);
        return false;
    }
    private boolean isLayerValid(@NotNull String layer){
        return MarkerUtils.markersMap.containsKey(layer);
    }
    private boolean followExitChain(@NotNull Player player){
        if (!InteractiveMarkerProcess.processes.containsKey(player)){
            player.sendMessage(Component.text("§cYou are not in a marker creation process"));
            return true;
        }
        InteractiveMarkerProcess.processes.remove(player);
        player.sendMessage(Component.text("§aYou have exited the marker creation process"));
        return false;
    }
    private boolean followAddChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (InteractiveMarkerProcess.processes.containsKey(player)){
            player.sendMessage(Component.text("§cYou are already in a marker creation process"));
            return true;
        }
        if (args.length == 2 && isLayerValid(args[1])){
            if (!player.getWorld().getName().equalsIgnoreCase(MapMarkers.instance.getConfig().getString(
                    "layers." + args[1] + ".world_name"
            ))){
                player.sendMessage(Component.text("§cYou are not in the same dimension as this layer!"));
                return false;
            }
            InteractiveMarkerProcess.processes.put(player, new InteractiveMarkerProcess(player, args[1]));
            return true;
        }
        player.sendMessage(Component.text("§cUsage /marker add <layerName>"));
        return false;
    }
    private boolean followRemoveChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length == 1 || !isLayerValid(args[1])){
            player.sendMessage(Component.text("§cUsage: /marker remove <layer> <name>"));
            return true;
        }
        String id = MarkerUtils.normalize(args[2]);
        if (MarkerUtils.markerExists(id, args[1]) == null){
            player.sendMessage(Component.text("§cMarker with id §6" + id + " §cdoes not exist in layer " + args[1]));
            return true;
        }
        if (args.length == 3){
            player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Are you sure you want to delete marker §6" + id + "§3?")
                    .style(Style.style().clickEvent(ClickEvent.runCommand("/marker remove " + args[1] + " " + id + " confirm"))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to confirm")))));
            return false;
        }
        if (args.length == 4 && args[3].equalsIgnoreCase("confirm")){
            if (MarkerUtils.removeMarker(id, args[1])){
                player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Marker §6" + id + " §3has been removed"));
                MarkerUtils.saveMarkers();
            } else {
                player.sendMessage(Component.text("§c§l[MapMarkers] §r§cMarker §6" + id + " §ccould not be removed"));
            }
        }
        return false;
    }
    private boolean followEditChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length == 1 || !isLayerValid(args[1])){
            player.sendMessage(Component.text("§cUsage: /marker edit <layer> <name>"));
            return true;
        }
        String id = MarkerUtils.normalize(args[2]);
        Marker marker = MarkerUtils.markerExists(id, args[1]);
        if (marker == null){
            player.sendMessage(Component.text("§cMarker with id §6" + id + " §cdoes not exist in layer " + args[1]));
            return true;
        }
        InteractiveMarkerProcess.processes.put(player, new InteractiveMarkerProcess(player, marker, args[1]));
        return false;
    }
    private boolean followNearbyChain(@NotNull Player player, @NotNull String @NotNull [] args) {
        Integer radius = null;
        if (args.length != 3 || !isLayerValid(args[1])){
            player.sendMessage(Component.text("§cUsage: /marker nearby <layer> <radius>"));
            return true;
        }

        try {
            radius = Integer.parseInt(args[2]);
        } catch (NumberFormatException ignored){
            player.sendMessage(Component.text("§cUsage: /marker nearby <layer> <radius>"));
            return true;
        }

        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Nearby markers:"));
        for (Marker marker : MarkerUtils.nearbyMarkers(player, args[1], radius)){
            TextComponent.@NotNull Builder builder = Component.text();
            builder.append(Component.text("§3[§9MapMarkers§3] §r§6" + marker.getName() + " §3(" + marker.getId() + ") "));
            builder.append(Component.text(" §d[§cEdit§d] ").style(Style.style().clickEvent(ClickEvent.runCommand("/marker edit " + args[1] + " " + marker.getId()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to edit")))));
            builder.append(Component.text(" §d[§cRemove§d] ").style(Style.style().clickEvent(ClickEvent.runCommand("/marker remove " + args[1] + " "  + marker.getId()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to remove")))));
            player.sendMessage(builder.build());
        }
        return false;
    }
    private boolean followInternalChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length <= 1) return true;
        InteractiveMarkerProcess process = InteractiveMarkerProcess.processes.get(player);
        if (process == null){
            player.sendMessage(Component.text("§cYou are not in a marker creation process"));
            return true;
        }
        if (args[1].equalsIgnoreCase("confirm")){
            process.handleMessage("", 4);
            return false;
        }
        if (args[1].equalsIgnoreCase("pos")){
            process.handleMessage("", 5);
            return false;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1)
                builder.append(" ");
        }
        String BuiltArgument = builder.toString();
        if (BuiltArgument.trim().length() < 1){
            player.sendMessage(Component.text("§cUsage: /marker i <command> <...args>"));
            return true;
        }
        if (args[1].equalsIgnoreCase("name")){
            process.handleMessage(BuiltArgument, 1);
            return false;
        }
        if (args[1].equalsIgnoreCase("desc")){
            process.handleMessage(BuiltArgument, 2);
            return false;
        }
        if (args[1].equalsIgnoreCase("icon")){
            process.handleMessage(BuiltArgument, 3);
            return false;
        }
        return false;
    }
    private boolean followHelpChain(@NotNull Player player){
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3MapMarker Commands:"));
        player.sendMessage(Component.text("§6/marker add <layer> §r§3- §r§6Creates a marker"));
        player.sendMessage(Component.text("§6/marker remove <layer> <id> §r§3- §r§6Removes a marker"));
        player.sendMessage(Component.text("§6/marker edit <layer> <id> §r§3- §r§6Edits a marker"));
        player.sendMessage(Component.text("§6/marker nearby <layer> <radius> §r§3- §r§6Lists nearby markers"));
        player.sendMessage(Component.text("§6/marker exit §r§3- §r§6Exit interactive process"));
        player.sendMessage(Component.text("§6/marker icons §r§3- §r§6List of available icons"));
        return false;
    }
}
