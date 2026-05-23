package wueffi.miniGameCreatORE.managers;

import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.utils.GameConfig;

import java.io.File;
import java.util.HashMap;
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

        loadedConfigs.put(config.getString("game.name"), config);

    }

    public GameConfig getConfigFromFolder(File folder) {
        if (!loadedConfigs.containsKey(folder.getName())) loadConfig(folder);
        return loadedConfigs.get(folder.getName());
    }

    public Map<String, GameConfig> getLoadedConfigs() {
        return loadedConfigs;
    }
}