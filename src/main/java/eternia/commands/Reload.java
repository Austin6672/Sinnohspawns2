package eternia.commands;

import eternia.configuration.ConfigManager;
import eternia.utilities.Permissions;


public class Reload implements CommandExecutor{

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        ConfigManager.load();
        src.sendMessage(Text.of(TextColors.YELLOW, "SinnohSpawns has been reloaded"));

        return CommandResult.success();
    }

    public static CommandSpec build() {
        return CommandSpec.builder()
                .permission(Permissions.RELOAD)
                .executor(new Reload())
                .build();
    }
}