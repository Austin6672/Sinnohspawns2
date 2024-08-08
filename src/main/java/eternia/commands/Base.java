package eternia.commands;

import eternia.managers.ZoneManager;
import eternia.utilities.Permissions;


public class Base implements CommandExecutor{

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        PaginationList.builder()
                .title(Text.of(TextColors.GREEN, "Sinnoh Spawns"))
                .padding(Text.of(TextColors.YELLOW, "="))
                .contents(Text.of(TextColors.GREEN, "/as help"),
                        (Text.of(TextColors.GREEN, "/as create <name> <radius>")),
                        (Text.of(TextColors.GREEN, "/as delete <name>")),
                        (Text.of(TextColors.GREEN, "/as list")),
                        (Text.of(TextColors.GREEN, "/as add <zone> <alias> <rarity>")),
                        (Text.of(TextColors.GREEN, "/as reload")))
                .sendTo(src);

        return CommandResult.success();
    }

    public static CommandSpec build() {
        return CommandSpec.builder()
                .permission(Permissions.BASE)
                .executor(new Base())
                .child(Add.build(), "add")
                .child(Base.help(), "help")
                .child(Reload.build(), "reload")
                .child(Create.build(), "create")
                .child(Delete.build(), "delete")
                .child(ListRoutes.build(), "list")
                .child(CommandSpec.builder()
                                .permission("eternia.admin")
                                .arguments(GenericArguments.integer(Text.of("radius")))
                                .executor(((src, args) -> {
                                    if (src instanceof Player) {
                                        src.sendMessage(Text.of(ZoneManager.getZonesNearby(
                                                ((Player) src).getPosition(),
                                                args.<Integer>getOne("radius").get()
                                        )));
                                    }
                                    return CommandResult.empty();
                                }))
                                .build(),
                        "nearbyzones"
                )
                .build();
    }

    private static CommandSpec help() {
        return CommandSpec.builder()
                .executor(new Base())
                .build();
    }
}