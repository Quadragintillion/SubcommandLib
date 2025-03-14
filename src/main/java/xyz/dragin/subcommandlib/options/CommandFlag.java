package xyz.dragin.subcommandlib.options;

import java.util.List;

/**
 * Represents an optional command flag (e.g. -f) with no parameters
 * @see CommandOption
 */
public abstract class CommandFlag implements Cloneable {
    private final String flag;

    /**
     * Gets the name of the flag, not including any dashes
     * @return The name of the flag, not including any dashes
     */
    public String getFlag() { return flag; }

    /**
     * Initialization constructor
     * @param flag The name of the flag, not including any dashes
     */
    public CommandFlag(String flag) {
        this.flag = flag;
    }

    /**
     * Gets the flag as a String to be used in the tab completion menu and for general human readability
     * @return The flag name including dashes
     */
    public String toString() {
        return (flag.length() > 1 ? "--" : "-") + getFlag();
    }

    /**
     * Gets any suggested flags that can be used in addition to this one (e.g. -zxvf)
     * @param previous All previously supplied flags in the "cluster"
     * @return List of single characters to suggest
     */
    public abstract List<Character> getSuggestedNext(List<CommandFlag> previous);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommandFlag other) return flag.equals(other.flag);
        else return false;
    }

    @Override
    public CommandFlag clone() {
        try {
            return (CommandFlag) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Convenience function for getting a CommandFlag for comparison
     * or for CommandFlags (specifically with longer names) that can't be in a "cluster"
     * without having to implement getSuggestedNext
     * @param name The name of the flag without any dashes
     * @return A CommandFlag for comparison purposes
     */
    public static CommandFlag simple(String name) {
        return new CommandFlag(name) {
            @Override
            public List<Character> getSuggestedNext(List<CommandFlag> previous) {
                return List.of();
            }
        };
    }
}
