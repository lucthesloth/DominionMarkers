package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarkerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("mapmarkers.marker"))
            return true;
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage(Component.text("§cOnly players can use this command"));
            return true;
        }
        Player player = (Player) commandSender;
        if (args.length == 0) {
            commandSender.sendMessage(Component.text("§cUsage: /marker <add|remove|edit|exit> <args...>"));
            return true;
        }
        if (args[0].equalsIgnoreCase("remove"))
            return followRemoveChain(player, args);
        if (args[0].equalsIgnoreCase("exit"))
            return followExitChain(player, args);
        if (args[0].equalsIgnoreCase("add"))
            return followAddChain(player, args);
        if (args[0].equalsIgnoreCase("edit"))
            return followEditChain(player, args);
        if (args[0].equalsIgnoreCase("i"))
            return followInternalChain(player, args);

        return false;
    }
    private boolean followExitChain(@NotNull Player player, @NotNull String @NotNull [] args){
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
        if (args.length == 1){
            InteractiveMarkerProcess.processes.put(player, new InteractiveMarkerProcess(player));
            return true;
        }
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            name.append(args[i]);
            if (i != args.length - 1) name.append(" ");
        }
        String id = MarkerUtils.normalize(name.toString());
        if (MarkerUtils.markerExists(id) != null){
            player.sendMessage(Component.text("§cMarker with id " + id + " already exists"));
            return true;
        }
        InteractiveMarkerProcess.processes.put(player, new InteractiveMarkerProcess(player, name.toString()));
        return false;
    }
    private boolean followRemoveChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length == 1){
            player.sendMessage(Component.text("§cUsage: /marker remove <name>"));
            return true;
        }
        String id = MarkerUtils.normalize(args[1]);
        if (MarkerUtils.markerExists(id) == null){
            player.sendMessage(Component.text("§cMarker with id " + id + " does not exist"));
            return true;
        }
        if (args.length == 2){
            player.sendMessage(Component.text("§a§l[MapMarkers] §r§aAre you sure you want to delete marker " + id + "?")
                    .style(Style.style().clickEvent(ClickEvent.runCommand("/marker remove " + id + " confirm"))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to confirm")))));
            return false;
        }
        if (args.length == 3 && args[2].equalsIgnoreCase("confirm")){
            if (MarkerUtils.removeMarker(id)){
                player.sendMessage(Component.text("§a§l[MapMarkers] §r§aMarker " + id + " has been removed"));
            } else {
                player.sendMessage(Component.text("§c§l[MapMarkers] §r§cMarker " + id + " could not be removed"));
            }
        }
        return false;
    }
    private boolean followEditChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length == 1){
            player.sendMessage(Component.text("§cUsage: /marker edit <name>"));
            return true;
        }
        String id = MarkerUtils.normalize(args[1]);
        Marker marker = MarkerUtils.markerExists(id);
        if (marker == null){
            player.sendMessage(Component.text("§cMarker with id " + id + " does not exist"));
            return true;
        }
        InteractiveMarkerProcess.processes.put(player, new InteractiveMarkerProcess(player, marker));
        return false;
    }
    private boolean followInternalChain(@NotNull Player player, @NotNull String @NotNull [] args){
        if (args.length == 1) return true;
        InteractiveMarkerProcess process = InteractiveMarkerProcess.processes.get(player);
        if (process == null){
            player.sendMessage(Component.text("§cYou are not in a marker creation process"));
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1)
                builder.append(" ");
        }
        String BuiltArgument = builder.toString();
        process.handleMessage(BuiltArgument);
        return false;
    }
}
