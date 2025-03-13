package xyz.dragin.subcommandlib.api;

import io.vavr.control.Either;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.api.options.CommandFlag;
import xyz.dragin.subcommandlib.api.options.CommandOption;
import xyz.dragin.subcommandlib.util.SubcommandUtils;
import xyz.dragin.subcommandlib.util.TabUtils;

import java.util.*;

public interface Subcommand extends CommandExecutor, TabCompleter {
    /**
     * @return The name of the command for registry and identification
     */
    @NotNull String getName();

    /**
     * @return A list of Subcommands that are children to this one
     */
    @NotNull List<Subcommand> getSubcommands();

    /**
     * @param sender The CommandSender running the command
     * @param arguments All String (required) or CommandFlag (optional) arguments passed to the command
     */
    boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments);

    /**
     * A tab completion for anything BUT the option for a flag,
     * adds onto any flags provided by getAllowedFlags()
     * Note: You won't need to filter based on what's typed; that's handled automatically
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @return A list of tab suggestions based on previous arguments
     */
    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments, String typed);

    /**
     * @param sender The CommandSender typing the command
     * @return A list of CommandFlags that can be used and will be treated as flags (all other strings are literal)
     */
    @NotNull List<CommandFlag> getAllowedFlags(@NotNull CommandSender sender);

    /**
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @return A list of flags that are suggested based on the current entry - usually just a call to getAllowedFlags()
     */
    @NotNull List<CommandFlag> suggestFlags(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments);

    /**
     * Lower-level CommandExecutor function
     * Generally, do not override
     */
    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Pass to a nested subcommand if needed
        if (args.length > 0
                && SubcommandUtils.contains(this, args[0])) {
            return SubcommandUtils.findSubcommandByName(this, args[0])
                    .onCommand(sender, command, label, args);
        }
        return execute(sender, TabUtils.parseFlags(List.of(args), getAllowedFlags(sender)));
    }

    /**
     * Lower-level TabCompleter function
     * Generally, do not override
     */
    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> previouslySupplied = new ArrayList<>(List.of(args));
        if (!previouslySupplied.isEmpty()) previouslySupplied.removeLast();

        // Passes to a nested subcommand if needed
        if (!getSubcommands().isEmpty()
                && args.length > 0
                && SubcommandUtils.contains(this, args[0])) {
            return SubcommandUtils.findSubcommandByName(this, args[0])
                    .onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }

        // Initializes the output as all subcommand names
        List<String> output = new ArrayList<>(getSubcommands().stream().map(Subcommand::getName).toList());

        // Parsed version of the already given arguments
        List<Either<String, CommandFlag>> arguments = TabUtils.parseFlags(previouslySupplied, getAllowedFlags(sender));
        // Checks whether to base the tab completion off valid options for a CommandOption
        if (!arguments.isEmpty()
                && arguments.getLast().isRight()
                && arguments.getLast().get() instanceof CommandOption option
                && option.getOption() == null
        ) {
            output = option.getSuggestedOptions();
        } else {
            // Adds everything to the output
            List<String> actualTabCompletion = tabComplete(sender, arguments, args.length > 0 ? args[args.length-1] : "");
            List<CommandFlag> suggestedFlags = suggestFlags(sender, arguments);
            output.addAll(actualTabCompletion);
            output.addAll(suggestedFlags.stream().map(CommandFlag::toString).toList());
        }

        return TabUtils.narrow(output, args.length > 0 ? args[args.length-1] : "");
    }
}
