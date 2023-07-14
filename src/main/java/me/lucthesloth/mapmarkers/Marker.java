package me.lucthesloth.mapmarkers;

import com.google.gson.annotations.Expose;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.Vector;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Popup;
import net.pl3x.map.core.markers.option.Tooltip;

import java.io.File;

public class Marker {

    @Expose
    private String id = "";
    @Expose
    private String name = "";
    @Expose
    private double x = 0;
    @Expose
    private double z = 0;
    @Expose
    private String icon = "";
    @Expose
    private String description = "";
    private Icon _icon;
    public Marker(){}
    public Marker(String id, double x, double z, String icon, String description, String name) {
        this.id = id;
        this.x = x;
        this.z = z;
        this.icon = icon;
        this.description = description;
        this.name = name;
    }
    public Marker(double x, double z, String name) {
        this.x = x;
        this.z = z;
        this.name = name;
    }
    public Icon getIcon(){
        if (_icon == null) {
            if (icon == null || icon.isEmpty() || !Pl3xMap.api().getIconRegistry().has(icon)) {
                MapMarkers.instance.getLogger().warning("Marker " + id + " has invalid icon " + icon + ". Using default icon.");
                icon = MapMarkers.instance.getConfig().getString("marker.default_image", "marker-icon");
            }
            _icon = net.pl3x.map.core.markers.marker.Marker.icon(id, x, z, icon);
            Options.Builder options = Options.builder();
            options.tooltipOpacity(1.0);
            options.popupContent(MapMarkers.instance.getConfig().getString("marker.pattern", "<center><b>{title}</b><br><i>{description}</i></center>").replace("{title}", name)
                    .replace("{description}", description));
            options.popupShouldAutoClose(true);
            options.popupShouldAutoPan(false);
            options.tooltipContent(MapMarkers.instance.getConfig().getString("marker.pattern", "<center><b>{title}</b><br><i>{description}</i></center>").replace("{title}", name)
                    .replace("{description}", description));
            options.tooltipOffset(Point.of(0, -(MapMarkers.instance.getConfig().getInt("marker.size.z", 32)/4)));
            options.tooltipDirection(Tooltip.Direction.TOP);
            options.popupOffset(Point.of(MapMarkers.instance.getConfig().getInt("marker.offset.x", 0),MapMarkers.instance.getConfig().getInt("marker.offset.z", 0)));
            _icon.setSize(Vector.of(MapMarkers.instance.getConfig().getInt("marker.size.x", 32), MapMarkers.instance.getConfig().getInt("marker.size.z", 32)));
            _icon.setOptions(options.build());

        }
        return _icon;
    }

    //getters and setters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getX() {
        return x;
    }
    public double getZ() {
        return z;
    }

    public String getDescription() {
        return description;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
        _icon = null;
    }
    public void setX(double x) {
        this.x = x;
        _icon = null;
    }
    public void setZ(double z) {
        this.z = z;
        _icon = null;
    }
    public void setIcon(String icon) {
        this.icon = icon;
        _icon = null;
    }
    public void setDescription(String description) {
        this.description = description;
        _icon = null;
    }
    public String getIconName(){
        return icon;
    }

}
