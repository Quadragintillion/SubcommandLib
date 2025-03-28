package xyz.dragin.subcommandlib.util;

import io.vavr.control.Either;
import xyz.dragin.subcommandlib.Subcommand;
import xyz.dragin.subcommandlib.options.CommandFlag;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for Subcommands
 */
public final class SubcommandUtils {
    private SubcommandUtils() {}

    /**
     * Checks whether a Subcommand has a certain child, for passing control when tab completing and executing
     * @param parent The parent Subcommand
     * @param childName The name of the child to check for
     * @return Whether the name exists as a child of the parent
     */
    public static boolean contains(Subcommand parent, String childName) {
        return parent.getSubcommands().stream().flatMap(
                (s) -> Stream.concat(Stream.of(s.getName()), s.getAliases().stream())
        ).collect(Collectors.toList()).contains(childName);
    }

    /**
     * Finds the Subcommand child of a Subcommand parent by its name
     * @param parent The Subcommand parent
     * @param childName The name of the child
     * @return The child Subcommand
     */
    public static Subcommand findSubcommandByName(Subcommand parent, String childName) {
        return parent.getSubcommands().stream().filter(
                (subcommand) -> subcommand.getName().equals(childName) || subcommand.getAliases().contains(childName)
        ).collect(Collectors.toList()).get(0);
    }

    /**
     * Filters all normal arguments from a list of variable arguments
     * @param arguments Variable argument types
     * @return Only specified normal arguments
     */
    public static List<String> getNormalArguments(List<Either<String, CommandFlag>> arguments) {
        return arguments.stream().filter(Either::isLeft).map(Either::getLeft).collect(Collectors.toList());
    }

    /**
     * Filters all flags from a list of variable arguments
     * @param arguments Variable argument types
     * @return Only specified flags
     */
    public static List<CommandFlag> getFlags(List<Either<String, CommandFlag>> arguments) {
        return arguments.stream().filter(Either::isRight).map(Either::get).collect(Collectors.toList());
    }

    /**
     * Finds a flag by a simple comparison instance
     * @param simple The instance for comparison
     * @param arguments Arguments to search through
     * @return The found CommandFlag or null
     */
    public static CommandFlag findFromSimple(CommandFlag simple, List<CommandFlag> arguments) {
        return arguments
                .stream()
                .filter(flag -> flag.equals(simple))
                .collect(Collectors.toList())
                .get(0);
    }
}
