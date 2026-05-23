package wueffi.miniGameCreatORE;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import wueffi.miniGameCreatORE.commands.GameCommand;
import wueffi.miniGameCreatORE.managers.ConfigManager;
import wueffi.miniGameCreatORE.managers.WorldManager;
import wueffi.miniGameCreatORE.utils.GameCommandTabCompleter;
import wueffi.miniGameCreatORE.utils.PlayerLeaveListener;

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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(this);
        WorldManager.setConfigManager(configManager);
        WorldManager.setPlugin(this);

        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);

        GameCommand gameCommand = new GameCommand(this, configManager);
        GameCommandTabCompleter gameCommandTabCompleter = new GameCommandTabCompleter(this);

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
                WorldManager.moveWorld(rootFolder);
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
}
