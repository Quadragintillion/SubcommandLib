package xyz.dragin.subcommandlib;

import io.vavr.control.Either;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.options.CommandFlag;
import xyz.dragin.subcommandlib.options.CommandOption;
import xyz.dragin.subcommandlib.util.SubcommandUtils;
import xyz.dragin.subcommandlib.util.TabUtils;

import java.util.*;

/**
 * A command or subcommand. Can have any amount of Subcommand children.
 */
public abstract class Subcommand implements CommandExecutor, TabCompleter {
    private final String name;
    /**
     * The all-lowercase name of the command for registry and identification; what's typed by the player
     * @return The name of the command
     */
    @NotNull public String getName() { return name; }

    /**
     * Initialization constructor
     * @param name The all-lowercase name of the Subcommand for registry and identification; what's typed by the player
     */
    public Subcommand(String name) {
        this.name = name;
    }

    /**
     * A list of Subcommands that are children to this one
     * @return All child Subcommands
     */
    @NotNull public abstract List<Subcommand> getSubcommands();

    /**
     * What should be done when the command is executed
     * @param sender The CommandSender running the command
     * @param arguments All String (required) or CommandFlag (optional) arguments passed to the command
     * @return False if and only if the command cannot be run by itself (parent to subcommands only)
     */
    public abstract boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments);

    /**
     * A tab completion for anything BUT the option for a flag,
     * adds onto any flags provided by getAllowedFlags()
     * Note: You won't need to filter based on what's typed; that's handled automatically
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @param typed The final incomplete argument to tab complete
     * @return A list of tab suggestions based on previous arguments
     */
    @NotNull public abstract List<String> tabComplete(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments, String typed);

    /**
     * CommandFlags that can be used and will be treated as flags (all other strings are literal)
     * @param sender The CommandSender typing the command
     * @return List of valid CommandFlags
     */
    @NotNull public abstract List<CommandFlag> getAllowedFlags(@NotNull CommandSender sender);
    /**
     * A list of flags that are suggested in the tab completion based on the current entry - usually just a call to getAllowedFlags()
     * @param sender The CommandSender typing the command
     * @param arguments All String (required) and CommandFlag (optional) arguments currently entered into the command
     * @return List of flags to suggest in the tab completion
     */
    @NotNull public abstract List<CommandFlag> suggestFlags(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments);

    /**
     * Lower-level CommandExecutor function. Generally, you should not override this.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Pass to a nested subcommand if needed
        if (args.length > 0
                && SubcommandUtils.contains(this, args[0])) {
            return SubcommandUtils.findSubcommandByName(this, args[0])
                    .onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }
        // False if it cannot be run by itself
        if (!execute(sender, TabUtils.parseFlags(List.of(args), getAllowedFlags(sender))))
            sender.sendMessage(ChatColor.RED + "This command cannot be run by itself.");
        return true;
    }

    /**
     * Lower-level TabCompleter function. Generally, you should not override this.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> previouslySupplied = new ArrayList<>(List.of(args));
        if (!previouslySupplied.isEmpty()) previouslySupplied.removeLast();

        // Passes to a nested subcommand if needed
        if (!getSubcommands().isEmpty()
                && !previouslySupplied.isEmpty()
                && SubcommandUtils.contains(this, previouslySupplied.getFirst())) {
            return SubcommandUtils.findSubcommandByName(this, previouslySupplied.getFirst())
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
            List<String> actualTabCompletion = tabComplete(sender, arguments, !previouslySupplied.isEmpty() ? previouslySupplied.getLast() : "");
            List<CommandFlag> suggestedFlags = suggestFlags(sender, arguments);
            List<String> additionalFlags = new ArrayList<>();
            if (args.length != 0) {
                // Get the current flag sequence to build upon
                List<CommandFlag> context = TabUtils.parseFlags(List.of(args[args.length - 1]), getAllowedFlags(sender))
                        .stream()
                        .filter(Either::isRight)
                        .map(Either::get)
                        .toList();
                if (!context.isEmpty() && context.getLast().getFlag().length() == 1) {
                    // Add any suggested single-digit flags
                    additionalFlags.addAll(
                            context.getLast().getSuggestedNext(context)
                                    .stream()
                                    .filter(character ->
                                            getAllowedFlags(sender)
                                                    .stream()
                                                    .filter(flag -> flag.getFlag().length() == 1)
                                                    .map(flag -> flag.getFlag().charAt(0))
                                                    .toList()
                                                    .contains(character)
                                    )
                                    .map(character -> "-" + String.join("", context
                                            .stream()
                                            .map(CommandFlag::getFlag)
                                            .toList()
                                    ) + character)
                                    .toList()
                    );
                    // I'm gonna look at this code in a year and have no clue what I was thinking with all this stream shit
                }
            }
            output.addAll(actualTabCompletion);
            output.addAll(suggestedFlags.stream().map(CommandFlag::toString).toList());
            output.addAll(additionalFlags);
        }

        return TabUtils.narrow(output, args.length > 0 ? args[args.length-1] : "");
    }
}
