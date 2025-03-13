package xyz.dragin.subcommandlib.api;

import io.vavr.control.Either;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.api.options.CommandFlag;

import java.util.List;

public interface NoParameterSubcommand extends NoFlagSubcommand {
    @Override
    default @NotNull List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull List<Either<String, CommandFlag>> list, String s) {
        return List.of();
    }
}
