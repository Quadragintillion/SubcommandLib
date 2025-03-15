# SubcommandLib
A nice library for custom Spigot commands, subcommands and tab completion

## About/Logic
In SubcommandLib, every command is a subcommand, including the base command (the part with the /). All these subcommands can have any amount of subcommand "children." This creates a sort of tree of subcommands, which is great for organization. Any subcommand can be registered as a base command using `SubcommandLib.register()`.

SubcommandLib also has a powerful argument system. You can define CommandFlags, which are optional parameters that can change things about the command without replacing a normal argument. Flags start with either a `--` (longer names) or `-` (single characters). There are also CommandOptions, which take in an additional parameter, e.g. `--name MyName`. Single character CommandFlags (but NOT CommandOptions) can be grouped together, e.g. `-Syu`.

[Link to documentation](https://dragin.xyz/javadoc/subcommandlib)

## Adding as a Dependency

TBA - Waiting for approval from CodeMC

## Example

<details>
<summary>Code</summary>

### SubcommandLibDemo.java
```java
public final class SubcommandLibDemo extends JavaPlugin {
  @Override
  public void onEnable() {
    SubcommandLib.register(new TestCommand(), this);
  }
}
```

### commands/test/TestCommand.java
```java
public class TestCommand implements Subcommand {
  @Override
  public @NotNull String getName() {
    return "test";
  }
  @Override
  public @NotNull List<Subcommand> getSubcommands() {
    return List.of(
        new TestSubcommand1(),
        new TestSubcommand2(),
        new TestCommand() // An infinitely recursive subcommand, because why not
    );
  } // This command does not override execute(), so it cannot be executed.
}
```

### commands/test/TestSubcommand1.java
```java
public class TestSubcommand1 implements Subcommand {
    @Override
    public @NotNull String getName() {
        return "subcommand1";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments) {
        // Split the arguments into normal arguments in flags, because in this case the order doesn't matter
        List<String> args = SubcommandUtils.getNormalArguments(arguments);
        List<CommandFlag> flags = SubcommandUtils.getFlags(arguments);

        if (args.size() == 1) {
            Player player = Bukkit.getPlayerExact(args.getFirst());
            if (player != null) {
                sender.sendMessage(ChatColor.GREEN + "Ran the command on " + args.getFirst() + "!");
                player.sendMessage(ChatColor.YELLOW + "Someone ran a command on you!");

                // --optional strikes them with lightning
                if (flags.contains(new CommandFlag("optional") { @Override public List<Character> getSuggestedNext(List<CommandFlag> previous) { return List.of(); }})) {;
                    player.getWorld().strikeLightning(player.getLocation());
                }

                // Check for -f and -r independently, although they can be typed together since they're one character and not CommandOptions
                // CommandFlag.simple() gives you a dummy CommandFlag for comparison purposes
                sender.sendMessage(flags.contains(CommandFlag.simple("f")) ? (ChatColor.GOLD + "flag -f specified") : (ChatColor.YELLOW + "flag -f not specified"));
                sender.sendMessage(flags.contains(CommandFlag.simple("r")) ? (ChatColor.GOLD + "flag -r specified") : (ChatColor.YELLOW + "flag -r not specified"));

                // Check whether --optional-with-option was specified
                // CommandOption.simple() functions the same as CommandFlag.simple()
                if (flags.contains(CommandOption.simple("optional-with-option"))) {
                    // Use the SubcommandUtils.findFromSimple() method to find the full flag
                    CommandOption option = (CommandOption) SubcommandUtils.findFromSimple(CommandOption.simple("optional-with-option"), flags);
                    if (option.getOption() != null) {
                        switch (option.getOption()) {
                            case "option1" -> sender.sendMessage(ChatColor.DARK_PURPLE + "You chose option 1");
                            case "option2" -> sender.sendMessage(ChatColor.DARK_BLUE + "You chose option 2");
                            case "pickme!!" -> sender.sendMessage(ChatColor.GREEN + "Your bank account has just been charged $192,331.97.");
                            default -> sender.sendMessage(ChatColor.RED + "That isn't a valid option!");
                        }
                    } else sender.sendMessage(ChatColor.RED + "You didn't specify an option to --optional-with-option");
                }
            } else sender.sendMessage(ChatColor.RED + "That player could not be found.");
        } else sender.sendMessage(ChatColor.RED + "You must specify a username!");

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments, String typed) {
        // Return the names of all online players, or nothing if there's already an argument
        return SubcommandUtils.getNormalArguments(arguments).isEmpty() ? Bukkit.getOnlinePlayers().stream().map(Player::getDisplayName).toList() : List.of();
    }

    @Override
    public @NotNull List<CommandFlag> getAllowedFlags(@NotNull CommandSender sender) {
        return List.of(
                CommandFlag.simple("optional"), // Flag --optional
                new CommandOption("optional-with-option") { // Flag --optional-with-option
                    @Override
                    public @NotNull List<String> getSuggestedOptions() {
                        return List.of("option1", "option2", "pickme!!"); // Suggested parameter choices for tab completion
                    }
                },
                new CommandFlag("f") { // Flag -f
                    @Override
                    public List<Character> getSuggestedNext(List<CommandFlag> previous) {
                        // Suggest -r unless it's already been given
                        return previous.contains(CommandFlag.simple("r")) ? List.of() : List.of('r');
                    }
                },
                new CommandFlag("r") { // Flag -r
                    @Override
                    public List<Character> getSuggestedNext(List<CommandFlag> previous) {
                        // Suggest -f unless it's already been given
                        return previous.contains(CommandFlag.simple("f")) ? List.of() : List.of('f');
                    }
                }
        );
    }

    // The default behavior for suggestFlags() is to suggest any flags that haven't already been used. In this case, we don't need to override it.
}
```

### commands/test/Subcommand2.java
```java
public class TestSubcommand2 implements Subcommand {
    @Override
    public @NotNull String getName() {
        return "subcommand2";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments) {
        // Send the player nothing but their arguments as a formatted array
        // All the stream stuff does is convert all the arguments into strings
        sender.sendMessage(Arrays.toString(arguments
                .stream()
                .map(Object::toString)
                .toArray()
        ));
        return true;
    }
}
```

</details>

[Result](https://youtu.be/HIiT0cBvs4Y)
