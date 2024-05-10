package com.pepperish.customteleportplugin.managers;

import com.pepperish.customteleportplugin.enums.Permission;
import com.pepperish.customteleportplugin.messengers.PlayerChatMessenger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TeleportManager {

    private static PlayerChatMessenger chatMessenger = new PlayerChatMessenger();

    private static LocationManager locationManager = new LocationManager();

    private static String shouldBeTeleportedPermission = Permission.SHOULD_BE_TELEPORTED.getString();

    private static boolean playersAreTeleported = false;

    private static JavaPlugin plugin;
    public TeleportManager(JavaPlugin pl) { plugin = pl; }

    public TeleportManager() {}



    public void tpPlayer(Player player, Location location) {
        player.teleportAsync(location.add(0, 1, 0)); // Add 1 to y so player doesn't spawn in block
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.setJumping(false);
    }

    public void tpAllPlayers() {
        boolean playersShouldBeMessagedOnTeleport = plugin.getConfig().getBoolean("message-players-on-tp");
        List<String> onTpMessages = plugin.getConfig().getStringList("on-tp-messages");
        AtomicInteger teleportedPlayerCount = new AtomicInteger(0);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(shouldBeTeleportedPermission))
                .forEach(p -> {
                    Optional<Location> nextAvailableLocation = locationManager.getNextAvailableLocation(p);
                    if(nextAvailableLocation.isPresent()) {
                        tpPlayer(p, nextAvailableLocation.get());
                        if (playersShouldBeMessagedOnTeleport) {
                            chatMessenger.sendChat(p, onTpMessages);
                        }
                        teleportedPlayerCount.incrementAndGet();
                    }
                    else {
                        List<String> messages = List.of(
                                "&e[CustomTeleport] &6(sent to all CTP admins),",
                                "&cWARNING: CTP could not teleport all players because there weren't enough set block locations!"
                        );
                        chatMessenger.messageAdmins(messages);
                        return;
                    }
                });


        chatMessenger.messageAdmins(String.format("&e[CustomTeleport] &6(sent to all CTP admins)\n" +
                "&aSuccessfully teleported &e%s &aplayers", teleportedPlayerCount.get()));

        playersAreTeleported = true;
    }
    public void returnPlayer(Player player, Location location) {
        player.teleportAsync(location);
        player.setWalkSpeed(1);
        player.setFlySpeed(1);
        player.setJumping(true);
    }

    public void returnAllPlayers(Player commandSender) {
        boolean playersShouldBeMessagedOnReturn = plugin.getConfig().getBoolean("message-players-on-return");
        List<String> onReturnMessages = plugin.getConfig().getStringList("on-return-messages");
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(shouldBeTeleportedPermission))
                .forEach(p -> {
                    Location returnLocation = locationManager.getReturnLocation(p);
                    returnPlayer(p, returnLocation);
                    if(playersShouldBeMessagedOnReturn) {
                        chatMessenger.sendChat(p, onReturnMessages);
                    }
                });
        locationManager.refresh();
        chatMessenger.sendChat(sender, "&aSuccessfully returned all players!");

        playersAreTeleported = false;
    }
}