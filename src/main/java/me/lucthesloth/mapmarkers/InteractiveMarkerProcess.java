package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.pl3x.map.core.Pl3xMap;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InteractiveMarkerProcess {
    public static final Map<UUID, InteractiveMarkerProcess> processes = new ConcurrentHashMap<>();

    public enum Step { NAME, DESCRIPTION, ICON, EDIT_MODE }
    public enum Command { NAME, DESCRIPTION, ICON, CONFIRM, POSITION }

    private final UUID playerId;
    private final EnumSet<Step> completedSteps = EnumSet.noneOf(Step.class);
    final Marker marker;
    final String oldID;
    final String layer;

    public InteractiveMarkerProcess(Player player, String layer) {
        this.playerId = player.getUniqueId();
        this.marker = new Marker();
        this.marker.setX(player.getLocation().getBlockX());
        this.marker.setZ(player.getLocation().getBlockZ());
        this.layer = layer;
        this.oldID = null;
        sendInformationMessage(player);
    }

    public InteractiveMarkerProcess(Player player, Marker marker, String layer) {
        this.playerId = player.getUniqueId();
        this.marker = marker;
        this.layer = layer;
        this.oldID = marker.getId();
        completedSteps.add(Step.NAME);
        completedSteps.add(Step.DESCRIPTION);
        completedSteps.add(Step.ICON);
        completedSteps.add(Step.EDIT_MODE);
        sendInformationMessage(player);
    }

    public void sendInformationMessage(Player player) {
        player.sendMessage(Component.text("\n\n\n").append(Msg.prefixed("§aInteractive Marker Creation")));
        player.sendMessage(Msg.prefixed("§5LAYER => §6" + layer));
        player.sendMessage(Msg.prefixed("§3ID => §6" + marker.getId()));
        player.sendMessage(Msg.prefixed("").append(Msg.suggestCmd("§3Name => §6" + marker.getName(),
                "/marker i name ", "Click to change name")));
        player.sendMessage(Msg.prefixed("").append(Msg.suggestCmd("§3Description => §6" + marker.getDescription(),
                "/marker i desc ", "Click to change description")));
        player.sendMessage(Msg.prefixed("").append(Msg.suggestCmd("§3Icon => §6" + marker.getIconName(),
                "/marker i icon ", "Click to change icon")));
        player.sendMessage(Msg.prefixed("").append(Msg.runCmd("§3Position => §6" + marker.getX() + ", " + marker.getZ(),
                "/marker i pos", "Click to set position to current player position")));

        TextComponent.Builder builder = Component.text();
        builder.append(Msg.prefixed(""));
        if (canConfirm()) {
            builder.append(Msg.runCmd("§c[Confirm] ", "/marker i confirm", "Click to confirm"));
        }
        builder.append(Msg.runCmd("§9[Cancel] ", "/marker exit", "Click to cancel"));
        player.sendMessage(builder.build());
    }

    private boolean canConfirm() {
        return completedSteps.contains(Step.EDIT_MODE)
                || (completedSteps.contains(Step.NAME)
                && completedSteps.contains(Step.DESCRIPTION)
                && completedSteps.contains(Step.ICON));
    }

    public void handleMessage(Player player, String message, Command command) {
        switch (command) {
            case NAME -> handleName(player, message);
            case DESCRIPTION -> handleDescription(player, message);
            case ICON -> handleIcon(player, message);
            case CONFIRM -> handleConfirm(player);
            case POSITION -> handlePosition(player);
        }
    }

    private void handleName(Player player, String message) {
        int max = MapMarkers.instance.getConfig().getInt("marker.max_name_length", 40);
        if (message.length() > max) {
            player.sendMessage(Msg.prefixed("§cName too long (max §d" + max + "§c characters)"));
            return;
        }
        if (MarkerUtils.markerExistsEqual(MarkerUtils.normalize(message), layer) != null) {
            player.sendMessage(Msg.prefixed("§cMarker already exists, choose another name"));
            return;
        }
        marker.setName(message.toUpperCase());
        marker.setId(MarkerUtils.normalize(message));
        completedSteps.add(Step.NAME);
        sendInformationMessage(player);
    }

    private void handleDescription(Player player, String message) {
        int max = MapMarkers.instance.getConfig().getInt("marker.max_description_length", 80);
        if (message.length() > max) {
            player.sendMessage(Msg.prefixed("§cDescription too long (max §d" + max + "§c characters)"));
            return;
        }
        marker.setDescription(message);
        completedSteps.add(Step.DESCRIPTION);
        sendInformationMessage(player);
    }

    private void handleIcon(Player player, String message) {
        if (!Pl3xMap.api().getIconRegistry().has(message)) {
            player.sendMessage(Msg.prefixed("§cIcon does not exist"));
            return;
        }
        marker.setIcon(message);
        completedSteps.add(Step.ICON);
        sendInformationMessage(player);
    }

    private void handleConfirm(Player player) {
        marker.setId(MarkerUtils.normalize(marker.getName()));
        if (marker.getName() == null || marker.getDescription() == null || marker.getIconName() == null
                || marker.getName().isEmpty() || marker.getDescription().isEmpty() || marker.getIconName().isEmpty()) {
            player.sendMessage(Msg.prefixed("§cPlease fill out all fields"));
            sendInformationMessage(player);
            return;
        }
        if (oldID == null) {
            if (MarkerUtils.addMarker(marker, layer)) {
                player.sendMessage(Msg.prefixed("§6Marker created"));
            } else {
                player.sendMessage(Msg.prefixed("§cFailed to create marker (duplicate or unknown layer)"));
                return;
            }
        } else {
            MarkerUtils.removeMarker(oldID, layer);
            MarkerUtils.addMarker(marker, layer);
            player.sendMessage(Msg.prefixed("§6Marker edited"));
        }
        processes.remove(playerId);
        MarkerUtils.saveMarkersAsync();
    }

    private void handlePosition(Player player) {
        marker.setX(player.getLocation().getBlockX());
        marker.setZ(player.getLocation().getBlockZ());
        player.sendMessage(Msg.prefixed("§aPosition set to §3" + marker.getX() + ", " + marker.getZ()));
        sendInformationMessage(player);
    }
}
