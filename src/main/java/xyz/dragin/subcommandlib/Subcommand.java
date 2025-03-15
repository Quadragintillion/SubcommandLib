package xyz.dragin.subcommandlib;

import io.vavr.control.Either;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.options.CommandFlag;
import xyz.dragin.subcommandlib.util.SubcommandUtils;

import java.util.*;

/**
 * A command or subcommand. Can have any amount of Subcommand children.
 */
public interface Subcommand {
    /**
     * The all-lowercase name of the command for registry and identification; what's typed by the player.
     * @return The name of the command
     */
    @NotNull String getName();

    /**
     * A list of Subcommands that are children to this one.
     * Default behavior: Returns an empty List.
     * @return All child Subcommands
     */
    @NotNull default List<Subcommand> getSubcommands() {
        return List.of();
    }

    /**
     * What should be done when the command is executed.
     * Default behavior: Returns false, telling the player it can't be run without a subcommand.
     * @param sender The CommandSender running the command
     * @param arguments All String (required) or CommandFlag (optional) arguments passed to the command
     * @return False if and only if the command cannot be run by itself (parent to subcommands only)
     */
    default boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments) {
        return false;
    }

    /**
     * A tab completion for anything BUT the option given to a flag (those are handled within the flags themselves).
     * Adds onto any flags provided by getAllowedFlags().
     * Note: You won't need to filter based on what's typed; that's handled automatically.
     * Default behavior: No tab completion options other than flags.
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @param typed The final incomplete argument to tab complete
     * @return A list of tab suggestions based on previous arguments
     */
    @NotNull default List<String> tabComplete(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments, String typed) {
        return List.of();
    }

    /**
     * CommandFlags that can be used and will be treated as flags (all other strings are literal).
     * Default behavior: Returns an empty List.
     * @param sender The CommandSender typing the command
     * @return List of valid CommandFlags
     */
    @NotNull default List<CommandFlag> getAllowedFlags(@NotNull CommandSender sender) {
        return List.of();
    }
    /**
     * A list of flags that are suggested in the tab completion based on the current entry.
     * Default behavior: suggests any flags from getAllowedFlags() that haven't been used.
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @return List of flags to suggest in the tab completion
     */
    @NotNull default List<CommandFlag> suggestFlags(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments) {
        ArrayList<CommandFlag> output = new ArrayList<>(getAllowedFlags(sender));
        output.removeAll(SubcommandUtils.getFlags(arguments));
        return output;
    }
}
