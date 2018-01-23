package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LanguageConfiguration {

    private Map<String, String> values = new HashMap<>();

    private ShopChest plugin;
    private boolean showMessages;

    public LanguageConfiguration(ShopChest plugin, boolean showMessages) {
        this.plugin = plugin;
        this.showMessages = showMessages;
    }

    public String getString(String path, String def) {
        if (values.containsKey(path)) {
            return values.get(path);
        } else {
            if (showMessages) plugin.getLogger().info("Could not find translation for \"" + path + "\" in selected language file. Using default translation (" + def + ")");
            return def;
        }

    }

    public void load(Path file) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            loadFromBufferedReader(br);
        }
    }

    public void loadFromReader(Reader r) throws IOException {
        try (BufferedReader br = new BufferedReader(r)) {
            loadFromBufferedReader(br);
        }

    }

    public void loadFromBufferedReader(BufferedReader br) {
        br.lines().forEachOrdered(this::loadLine);
    }

    public void loadFromString(String s) {
        Arrays.stream(s.split("\n")).forEach(this::loadLine);
    }

    private void loadLine(String line) {
        if (line.isEmpty() || line.charAt(0) == '#') {
            return;
        }

        int index = line.indexOf('=');
        if (index == -1) {
            return;
        }
        values.put(line.substring(0, index), line.substring(index + 1));
    }

}
