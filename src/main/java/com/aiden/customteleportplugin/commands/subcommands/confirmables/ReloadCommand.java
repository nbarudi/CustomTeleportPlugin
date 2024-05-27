package com.aiden.customteleportplugin.commands.subcommands.confirmables;

import com.aiden.customteleportplugin.commands.CommandManager;
import com.aiden.customteleportplugin.commands.subcommands.confirmables.exclusive.TpAllCommand;
import com.aiden.customteleportplugin.listeners.HandlePlayerCommand;
import com.aiden.customteleportplugin.listeners.HandlePlayerDisconnect;
import com.aiden.customteleportplugin.listeners.HandlePlayerJoin;
import com.aiden.customteleportplugin.listeners.HandleToolUse;
import com.aiden.customteleportplugin.managers.BlockLocationManager;
import com.aiden.customteleportplugin.managers.TeleportFreezeManager;
import com.aiden.customteleportplugin.util.CommandState;
import com.aiden.customteleportplugin.managers.FileManager;
import com.aiden.customteleportplugin.messengers.PlayerChatMessenger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class ReloadCommand extends ConfirmableSubcommand {

    private static final FileManager fileManager = new FileManager();

    private static final BlockLocationManager locationManager = new BlockLocationManager();

    private static final PlayerChatMessenger chatMessenger = new PlayerChatMessenger();

    private static JavaPlugin plugin;

    public ReloadCommand(JavaPlugin pl) { plugin = pl; }

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getDescription() { return "Save all block locations and reload the plugin"; }

    @Override
    public String getSyntax() { return "/ctp reload"; }

    @Override
    public void perform(Player sender, String[] args) {
        CommandState tpAllCommandState = new TpAllCommand().getCommandState();
        if(tpAllCommandState.equals(CommandState.CURRENTLY_EXECUTED)) {
            chatMessenger.sendChat(sender, "&cCannot reload until &e/ctp return &cis executed!");
            return;
        }
        Set<Location> allBlockLocations = locationManager.getAllBlockLocations();
        if(!fileManager.saveSetToFile(allBlockLocations)) {
            chatMessenger.sendChat(sender, "&cAn unknown error occurred when trying to save the set\n" +
                    "block locations. Please check the console for details.");
        }
        plugin.reloadConfig();
        unregisterListeners();
        unregisterCommands();
        registerListeners();
        registerCommands();
        FileManager fm = new FileManager(plugin);
        TeleportFreezeManager tfm = new TeleportFreezeManager(plugin);
    }

    @Override
    public String getConfirmationMessage() {
        return "WARNING: You can only reload the plugin if no players are currently teleported to their blocks!";
    }

    private void unregisterListeners() { HandlerList.unregisterAll(plugin); }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new HandlePlayerCommand(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HandlePlayerDisconnect(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HandlePlayerJoin(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HandleToolUse(), plugin);
    }

    private void unregisterCommands() { plugin.getCommand("ctp").setExecutor(null); }

    private void registerCommands() { plugin.getCommand("ctp").setExecutor(new CommandManager(plugin)); }
}
