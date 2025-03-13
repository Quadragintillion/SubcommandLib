package xyz.dragin.subcommandlib.api;

import io.vavr.control.Either;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.api.options.CommandFlag;

import java.util.List;

/**
 * A type of Subcommand that cannot be run by itself for less typing and space
 */
public interface ParentOnlySubcommand extends NoParameterSubcommand {
    @Override
    default boolean execute(@NotNull CommandSender commandSender, @NotNull List<Either<String, CommandFlag>> list) {
        commandSender.sendMessage(ChatColor.RED + "This command cannot be run by itself.");
        return true;
    }
}
