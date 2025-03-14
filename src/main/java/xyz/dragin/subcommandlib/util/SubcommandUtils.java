package xyz.dragin.subcommandlib.util;

import xyz.dragin.subcommandlib.api.Subcommand;

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
        return parent.getSubcommands().stream().map(Subcommand::getName).toList().contains(childName);
    }

    /**
     * Finds the Subcommand child of a Subcommand parent by its name
     * @param parent The Subcommand parent
     * @param childName The name of the child
     * @return The child Subcommand
     */
    public static Subcommand findSubcommandByName(Subcommand parent, String childName) {
        return parent.getSubcommands().stream().filter(
                (subcommand) -> subcommand.getName().equals(childName)
        ).toList().getFirst();
    }
}
