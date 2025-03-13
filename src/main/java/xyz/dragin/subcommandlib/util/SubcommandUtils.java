package xyz.dragin.subcommandlib.util;

import xyz.dragin.subcommandlib.api.Subcommand;

public final class SubcommandUtils {
    private SubcommandUtils() {}
    public static boolean contains(Subcommand parent, String childName) {
        return parent.getSubcommands().stream().map(Subcommand::getName).toList().contains(childName);
    }
    public static Subcommand findSubcommandByName(Subcommand parent, String childName) {
        return parent.getSubcommands().stream().filter(
                (subcommand) -> subcommand.getName().equals(childName)
        ).toList().getFirst();
    }
}
