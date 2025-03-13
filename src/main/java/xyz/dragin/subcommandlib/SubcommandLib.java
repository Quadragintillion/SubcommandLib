package xyz.dragin.subcommandlib;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.api.Subcommand;

public final class SubcommandLib extends JavaPlugin{
    public static void register(@NotNull Subcommand command, @NotNull JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command.getName());
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
