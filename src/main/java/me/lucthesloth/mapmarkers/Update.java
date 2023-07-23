package me.lucthesloth.mapmarkers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.lucthesloth.mapmarkers.util.MarkerUtils;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Update {
    public static String repo = "https://github.com/lucthesloth/DominionMarkers/actions/workflows/maven-publish.yml";
    private boolean updateAvailable = false;
    private String latestVersion;
    private final MapMarkers plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Update(MapMarkers instance) throws IOException {
        plugin = instance;
        checkForUpdate();
    }
    private void checkForUpdate() throws IOException {
        final URL url = new URL("https://api.github.com/repos/lucthesloth/DominionMarkers/actions/artifacts?per_page=1&page=1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/vnd.github+json");
        con.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        int status = con.getResponseCode();
        if (status == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            response t = gson.fromJson(content.toString(), response.class);
            if (!t.artifacts[0].expired && !t.artifacts[0].name.contains(plugin.getPluginMeta().getVersion())) {
                updateAvailable = true;
                latestVersion = t.artifacts[0].name;
            }
        }
    }
    public String getLatestVersion() {
        return updateAvailable ? latestVersion : null;
    }
    public BuildableComponent<TextComponent, TextComponent.Builder> getUpdateMessage() {
        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();
        builder.append(MarkerUtils.prefix);
        builder.append(Component.text("&6An update is available. Please &c[Click Here]&6 to download the latest version.")
                .style(style -> style.hoverEvent(Component.text("Click to download the latest version"))
                        .clickEvent(ClickEvent.openUrl(repo))));
        return builder.build();
    }
    public String getUpdateMessageStr(){
        return updateAvailable ? String.format("&6An update is available. Please head to &3%s&6 to download the latest version.", repo) : null;
    }
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
}
class response{
    int total_count;
    artifact[] artifacts;
}
class artifact {
    String id;
    String name;
    boolean expired;
}