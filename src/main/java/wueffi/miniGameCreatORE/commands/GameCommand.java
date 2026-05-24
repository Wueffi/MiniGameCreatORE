package wueffi.miniGameCreatORE.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.managers.ConfigManager;
import wueffi.miniGameCreatORE.managers.WorldManager;
import wueffi.miniGameCreatORE.utils.GameConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static wueffi.miniGameCreatORE.MiniGameCreatORE.sendMGCError;
import static wueffi.miniGameCreatORE.MiniGameCreatORE.sendMGCInfo;

public final class GameCommand implements CommandExecutor {
    private final MiniGameCreatORE plugin;
    private final ConfigManager configManager;
    private static final HashMap<String, String> commandsPermissions = new HashMap<>();

    private static final List<String> validSettings = new ArrayList<>();
    private static final List<String> booleanSettings = new ArrayList<>();
    private static final List<String> integerSettings = new ArrayList<>();
    private static final List<String> blockSettings = new ArrayList<>();
    private static final List<String> itemSettings = new ArrayList<>();

    public static final List<String> allBlocks = Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .map(m -> m.name().toLowerCase())
            .toList();
    public static final List<String> allDamageCauses = Arrays.stream(EntityDamageEvent.DamageCause.values())
            .map(d -> d.name().toLowerCase())
            .toList();
    public static final List<String> allItems = Arrays.stream(Material.values())
            .filter(Material::isItem)
            .map(m -> m.name().toLowerCase())
            .toList();

    public GameCommand(MiniGameCreatORE plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public static @NotNull HashMap<String, String> getCommandsPermissions() {
        if (commandsPermissions.isEmpty()) {
            commandsPermissions.put("create", "mgcreator.create");
            commandsPermissions.put("resume", "mgcreator.create");
            commandsPermissions.put("editconfig", "mgcreator.create");
            commandsPermissions.put("showconfig", "mgcreator.create");
            commandsPermissions.put("delete", "mgcreator.create");
            commandsPermissions.put("publish", "mgcreator.publish");
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

    public static @NotNull List<String> getBooleanSettings() {
        if (booleanSettings.isEmpty()) {
            booleanSettings.add("respawnMode");
            booleanSettings.add("doDurability");
            booleanSettings.add("allowPVP");
            booleanSettings.add("allowFriendlyFire");
            booleanSettings.add("allowCrafting");
            booleanSettings.add("silenceDeathMessages");
            booleanSettings.add("doHunger");
            booleanSettings.add("allowOpeningContainers");
        }
        return booleanSettings;
    }

    public static @NotNull List<String> getIntegerSettings() {
        if (integerSettings.isEmpty()) {
            integerSettings.add("maxPlayers");
            integerSettings.add("teams");
            integerSettings.add("minPlayers");
            integerSettings.add("respawnDelay");
            integerSettings.add("timeLimit");
        }
        return integerSettings;
    }

    public static @NotNull List<String> getBlockSettings() {
        if (blockSettings.isEmpty()) {
            blockSettings.add("allowed_break_blocks");
            blockSettings.add("allowed_place_blocks");
        }
        return blockSettings;
    }

    public static @NotNull List<String> getItemSettings() {
        if (itemSettings.isEmpty()) {
            itemSettings.add("inventory");
        }
        return itemSettings;
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
                if (plugin.loadGameDrafts().containsValue(args[1] + "_world")) {
                    sendMGCError(player, "There is already a draft using this name!");
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

                if (getBooleanSettings().contains(setting)) {
                    if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
                        sendMGCError(player, "Not a valid value! Allowed: Boolean");
                        return true;
                    }
                    Boolean value2 = Boolean.parseBoolean(value);
                    config.set("game." + setting, value2);
                    config.save();
                    sendMGCInfo(player, "Set " + setting + " to " + value + "!");
                }
                else if (getIntegerSettings().contains(setting)) {
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        sendMGCError(player, "Not a valid value! Allowed: Integer");
                        return true;
                    }
                    config.set("game." + setting, Integer.parseInt(value));
                    config.save();
                    sendMGCInfo(player, "Set " + setting + " to " + value + "!");
                }
                else if (getBlockSettings().contains(setting)) {
                    if (!allBlocks.contains(value)) {
                        sendMGCError(player, "Not a valid value! Allowed: Block");
                        return true;
                    }
                    if (config.isInList("game." + setting, value)) {
                        config.removeListOption("game." + setting, value);
                        sendMGCError(player, "Removed " + value + " from " + setting + "!");
                    } else {
                        config.addListOption("game." + setting, value);
                        sendMGCInfo(player, "Added " + value + " to " + setting + "!");
                    }
                    return true;
                }
                else if (getItemSettings().contains(setting)) {
                    if (!allItems.contains(value)) {
                        sendMGCError(player, "Not a valid value! Allowed: Item");
                        return true;
                    }
                    if (config.isInList("game." + setting, value)) {
                        config.removeListOption("game." + setting, value);
                        sendMGCError(player, "Removed " + value + " from " + setting + "!");
                    } else {
                        config.addListOption("game." + setting, value);
                        sendMGCInfo(player, "Added " + value + " to " + setting + "!");
                    }
                    return true;
                }
                else {
                    if (!allDamageCauses.contains(value)) {
                        sendMGCError(player, "Not a valid value! Allowed: Damage Cause");
                        return true;
                    }
                    if (config.isInList("game." + setting, value)) {
                        config.removeListOption("game." + setting, value);
                        sendMGCError(player, "Removed " + value + " from " + setting + "!");
                    } else {
                        config.addListOption("game." + setting, value);
                        sendMGCInfo(player, "Added " + value + " to " + setting + "!");
                    }
                    return true;
                }
                break;

            case "showconfig":
                if (!plugin.loadGameDrafts().containsKey(player.getUniqueId())) {
                    sendMGCError(player, "You don't have a draft yet! Use \"/mgcreator create\" to create one!");
                    return true;
                }

                target = plugin.loadGameDrafts().get(player.getUniqueId());
                folder = new File(plugin.getDataFolder(), "gameWorlds/" + target);
                GameConfig showConfig = configManager.getConfigFromFolder(folder);

                player.sendMessage(Component.text("--- Game Config ---").color(NamedTextColor.GOLD));

                for (String s : getValidSettings()) {
                    Object val = showConfig.get("game." + s);
                    if (val == null) continue;

                    if (val instanceof List<?> list) {
                        player.sendMessage(Component.text(s + ":").color(NamedTextColor.YELLOW));
                        for (Object item : list) {
                            String itemStr = item.toString();
                            Component removeButton = Component.text(" [remove]")
                                    .color(NamedTextColor.RED)
                                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/mgcreator editconfig " + s + " " + itemStr))
                                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click to remove " + itemStr)));
                            player.sendMessage(Component.text("  - " + itemStr).color(NamedTextColor.WHITE).append(removeButton));
                        }
                    } else {
                        Component editButton = Component.text(" [edit]")
                                .color(NamedTextColor.AQUA)
                                .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/mgcreator editconfig " + s + " " + val))
                                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("Click to edit " + s)));
                        player.sendMessage(Component.text(s + ": ").color(NamedTextColor.YELLOW).append(Component.text(val.toString()).color(NamedTextColor.WHITE)).append(editButton));
                    }
                }
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
                sendMGCInfo(player, "Sucessfully deleted draft: " + target);
                break;

            case "publish":
                if (args.length < 2) {
                    sendMGCError(player, "Usage: /mgcreator publish <player>");
                    return true;
                }

                Player targetPlayer = plugin.getServer().getPlayerExact(args[1]);
                if (targetPlayer == null) {
                    sendMGCError(player, "Player " + args[1] + " is not online!");
                    return true;
                }

                if (!plugin.loadGameDrafts().containsKey(targetPlayer.getUniqueId())) {
                    sendMGCError(player, targetPlayer.getName() + " doesn't have a draft!");
                    return true;
                }

                target = plugin.loadGameDrafts().get(targetPlayer.getUniqueId());
                folder = new File(plugin.getDataFolder(), "gameWorlds/" + target);

                WorldManager worldManager = new WorldManager();
                if (!worldManager.saveAndCleanWorld(folder)) {
                    sendMGCError(player, "Could not publish " + targetPlayer.getName() + "'s draft!");
                    return true;
                }

                String game = target.replace("_world", "");

                configManager.addMGCGame(game);

                plugin.removeGameDraft(targetPlayer.getUniqueId());
                sendMGCInfo(player, "Published " + game + " successfully!");
                sendMGCInfo(targetPlayer, "Your draft " + game + " was published by " + player.getName() + "!");
                break;
        }

        return true;
    }
}