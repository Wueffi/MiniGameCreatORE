package wueffi.miniGameCreatORE;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import wueffi.miniGameCreatORE.commands.GameCommand;
import wueffi.miniGameCreatORE.managers.ConfigManager;
import wueffi.miniGameCreatORE.managers.WorldManager;
import wueffi.miniGameCreatORE.utils.GameCommandTabCompleter;
import wueffi.miniGameCreatORE.utils.PlayerEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MiniGameCreatORE extends JavaPlugin {
    public static final Component prefix = Component.text()
            .append(Component.text("[", NamedTextColor.GRAY))
            .append(Component.text("MiniGameCreatORE", NamedTextColor.GOLD))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .hoverEvent(HoverEvent.showText(
                    Component.text("Made with ❤ by Waffle", NamedTextColor.GOLD)
            ))
            .build();

    private static final String mgcVersion = "2.2.0";

    @Override
    public void onEnable() {
        Plugin target = Bukkit.getPluginManager().getPlugin("MiniGameCore");

        if (target == null || !target.isEnabled()) {
            getLogger().severe("MiniGameCore is missing or not enabled! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        String installedVersion = target.getPluginMeta().getVersion();
        if (!isVersionAtLeast(installedVersion, mgcVersion)) {
            getLogger().severe("MiniGameCore version " + installedVersion + " is outdated! Requires at least " + mgcVersion + ". Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(this);
        WorldManager.setConfigManager(configManager);
        WorldManager.setPlugin(this);

        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        GameCommand gameCommand = new GameCommand(this, configManager);
        GameCommandTabCompleter gameCommandTabCompleter = new GameCommandTabCompleter(this, configManager);

        getCommand("mgcreator").setExecutor(gameCommand);
        getCommand("mgcreator").setTabCompleter(gameCommandTabCompleter);

        getLogger().info("Commands registered!");
    }

    @Override
    public void onDisable() {
        for (Map.Entry<UUID, String> entry : loadGameDrafts().entrySet()) {
            String worldName = entry.getValue();
            File rootFolder = new File(Bukkit.getWorldContainer(), worldName);

            if (rootFolder.exists()) {
                WorldManager.moveToArchive(rootFolder);
            }
        }
    }
    public static void sendMGCInfo(Player player, String message) {
        player.sendMessage(Component.text()
                .append(prefix)
                .append(Component.text(message, NamedTextColor.DARK_GREEN))
                .build());
    }

    public static void sendMGCError(Player player, String message) {
        player.sendMessage(Component.text()
                .append(prefix)
                .append(Component.text(message, NamedTextColor.RED))
                .build());
    }

    public HashMap<UUID, String> loadGameDrafts() {
        HashMap<UUID, String> map = new HashMap<>();

        ConfigurationSection section = getConfig().getConfigurationSection("gameDrafts");
        if (section == null) return map;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String value = section.getString(key);

                map.put(uuid, value);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return map;
    }

    public void addGameDraft(UUID uuid, String value) {
        getConfig().set("gameDrafts." + uuid.toString(), value);
        saveConfig();
    }

    public void removeGameDraft(UUID uuid) {
        getConfig().set("gameDrafts." + uuid.toString(), null);
        saveConfig();
    }

    private boolean isVersionAtLeast(String current, String minimum) {
        String[] currParts = current.split("[-+]")[0].split("\\.");
        String[] minParts = minimum.split("[-+]")[0].split("\\.");

        int length = Math.max(currParts.length, minParts.length);
        for (int i = 0; i < length; i++) {
            int currNum = i < currParts.length ? parsePart(currParts[i]) : 0;
            int minNum = i < minParts.length ? parsePart(minParts[i]) : 0;

            if (currNum > minNum) return true;
            if (currNum < minNum) return false;
        }
        return true;
    }

    private int parsePart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
