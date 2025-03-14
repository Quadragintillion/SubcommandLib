package xyz.dragin.subcommandlib;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class used for Subcommand registry
 */
public final class SubcommandLib extends JavaPlugin {
    private SubcommandLib() {}

    /**
     * Registers your Subcommand for use in game based on its name. The name must match one given in plugin.yml for the command to appear in game.
     * @param command The Subcommand to register
     * @param plugin The JavaPlugin to register on the behalf of, usually "this"
     */
    public static void register(@NotNull Subcommand command, @NotNull JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command.getName());
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
