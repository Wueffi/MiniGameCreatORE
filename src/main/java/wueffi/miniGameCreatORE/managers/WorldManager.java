package wueffi.miniGameCreatORE.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.utils.GameConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldManager {

    private static final String[] PLAYER_DATA_FOLDERS = { "playerdata", "stats", "advancements" };

    private static MiniGameCreatORE plugin;
    private static ConfigManager configManager;

    public static boolean createWorld(Player player, String gameName, int maxPlayers) {
        String worldName = gameName + "_world";

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        File storedFolder = new File(new File(plugin.getDataFolder(), "gameWorlds"), worldName);

        if (Bukkit.getWorld(worldName) != null || worldFolder.exists() || storedFolder.exists()) {
            return false;
        }

        worldFolder.mkdirs();

        try (var in = plugin.getResource("default.zip")) {
            if (in == null) {
                plugin.getLogger().severe("default.zip not found!");
                return false;
            }
            unzip(in, worldFolder);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        World world = new WorldCreator(worldName)
                .environment(World.Environment.NORMAL)
                .createWorld();

        if (world == null) {
            return false;
        }

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.FALL_DAMAGE, false);
        world.setTime(6000);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setSpawnLocation(0, 64, 0);

        configManager.createConfig(worldFolder, gameName, maxPlayers);
        teleportToEdit(player, world);
        plugin.addGameDraft(player.getUniqueId(), worldFolder.getName());
        return true;
    }

    private static void unzip(InputStream in, File targetDir) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                File dest = new File(targetDir, entry.getName());

                if (!dest.toPath().normalize().startsWith(targetDir.toPath().normalize())) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    dest.getParentFile().mkdirs();
                    Files.copy(zip, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }

    public static void setConfigManager(ConfigManager configManager) {
        WorldManager.configManager = configManager;
    }

    public static void setPlugin(MiniGameCreatORE plugin) {
        WorldManager.plugin = plugin;
    }

    public boolean saveAndCleanWorld(File folder) {
        World world = Bukkit.getWorld(folder.getName());
        if (world == null) return false;

        World defaultWorld = Bukkit.getWorld("world");
        if (defaultWorld == null) return false;

        world.save();
        for (Player player : world.getPlayers()) {
            player.teleport(defaultWorld.getSpawnLocation());
        }
        Bukkit.unloadWorld(world, false);

        stripPlayerData(folder);

        File destination = new File(plugin.getDataFolder().getParentFile(), "MiniGameCore/MiniGames/" + folder.getName());
        destination.mkdirs();

        try {
            Files.move(folder.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to move world to gameWorlds: " + e.getMessage());
        }

        return true;
    }

    public static boolean editWorld(Player player, File folder) {
        GameConfig config = configManager.getConfigFromFolder(folder);
        if (config == null) {
            plugin.getLogger().warning("config null");
            return false;
        }

        String worldName = folder.getName();

        File rootFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!rootFolder.exists() && folder.exists()) {
            try {
                Files.move(folder.toPath(), rootFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to move world to root: " + e.getMessage());
                return false;
            }
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            world = new WorldCreator(worldName)
                    .environment(World.Environment.NORMAL)
                    .createWorld();
        }

        if (world == null) return false;

        teleportToEdit(player, world);
        return true;
    }

    public static void moveWorld(File folder) {
        File gameWorldsFolder = new File(plugin.getDataFolder(), "gameWorlds");
        gameWorldsFolder.mkdirs();
        File destination = new File(gameWorldsFolder, folder.getName());

        try {
            Files.move(folder.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to move world to gameWorlds: " + e.getMessage());
        }
    }

    public static boolean deleteWorld(File folder) {
        String worldName = folder.getName();
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            World defaultWorld = Bukkit.getWorld("world");
            if (defaultWorld == null) return false;

            for (Player player : world.getPlayers()) {
                player.teleport(defaultWorld.getSpawnLocation());
            }
            Bukkit.unloadWorld(world, false);
        }

        File rootFolder = new File(Bukkit.getWorldContainer(), worldName);
        File gameWorldsFolder = new File(plugin.getDataFolder(), "gameWorlds");
        File storedFolder = new File(gameWorldsFolder, worldName);

        if (rootFolder.exists()) {
            deleteFolderRecursive(rootFolder);
            return true;
        } else if (storedFolder.exists()) {
            deleteFolderRecursive(storedFolder);
            return true;
        }

        return false;
    }

    private static void teleportToEdit(Player player, World world) {
        Location spawn = new Location(world, 0, 64, 0);
        player.teleport(spawn);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void stripPlayerData(File worldFolder) {
        for (String folderName : PLAYER_DATA_FOLDERS) {
            File folder = new File(worldFolder, folderName);
            if (folder.exists() && folder.isDirectory()) {
                deleteFolderRecursive(folder);
            }
        }

        File sessionLock = new File(worldFolder, "session.lock");
        if (sessionLock.exists()) {
            sessionLock.delete();
        }

        File uidFile = new File(worldFolder, "uid.dat");
        if (uidFile.exists()) {
            uidFile.delete();
        }
    }

    private static void deleteFolderRecursive(File folder) {
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete: " + folder.getAbsolutePath());
        }
    }
}