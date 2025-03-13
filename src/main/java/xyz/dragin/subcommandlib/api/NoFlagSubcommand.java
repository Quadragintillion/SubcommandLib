package xyz.dragin.subcommandlib.api;

import io.vavr.control.Either;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.dragin.subcommandlib.api.options.CommandFlag;

import java.util.List;

public interface NoFlagSubcommand extends Subcommand {
    @Override
    default @NotNull List<CommandFlag> getAllowedFlags(@NotNull CommandSender commandSender) {
        return List.of();
    }

    @Override
    default @NotNull List<CommandFlag> suggestFlags(@NotNull CommandSender commandSender, @NotNull List<Either<String, CommandFlag>> list) {
        return List.of();
    }
}
