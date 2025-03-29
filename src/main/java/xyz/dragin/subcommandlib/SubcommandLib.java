package xyz.dragin.subcommandlib;

import io.vavr.control.Either;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.options.CommandFlag;
import xyz.dragin.subcommandlib.options.CommandOption;
import xyz.dragin.subcommandlib.util.SubcommandUtils;
import xyz.dragin.subcommandlib.util.TabUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class used for Subcommand registry
 */
public final class SubcommandLib extends JavaPlugin {
    private SubcommandLib() {
    }

    /**
     * Registers your Subcommand for use in game based on its name.
     * The name must match one given in plugin.yml for the command to appear in game.
     * If the Subcommand is also a Listener, events will automatically be registered.
     * @param command The Subcommand to register
     * @param plugin  The JavaPlugin to register on the behalf of, usually "this"
     */
    public static void register(@NotNull Subcommand command, @NotNull JavaPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command.getName());
        SubcommandWrapper wrapper = new SubcommandWrapper(command);

        pluginCommand.setExecutor(wrapper);
        pluginCommand.setTabCompleter(wrapper);

        if (command instanceof Listener) Bukkit.getPluginManager().registerEvents((Listener) command, plugin);
    }

    private static class SubcommandWrapper implements CommandExecutor, TabCompleter {
        private final Subcommand subcommand;
        public SubcommandWrapper(Subcommand subcommand) {
            this.subcommand = subcommand;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            // Pass to a nested subcommand if needed
            if (args.length > 0
                    && SubcommandUtils.contains(subcommand, args[0])) {
                return new SubcommandWrapper(SubcommandUtils.findSubcommandByName(subcommand, args[0]))
                        .onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            // False if it cannot be run by itself
            if (!subcommand.execute(sender, TabUtils.parseFlags(Arrays.asList(args), subcommand.getAllowedFlags(sender))))
                sender.sendMessage(ChatColor.RED + "This command cannot be run by itself.");
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            List<String> previouslySupplied = new ArrayList<>(Arrays.asList(args));
            if (!previouslySupplied.isEmpty()) previouslySupplied.remove(previouslySupplied.size()-1);

            // Passes to a nested subcommand if needed
            if (!subcommand.getSubcommands().isEmpty()
                    && !previouslySupplied.isEmpty()
                    && SubcommandUtils.contains(subcommand, previouslySupplied.get(0))) {
                return new SubcommandWrapper(SubcommandUtils.findSubcommandByName(subcommand, previouslySupplied.get(0)))
                        .onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }

            // Initializes the output as all subcommand names
            List<String> output = subcommand.getSubcommands().stream().flatMap(
                    (s) -> Stream.concat(Stream.of(s.getName()), s.getAliases().stream())
            ).map(Object::toString).collect(Collectors.toList());

            // Parsed version of the already given arguments
            List<Either<String, CommandFlag>> arguments = TabUtils.parseFlags(previouslySupplied, subcommand.getAllowedFlags(sender));
            // Checks whether to base the tab completion off valid options for a CommandOption
            if (!arguments.isEmpty()
                    && arguments.get(arguments.size()-1).isRight()
                    && arguments.get(arguments.size()-1).get() instanceof CommandOption
                    && ((CommandOption)arguments.get(arguments.size()-1).get()).getOption() == null
            ) {
                output = ((CommandOption)arguments.get(arguments.size()-1).get()).getSuggestedOptions();
            } else {
                // Adds everything to the output
                List<String> actualTabCompletion = subcommand.tabComplete(sender, arguments, !previouslySupplied.isEmpty() ? previouslySupplied.get(previouslySupplied.size()-1) : "");
                List<CommandFlag> suggestedFlags = subcommand.suggestFlags(sender, arguments);
                List<String> additionalFlags = new ArrayList<>();
                if (args.length != 0) {
                    // Get the current flag sequence to build upon
                    List<CommandFlag> context = TabUtils.parseFlags(Collections.singletonList(args[args.length - 1]), subcommand.getAllowedFlags(sender))
                            .stream()
                            .filter(Either::isRight)
                            .map(Either::get)
                            .collect(Collectors.toList());
                    if (!context.isEmpty() && context.get(context.size()-1).getFlag().length() == 1) {
                        // Add any suggested single-digit flags
                        additionalFlags.addAll(
                                context.get(context.size()-1).getSuggestedNext(context)
                                        .stream()
                                        .filter(character ->
                                                subcommand.getAllowedFlags(sender)
                                                        .stream()
                                                        .filter(flag -> flag.getFlag().length() == 1)
                                                        .map(flag -> flag.getFlag().charAt(0))
                                                        .collect(Collectors.toList())
                                                        .contains(character)
                                        )
                                        .map(character -> "-" + context
                                                .stream()
                                                .map(CommandFlag::getFlag)
                                                .collect(Collectors.joining(""))
                                         + character)
                                        .collect(Collectors.toList())
                        );
                        // I'm gonna look at this code in a year and have no clue what I was thinking with all this stream shit
                    }
                }
                output.addAll(actualTabCompletion);
                output.addAll(suggestedFlags.stream().map(CommandFlag::toString).collect(Collectors.toList()));
                output.addAll(additionalFlags);
            }

            return TabUtils.narrow(output, args.length > 0 ? args[args.length - 1] : "");
        }
    }
}
