package xyz.dragin.subcommandlib.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents a command option (e.g. -o option) with a parameter
 * @see CommandFlag
 */
public abstract class CommandOption extends CommandFlag {
    private String option;

    /**
     * Gets the option parameter
     * @return The option parameter given or null
     */
    @Nullable public String getOption() { return option; }

    /**
     * Sets the parameter of the flag
     * @param option The new parameter
     */
    public void setOption(String option) { this.option = option; }

    /**
     * A list of suggested options used for tab completion after the flag
     * @return The list of suggested options
     */
    @NotNull public abstract List<String> getSuggestedOptions();

    /**
     * Initialization constructor without an option, for when an option hasn't been passed yet
     * @param flag The name of the flag without any dashes
     */
    public CommandOption(String flag) {
        this(flag, null);
    }

    /**
     * Initialization constructor with an option, for when an option has been passed
     * @param flag The name of the flag without any dashes
     * @param option The passed option
     */
    public CommandOption(String flag, String option) {
        super(flag);
        this.option = option;
    }

    public final List<Character> getSuggestedNext(List<CommandFlag> previous) { return Collections.emptyList(); }

    @Override
    public CommandOption clone() {
        return (CommandOption) super.clone();
    }

    /**
     * Convenience function for getting a CommandOption for comparison
     * without having to implement getSuggestedOptions
     * @param name The name of the flag without any dashes
     * @return A CommandOption for comparison purposes
     */
    public static CommandOption simple(String name) {
        return new CommandOption(name) {
            @Override
            public @NotNull List<String> getSuggestedOptions() {
                return Collections.emptyList();
            }
        };
    }
}
