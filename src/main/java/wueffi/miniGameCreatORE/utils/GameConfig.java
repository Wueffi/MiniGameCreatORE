package wueffi.miniGameCreatORE.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import wueffi.miniGameCreatORE.MiniGameCreatORE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class GameConfig {

    private final MiniGameCreatORE plugin;
    private final File file;
    private final FileConfiguration config;

    public GameConfig(MiniGameCreatORE plugin, File worldFolder, String gameName, int minPlayers) {
        this.plugin = plugin;

        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }

        this.file = new File(worldFolder, "config.yml");

        if (!file.exists()) {
            try (var in = plugin.getResource("config.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        config.set("game.name", gameName);
        config.set("game.minPlayers", minPlayers);

        save();
    }

    public GameConfig(MiniGameCreatORE plugin, File worldFolder) {
        this.plugin = plugin;

        this.file = new File(worldFolder, "config.yml");

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public void addListOption(String path, String value) {
        List<String> list = config.getStringList(path);
        if (!list.contains(value)) {
            list.add(value);
            config.set(path, list);
        }
    }

    public void removeListOption(String path, String value) {
        List<String> list = config.getStringList(path);
        if (list.remove(value)) {
            config.set(path, list);
        }
    }

    public boolean isInList(String path, String value) {
        return config.getStringList(path).contains(value);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}