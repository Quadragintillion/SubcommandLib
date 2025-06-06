# SubcommandLib
A nice library for custom Spigot commands, subcommands and tab completion

## SubcommandLib will likely no longer be updated
I created SubcommandLib for myself because I thought there wasn't a good library for commands. However, I recently found that [CommandAPI](https://www.spigotmc.org/resources/api-commandapi-1-16-5-1-21-4.62353/) has a far better system, including things like built-in error checking. I may later create a fork or extension of CommandAPI that contains Bash command-line features like SubcommandLib's optional CommandFlags and spaced arguments using quotations (e.g. "this is one argument"). However, as of now, I see no reason to continue developing this.

## About/Logic
In SubcommandLib, every command is a subcommand, including the base command (the part with the /). All these subcommands can have any amount of subcommand "children." This creates a sort of tree of subcommands, which is great for organization. Any subcommand can be registered as a base command using `SubcommandLib.register()`.

SubcommandLib also has a powerful argument system. You can define CommandFlags, which are optional parameters that can change things about the command without replacing a normal argument. Flags start with either a `--` (longer names) or `-` (single characters). There are also CommandOptions, which take in an additional parameter, e.g. `--name MyName`. Single character CommandFlags (but NOT CommandOptions) can be grouped together, e.g. `-Syu`.

[Link to documentation](https://dragin.xyz/javadoc/subcommandlib)

[Support Discord](https://discord.dragin.xyz)

## Adding as a Dependency

The current verison of SubcommandLib is 1.5.1.

SubcommandLib uses the Either class from Vavr.

SubcommandLib is not a plugin, only a library. You will need to [shade it](https://gradleup.com/shadow/).

<details>
<summary>Maven</summary>

```xml
<project>
    <repositories>
        <repository>
            <id>codemc</id>
            <url>https://repo.codemc.io/repository/quadragintillion/</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>xyz.dragin</groupId>
            <artifactId>subcommandlib</artifactId>
            <version>VERSION</version>
        </dependency>
        <dependency>
            <groupId>io.vavr</groupId>
            <artifactId>vavr</artifactId>
            <version>0.10.6</version>
        </dependency>
    </dependencies>
</project>
```
</details>
<details>
<summary>Gradle (Groovy)</summary>

```gradle
repositories {
    maven { url 'https://repo.codemc.io/repository/quadragintillion/' }
}

dependencies {
    implementation 'xyz.dragin:subcommandlib:VERSION'
    implementation 'io.vavr:vavr:0.10.6'
}
```
</details>
<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    maven(url = "https://repo.codemc.io/repository/quadragintillion/")
}

dependencies {
    implementation("xyz.dragin:subcommandlib:VERSION")
    implementation("io.vavr:vavr:0.10.6")
}
```
</details>

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
        new ComplexSubcommand(), // A more complex subcommand (subcommand1)
        new SimpleSubcommand(), // A more simple sumbcommand (subcommand2)
        new TestCommand() // An infinitely recursive subcommand, because why not
    );
  } // This command does not override execute(), so it cannot be executed.
}
```

### commands/test/ComplexSubcommand.java
```java
public class ComplexSubcommand implements Subcommand {
    @Override
    public @NotNull String getName() {
        return "subcommand1";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("alias");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull List<Either<String, CommandFlag>> arguments) {
        // Split the arguments into normal arguments in flags, because in this case the order doesn't matter
        List<String> args = SubcommandUtils.getNormalArguments(arguments);
        List<CommandFlag> flags = SubcommandUtils.getFlags(arguments);

        if (args.size() == 1) {
            Player player = Bukkit.getPlayerExact(args.getFirst());
            if (player != null) {
                sender.sendMessage(ChatColor.GREEN + "Ran subcommand1 on " + args.getFirst() + "!");
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

### commands/test/SimpleSubcommand.java
```java
public class SimpleSubcommand implements Subcommand {
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
