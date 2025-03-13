package xyz.dragin.subcommandlib.api.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a command option (e.g. -o option) with a parameter
 * @see CommandFlag
 */
public abstract class CommandOption extends CommandFlag {
    private String option;

    /**
     * @return The option parameter given or null
     */
    @Nullable public String getOption() { return option; }

    /**
     * @param option The new parameter
     */
    public void setOption(String option) { this.option = option; }

    /**
     * @return A list of options for tab completion after the flag
     */
    @NotNull public abstract List<String> getSuggestedOptions();

    public CommandOption(String flag) {
        this(flag, null);
    }
    public CommandOption(String flag, String option) {
        super(flag);
        this.option = option;
    }

    @Override
    public CommandOption clone() {
        return (CommandOption) super.clone();
    }
}
