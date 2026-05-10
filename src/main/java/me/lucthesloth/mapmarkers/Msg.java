package me.lucthesloth.mapmarkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Msg {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    public static final String PREFIX = "§3[§9MapMarkers§3] §r";

    private Msg() {}

    public static Component of(String legacy) {
        return LEGACY.deserialize(legacy);
    }

    public static Component prefixed(String legacy) {
        return LEGACY.deserialize(PREFIX + legacy);
    }

    public static Component link(String legacy, String url, String hover) {
        return of(legacy).style(Style.style()
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(of(hover)))
                .build());
    }

    public static Component runCmd(String legacy, String command, String hover) {
        return of(legacy).style(Style.style()
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(of(hover)))
                .build());
    }

    public static Component suggestCmd(String legacy, String command, String hover) {
        return of(legacy).style(Style.style()
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(of(hover)))
                .build());
    }
}
