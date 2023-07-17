package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.pl3x.map.core.Pl3xMap;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class InteractiveMarkerProcess {
    public static HashMap<Player, InteractiveMarkerProcess> processes = new HashMap<>();
    Player player;
    Marker marker;
    String oldID;
    int step = 0x10;
    public InteractiveMarkerProcess(Player player) {
        marker = new Marker();
        marker.setX(player.getLocation().getBlockX());
        marker.setZ(player.getLocation().getBlockZ());
        this.setStepBit(5);
        this.player = player;
        sendInformationMessage();

    }
    public InteractiveMarkerProcess(Player player, String name) {
        marker = new Marker(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), name);
        marker.setId(MarkerUtils.normalize(name));
        this.setStepBit(0);
        this.setStepBit(5);
        this.player = player;
        sendInformationMessage();
    }
    public InteractiveMarkerProcess(Player player, Marker marker) {
        this.marker = marker;
        step = 0x1F;
        this.player = player;
        this.oldID = marker.getId();
        sendInformationMessage();
    }
    //Test
    public void sendInformationMessage(){
        player.sendMessage(Component.text("\n\n\n§3[§9MapMarkers§3] §r§aInteractive Marker Creation"));
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3ID => §6" + marker.getId()));
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Name => §6" + marker.getName(),
                Style.style().clickEvent(ClickEvent.suggestCommand("/marker i name "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to change name"))).build()));
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Description => §6" + marker.getDescription(),
                Style.style().clickEvent(ClickEvent.suggestCommand("/marker i desc "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to change description"))).build()));
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Icon => §6" + marker.getIconName(),
                Style.style().clickEvent(ClickEvent.suggestCommand("/marker i icon "))
                .hoverEvent(HoverEvent.showText(Component.text("Click to change icon"))).build()));
        player.sendMessage(Component.text("§3[§9MapMarkers§3] §r§3Position => §6" + marker.getX() + ", " + marker.getZ(),
                Style.style().clickEvent(ClickEvent.runCommand("/marker i pos"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to set position to current player position"))).build()));
        TextComponent.Builder Builder = Component.text();
        Builder.append(Component.text("§3[§9MapMarkers§3] "));
        if ((step & 0x17) == 0x17 || (step >> 3 & 1) == 1){
            Builder.append(Component.text("§r§c[Confirm] ", Style.style().clickEvent(ClickEvent.runCommand("/marker i confirm"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to confirm"))).build()));
        }
        Builder.append(Component.text("§r§9[Cancel] ", Style.style().clickEvent(ClickEvent.runCommand("/marker exit"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to cancel"))).build()));
        player.sendMessage(Builder.build());
    }

    /**
     * First bit = Name set
     * Second bit = Description set
     * Third bit = Icon set
     * Fifth bit = Edit mode
     */
    public void setStepBit(int step){
        this.step |= (1 << step);
    }
    public void handleMessage(String message, int command){
        switch (command) {
            case 1 -> {
                if (message.length() <= MapMarkers.instance.getConfig().getInt("marker.max_name_length", 40)) {
                    if (MarkerUtils.markerExists(MarkerUtils.normalize(message)) != null) {
                        player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§cMarker already exists, please enter a name for the marker")));
                        return;
                    }
                    marker.setName(message);
                    marker.setId(MarkerUtils.normalize(message));
                    setStepBit(0);
                    sendInformationMessage();
                } else {
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§cName is too long, please enter a name for the marker (max §r§d" + MapMarkers.instance.getConfig().getInt("marker.max_name_length", 40) + "§r§c characters)")));
                }
            }
            case 2 -> {
                if (message.length() <= MapMarkers.instance.getConfig().getInt("marker.max_description_length", 80)) {
                    marker.setDescription(message);
                    setStepBit(1);
                    sendInformationMessage();
                } else {
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§cDescription is too long, please enter a description for the marker (max §r§d" + MapMarkers.instance.getConfig().getInt("marker.max_description_length", 80) + "§r§c characters)")));
                }
            }
            case 3 -> {
                if (Pl3xMap.api().getIconRegistry().has(message)) {
                    marker.setIcon(message);
                    setStepBit(2);
                    sendInformationMessage();
                } else {
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§cIcon does not exist, please enter the icon id for the marker")));
                }
            }
            case 4 -> {
                if (marker.getId() == null) {
                    marker.setId(MarkerUtils.normalize(marker.getName()));
                }
                if (marker.getName() == null || marker.getDescription() == null || marker.getIcon() == null
                        || marker.getName().isEmpty() || marker.getDescription().isEmpty() || marker.getIconName().isEmpty()) {
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§cPlease fill out all fields")));
                    sendInformationMessage();
                    return;
                }
                if (oldID == null) {
                    MarkerUtils.addMarker(marker);
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§6Marker created")));
                } else {
                    MarkerUtils.removeMarker(oldID);
                    MarkerUtils.addMarker(marker);
                    MarkerUtils.saveMarkers();
                    player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§6Marker edited")));
                }
                processes.remove(player);
            }
            case 5 -> {
                marker.setX(player.getLocation().getBlockX());
                marker.setZ(player.getLocation().getBlockZ());
                player.sendMessage(Component.text(("§3[§9MapMarkers§3] §r§aPosition set to §3" + marker.getX() + ", " + marker.getZ())));
                sendInformationMessage();
            }
            default -> {
            }
        }
    }

}
