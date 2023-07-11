package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class InteractiveMarkerProcess {
    public static HashMap<Player, InteractiveMarkerProcess> processes = new HashMap<>();
    Player player;
    Marker marker;
    String oldID;
    int step = 0x0;
    public InteractiveMarkerProcess(Player player) {
        this.player = player;
    }
    public InteractiveMarkerProcess(Player player, String name) {
        marker = new Marker(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), name);
        marker.setId(MarkerUtils.normalize(name));
        this.setStepBit(0);
        this.player = player;
    }
    public InteractiveMarkerProcess(Player player, Marker marker) {
        this.marker = marker;
        step = 0x1F;
        this.player = player;
        this.oldID = marker.getId();
        sendMessage();
    }
    //Test
    public void sendInformationMessage(){
        if ((step >> 4 & 1) != 0) {
            player.sendMessage(Component.text(("§a§l[MapMarkers] §r§aMarker Edit Mode for marker " + marker.getName())));
            player.sendMessage(Component.text("§a§l[MapMarkers] §r§aName => " + marker.getName()));
            player.sendMessage(Component.text("§a§l[MapMarkers] §r§aDescription => " + marker.getDescription()));
            player.sendMessage(Component.text("§a§l[MapMarkers] §r§aIcon => " + marker.getIcon()));
            player.sendMessage(Component.text("§a§l[MapMarkers] §r§aPosition => " + marker.getX() + ", " + marker.getZ(),
                Style.style().clickEvent(ClickEvent.runCommand("/dominionmarker edit position"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to set position to current player position"))).build()));
        }
    }
    public void sendMessage(){
        if ((step & 1) == 0){
            player.sendMessage(Component.text(("§a§l[MapMarkers] §r§aPlease enter a name for the marker")));
            return;
        }
        if ((step>>1 & 1) == 0){
            player.sendMessage(Component.text(("§a§l[MapMarkers] §r§aPlease enter a description for the marker")));
            return;
        }
        if ((step>>2 & 1) == 0){
            player.sendMessage(Component.text(("§a§l[MapMarkers] §r§aPlease enter the icon id for the marker")));
            return;
        }
    }
    /**
     * First bit = Name set
     * Second bit = Description set
     * Third bit = Icon set
     * Fifth bit = Edit mode
     */
    public void resetStep(int step){
        this.step &= ~(1 << step);
    }
    public void setStepBit(int step){
        this.step |= (1 << step);
    }
    public void handleMessage(String message){

    }

}
