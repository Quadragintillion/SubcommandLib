package xyz.dragin.subcommandlib.util;

import io.vavr.control.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.dragin.subcommandlib.options.CommandFlag;
import xyz.dragin.subcommandlib.options.CommandOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for tab completion
 */
public final class TabUtils {
    private TabUtils() {}

    /**
     * Checks for valid flags in a command
     * @param arguments The arguments passed to the command to parse
     * @param allowedFlags The List of flags to consider when parsing, otherwise everything is interpreted as normal arguments
     * @return A List of Objects, each either a String or CommandFlag, that have been supplied
     */
    public static List<Either<String, CommandFlag>> parseFlags(@NotNull List<String> arguments, @NotNull List<CommandFlag> allowedFlags) {
        List<Either<String, CommandFlag>> output = new ArrayList<>();
        CommandOption nextOption = null;
        for (String arg : arguments) {
            if (nextOption != null) {
                nextOption.setOption(arg);
                output.add(Either.right(nextOption));
                nextOption = null;
            } else {
                List<CommandFlag> flags = new ArrayList<>();

                if (arg.startsWith("--")) {
                    for (CommandFlag allowedFlag : allowedFlags) {
                        if (allowedFlag.toString().equals(arg)) flags = List.of(allowedFlag);
                    }
                } else if (arg.startsWith("-")) {
                    for (char c : arg.substring(1).toCharArray()) {
                        CommandFlag matchingFlag = null;
                        for (CommandFlag allowedFlag : allowedFlags) {
                            if (allowedFlag.getFlag().equals("" + c)) matchingFlag = allowedFlag;
                        }
                        if (matchingFlag == null) {
                            flags = List.of();
                            break;
                        } else flags.add(matchingFlag);
                    }
                }

                if (flags.isEmpty()) output.add(Either.left(arg));
                else if (flags.size() == 1 && flags.getFirst() instanceof CommandOption option) {
                    nextOption = option.clone();
                } else output.addAll(flags.stream().map(Either::<String, CommandFlag>right).toList());
            }
        }
        if (nextOption != null) output.add(Either.right(nextOption));
        return output;
    }

    /**
     * Narrows down options based on what's already typed
     * @param options All available options
     * @param typed What's already typed
     * @return All the options that start with what was typed
     */
    public static List<String> narrow(@NotNull List<String> options, @Nullable String typed) {
        List<String> output = new ArrayList<>();
        for (String option : options) {
            if (typed == null || option.startsWith(typed)) output.add(option);
        }
        return output;
    }
}
