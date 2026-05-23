package wueffi.miniGameCreatORE.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.commands.GameCommand;

import java.util.*;

import static wueffi.miniGameCreatORE.commands.GameCommand.*;

public final class GameCommandTabCompleter implements TabCompleter {

    private static final Map<String, String> commandsPermissions = GameCommand.getCommandsPermissions();

    public GameCommandTabCompleter(MiniGameCreatORE plugin) {}

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return commandsPermissions.entrySet().stream()
                    .filter(e -> player.hasPermission(e.getValue()))
                    .map(Map.Entry::getKey)
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        String subcmd = args[0].toLowerCase();

        if (!commandsPermissions.containsKey(subcmd) ||
                !player.hasPermission(commandsPermissions.get(subcmd))) {
            return List.of();
        }

        if (args.length == 2) {
            switch (subcmd) {
                case "create":
                    return List.of("<gameName>");
                case "editconfig":
                    return GameCommand.getValidSettings();
            }
        }

        if (args.length == 3) {
            if (subcmd.equals("create")) {
                return List.of("<maxPlayers>");
            }

            if (subcmd.equals("editconfig")) {
                if (getBooleanSettings().contains(args[1])) return List.of("true", "false");
                if (getIntegerSettings().contains(args[1])) return List.of("<int>");
                if (getBlockSettings().contains(args[1])) return allBlocks;
                if (getItemSettings().contains(args[1])) return allItems;
                if (Objects.equals(args[1], "blocked_damage_causes")) return allDamageCauses;
                return List.of("<Config Option not known!>");
            }
        }

        return completions;
    }
}