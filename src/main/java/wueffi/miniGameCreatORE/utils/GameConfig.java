package wueffi.miniGameCreatORE.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import wueffi.miniGameCreatORE.MiniGameCreatORE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
            saveWithQuotedLists();
        }
    }

    public void removeListOption(String path, String value) {
        List<String> list = config.getStringList(path);
        if (list.remove(value)) {
            config.set(path, list);
            saveWithQuotedLists();
        }
    }

    public boolean isInList(String path, String value) {
        return config.getStringList(path).contains(value);
    }

    public void addSpawnPoint(int x, int y, int z) {
        int index = 1;
        while (config.contains("spawnPoints.spawn" + index)) {
            index++;
        }

        String path = "spawnPoints.spawn" + index;
        config.set(path + ".x", x);
        config.set(path + ".y", y);
        config.set(path + ".z", z);

        save();
    }

    public void removeSpawnPoint(String name) {
        String path = "spawnPoints." + name;
        if (!config.contains(path)) {
            return;
        }
        config.set(path, null);
        save();
    }

    public boolean isSpawnPoint(String name) {
        String path = "spawnPoints." + name;
        return config.contains(path);
    }

    public boolean isTeam(String teamId) {
        String path = "teamSpawnPoints." + teamId;
        return config.contains(path);
    }

    public boolean isTeamSpawnPoint(String teamId, String name) {
        String path = "teamSpawnPoints." + teamId + "." + name;
        return config.contains(path);
    }

    public void addTeamSpawnPoint(String teamId, int x, int y, int z) {
        String basePath = "teamSpawnPoints." + teamId;

        int index = 1;
        while (config.contains(basePath + ".spawn" + index)) {
            index++;
        }

        String path = basePath + ".spawn" + index;
        config.set(path + ".x", x);
        config.set(path + ".y", y);
        config.set(path + ".z", z);

        save();
    }

    public void removeTeamSpawnPoint(String teamId, String spawnpointName) {
        String path = "teamSpawnPoints." + teamId + "." + spawnpointName;
        if (!config.contains(path)) {
            return;
        }
        config.set(path, null);
        save();
    }

    public List<String> getSpawnPointNames() {
        ConfigurationSection section = config.getConfigurationSection("spawnPoints");
        if (section == null) return List.of();
        return new ArrayList<>(section.getKeys(false));
    }

    public List<String> getTeamSpawnPointNames(String teamId) {
        ConfigurationSection section = config.getConfigurationSection("teamSpawnPoints." + teamId);
        if (section == null) return List.of();
        return new ArrayList<>(section.getKeys(false));
    }

    private void saveWithQuotedLists() {
        try {
            String yaml = config.saveToString();
            yaml = yaml.replaceAll("(?m)^(\\s*- )(?!\")([^\\n]+)$", "$1\"$2\"");
            Files.writeString(file.toPath(), yaml);
        } catch (IOException e) {
            e.printStackTrace();
        }
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