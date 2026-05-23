package wueffi.miniGameCreatORE.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.managers.ConfigManager;
import wueffi.miniGameCreatORE.managers.WorldManager;
import wueffi.miniGameCreatORE.utils.GameConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static wueffi.miniGameCreatORE.MiniGameCreatORE.sendMGCError;

public final class GameCommand implements CommandExecutor {
    private final MiniGameCreatORE plugin;
    private final ConfigManager configManager;
    private static final HashMap<String, String> commandsPermissions = new HashMap<>();
    private static List<String> validSettings = new ArrayList<>();

    public GameCommand(MiniGameCreatORE plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public static @NotNull HashMap<String, String> getCommandsPermissions() {
        if (commandsPermissions.isEmpty()) {
            commandsPermissions.put("create", "mgcreator.create");
            commandsPermissions.put("resume", "mgcreator.create");
            commandsPermissions.put("editconfig", "mgcreator.create");
            commandsPermissions.put("delete", "mgcreator.create");
        }
        return commandsPermissions;
    }
    public static @NotNull List<String> getValidSettings() {
        if (validSettings.isEmpty()) {
            validSettings.add("maxPlayers");
            validSettings.add("teams");
            validSettings.add("minPlayers");
            validSettings.add("inventory");
            validSettings.add("allowed_break_blocks");
            validSettings.add("allowed_place_blocks");
            validSettings.add("respawnMode");
            validSettings.add("respawnDelay");
            validSettings.add("doDurability");
            validSettings.add("allowPVP");
            validSettings.add("blocked_damage_causes");
            validSettings.add("timeLimit");
            validSettings.add("allowFriendlyFire");
            validSettings.add("allowCrafting");
            validSettings.add("silenceDeathMessages");
            validSettings.add("doHunger");
            validSettings.add("allowOpeningContainers");
        }
        return validSettings;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Yo console User, only players can use this command!");
            return true;
        }
        HashMap<String, String> commandsPermissions = getCommandsPermissions();

        if (args.length < 1) {
            StringBuilder availableCommands = new StringBuilder("§fUsage: §6/mgcreator <");

            for (String command: commandsPermissions.keySet()) {
                if (player.hasPermission(commandsPermissions.get(command))) {
                    availableCommands.append(command).append(" | ");
                }
            }
            if (!availableCommands.isEmpty()) {
                availableCommands.setLength(availableCommands.length() - 3);
            }
            availableCommands.append(">");
            player.sendMessage(availableCommands.toString());
            return true;
        }

        String subcmd = args[0].toLowerCase();

        if (!commandsPermissions.containsKey(subcmd)) {
            sendMGCError(player, "Unknown subcommand!");
            return true;
        }

        if (!(player.hasPermission(commandsPermissions.get(subcmd)) || player.hasPermission("mgcore.admin"))) {
            sendMGCError(player, "You don't have permission to use that subcommand!!");
            return true;
        }

        String target;
        File folder;

        switch (subcmd) {
            case "create":
                if (args.length != 3) {
                    sendMGCError(player, "Your Arguments are wrong!");
                    return true;
                }
                if (plugin.loadGameDrafts().containsKey(player.getUniqueId())) {
                    sendMGCError(player, "You already have a draft! Use \"/mgcreator resume\" to resume editing!");
                    return true;
                }

                if (!WorldManager.createWorld(player, args[1], Integer.parseInt(args[2]))) {
                    sendMGCError(player, "Could not start World-Creation!");
                    return true;
                }
                break;

            case "resume":
                if (!plugin.loadGameDrafts().containsKey(player.getUniqueId())) {
                    sendMGCError(player, "You don't have a draft yet! Use \"/mgcreator create\" to create one!");
                    return true;
                }

                target = plugin.loadGameDrafts().get(player.getUniqueId());
                folder = new File(plugin.getDataFolder(), "gameWorlds/" + target);
                if (!WorldManager.editWorld(player, folder)) {
                    sendMGCError(player, "Could not resume editing draft!");
                    return true;
                }
                break;

            case "editconfig":
                if (!plugin.loadGameDrafts().containsKey(player.getUniqueId())) {
                    sendMGCError(player, "You don't have a draft yet! Use \"/mgcreator create\" to create one!");
                    break;
                }

                if (args.length < 3) {
                    sendMGCError(player, "Usage: /mgcreator editconfig <setting> <value>");
                    break;
                }

                target = plugin.loadGameDrafts().get(player.getUniqueId());
                folder = new File(plugin.getDataFolder(), "gameWorlds/" + target);

                GameConfig config = configManager.getConfigFromFolder(folder);

                String setting = args[1];
                String value = args[2];

                if (!getValidSettings().contains(setting)) {
                    sendMGCError(player, "Not a valid setting!");
                    return true;
                }

                config.set(setting, value);
                config.save();

                player.sendMessage("Set " + setting + " to " + value);
                break;

            case "delete":
                if (!plugin.loadGameDrafts().containsKey(player.getUniqueId())) {
                    sendMGCError(player, "You don't have a draft yet! Use \"/mgcreator create\" to create one!");
                    return true;
                }

                target = plugin.loadGameDrafts().get(player.getUniqueId());
                folder = new File(plugin.getDataFolder(), "gameWorlds/" + target);
                if (!WorldManager.deleteWorld(folder)) {
                    sendMGCError(player, "Could not delete draft!");
                    return true;
                }
                plugin.removeGameDraft(player.getUniqueId());
                break;
        }

        return true;
    }
}