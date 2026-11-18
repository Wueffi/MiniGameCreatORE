package wueffi.miniGameCreatORE.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;
import wueffi.miniGameCreatORE.MiniGameCreatORE;
import wueffi.miniGameCreatORE.managers.WorldManager;
import wueffi.MiniGameCore.api.GameSpectateEvent;

import java.io.File;
import java.util.UUID;

import static wueffi.miniGameCreatORE.managers.WorldManager.isInWorld;

public class PlayerEventListener implements Listener {

    private final MiniGameCreatORE plugin;

    public PlayerEventListener(MiniGameCreatORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String worldName = plugin.loadGameDrafts().get(uuid);
        if (worldName == null) return;

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        World defaultWorld = Bukkit.getWorld("world");
        if (defaultWorld == null) return;

        for (Player player1 : world.getPlayers()) {
            player1.teleport(defaultWorld.getSpawnLocation());
        }

        Bukkit.unloadWorld(world, true);
        WorldManager.moveToArchive(worldFolder);
    }

    @EventHandler
    public void onPlayerSpectate(GameSpectateEvent event) {
        Player target = Bukkit.getPlayer(event.getTargetUUID());

        if (target != null && isInWorld(target)) {
            event.setCancelled(true);
        }
    }
}