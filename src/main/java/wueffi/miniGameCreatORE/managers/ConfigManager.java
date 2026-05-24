package wueffi.miniGameCreatORE.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.utils.GameConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final MiniGameCreatORE plugin;
    private final Map<String, GameConfig> loadedConfigs = new HashMap<>();

    public ConfigManager(MiniGameCreatORE plugin) {
        this.plugin = plugin;
    }

    public GameConfig createConfig(File folder, String gameName, int minPlayers) {
        GameConfig config = new GameConfig(plugin, folder, gameName, minPlayers);

        loadedConfigs.put(folder.getName(), config);

        return config;
    }

    public void loadConfig(File folder) {
        File file = new File(folder, "config.yml");

        if (!file.exists()) {
            return;
        }

        GameConfig config = new GameConfig(plugin, folder);

        loadedConfigs.put(folder.getName(), config);
    }

    public GameConfig getConfigFromFolder(File folder) {
        if (!loadedConfigs.containsKey(folder.getName())) loadConfig(folder);
        return loadedConfigs.get(folder.getName());
    }

    public void addMGCGame(String name) {
        File mgcConfig = new File(plugin.getDataFolder().getParentFile(), "MiniGameCore/config.yml");
        if (!mgcConfig.exists()) {
            plugin.getLogger().severe("MiniGameCore/config.yml not found!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(mgcConfig);
        List<String> games = config.getStringList("available-games");
        if (!games.contains(name)) {
            games.add(name);
            config.set("available-games", games);
            try {
                config.save(mgcConfig);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save MiniGameCore config: " + e.getMessage());
            }
        }
    }
}