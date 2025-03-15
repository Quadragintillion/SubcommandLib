package xyz.dragin.subcommandlib;

import io.vavr.control.Either;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.options.CommandFlag;
import xyz.dragin.subcommandlib.options.CommandOption;
import xyz.dragin.subcommandlib.util.SubcommandUtils;
import xyz.dragin.subcommandlib.util.TabUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class used for Subcommand registry
 */
public final class SubcommandLib extends JavaPlugin {
    private SubcommandLib() {
    }

    /**
     * Registers your Subcommand for use in game based on its name. The name must match one given in plugin.yml for the command to appear in game.
     *
     * @param command The Subcommand to register
     * @param plugin  The JavaPlugin to register on the behalf of, usually "this"
     */
    public static void register(@NotNull Subcommand command, @NotNull JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command.getName());
        SubcommandWrapper wrapper = new SubcommandWrapper(command);

        pluginCommand.setExecutor(wrapper);
        pluginCommand.setTabCompleter(wrapper);
    }

    private record SubcommandWrapper(Subcommand subcommand) implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            // Pass to a nested subcommand if needed
            if (args.length > 0
                    && SubcommandUtils.contains(subcommand, args[0])) {
                return new SubcommandWrapper(SubcommandUtils.findSubcommandByName(subcommand, args[0]))
                        .onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            // False if it cannot be run by itself
            if (!subcommand.execute(sender, TabUtils.parseFlags(List.of(args), subcommand.getAllowedFlags(sender))))
                sender.sendMessage(ChatColor.RED + "This command cannot be run by itself.");
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            List<String> previouslySupplied = new ArrayList<>(List.of(args));
            if (!previouslySupplied.isEmpty()) previouslySupplied.removeLast();

            // Passes to a nested subcommand if needed
            if (!subcommand.getSubcommands().isEmpty()
                    && !previouslySupplied.isEmpty()
                    && SubcommandUtils.contains(subcommand, previouslySupplied.getFirst())) {
                return new SubcommandWrapper(SubcommandUtils.findSubcommandByName(subcommand, previouslySupplied.getFirst()))
                        .onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }

            // Initializes the output as all subcommand names
            List<String> output = new ArrayList<>(subcommand.getSubcommands().stream().map(Subcommand::getName).toList());

            // Parsed version of the already given arguments
            List<Either<String, CommandFlag>> arguments = TabUtils.parseFlags(previouslySupplied, subcommand.getAllowedFlags(sender));
            // Checks whether to base the tab completion off valid options for a CommandOption
            if (!arguments.isEmpty()
                    && arguments.getLast().isRight()
                    && arguments.getLast().get() instanceof CommandOption option
                    && option.getOption() == null
            ) {
                output = option.getSuggestedOptions();
            } else {
                // Adds everything to the output
                List<String> actualTabCompletion = subcommand.tabComplete(sender, arguments, !previouslySupplied.isEmpty() ? previouslySupplied.getLast() : "");
                List<CommandFlag> suggestedFlags = subcommand.suggestFlags(sender, arguments);
                List<String> additionalFlags = new ArrayList<>();
                if (args.length != 0) {
                    // Get the current flag sequence to build upon
                    List<CommandFlag> context = TabUtils.parseFlags(List.of(args[args.length - 1]), subcommand.getAllowedFlags(sender))
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
                                                subcommand.getAllowedFlags(sender)
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

            return TabUtils.narrow(output, args.length > 0 ? args[args.length - 1] : "");
        }
    }
}
