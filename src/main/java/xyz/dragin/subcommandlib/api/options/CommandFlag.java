package xyz.dragin.subcommandlib.api.options;

/**
 * Represents an optional command flag (e.g. -f) with no parameters
 * @see CommandOption
 */
public class CommandFlag implements Cloneable {
    private final String flag;

    /**
     * @return The name of the flag, not including any dashes
     */
    public String getFlag() { return flag; }

    /**
     * @param flag The name of the flag, not including any dashes
     */
    public CommandFlag(String flag) {
        this.flag = flag;
    }

    /**
     * @return The flag name including dashes
     */
    public String toString() {
        return (flag.length() > 1 ? "--" : "-") + getFlag();
    }

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
}
