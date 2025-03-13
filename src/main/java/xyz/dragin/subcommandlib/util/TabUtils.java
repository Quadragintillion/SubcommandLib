package xyz.dragin.subcommandlib.util;

import io.vavr.control.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.dragin.subcommandlib.api.options.CommandFlag;
import xyz.dragin.subcommandlib.api.options.CommandOption;

import java.util.ArrayList;
import java.util.List;

public class TabUtils {
    private TabUtils() {}

    /**
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
                CommandFlag flag = null;
                for (CommandFlag allowedFlag : allowedFlags) {
                    if (arg.equals(allowedFlag.toString())) {
                        flag = allowedFlag;
                        break;
                    }
                }
                if (flag == null) output.add(Either.left(arg));
                else if (flag instanceof CommandOption option) {
                    nextOption = option.clone();
                } else output.add(Either.right(flag));
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
