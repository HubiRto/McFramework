package pl.pomoku.mcframework.command;

import org.bukkit.command.CommandSender;
import pl.pomoku.mcframework.utils.MatcherUtil;
import pl.pomoku.mcframework.utils.TextUtil;

import java.util.*;

public abstract class AbstractCommand {
    private static final String NO_PERM_MES = "<red>Nie masz uprawnień do tej komendy";

    protected Set<SubCommand> subCommands = new HashSet<>();

    public void addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public void execute(CommandSender sender, String[] args) {

    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    public void run(CommandSender sender, String[] args) {
        if (args.length == 0) {
            execute(sender, args);
            return;
        }

        Optional<SubCommand> optSubCommand = this.subCommands.stream()
                .filter(sc -> sc.getName().equalsIgnoreCase(args[0])).findAny();

        if (optSubCommand.isEmpty()) {
            execute(sender, args);
            return;
        }

        SubCommand subCommand = optSubCommand.get();
        if (subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())) {
            subCommand.run(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(TextUtil.textToComponent(NO_PERM_MES));
        }
    }

    public List<String> runTab(CommandSender sender, String[] args, ArgumentMatcher matcher) {
        if (args.length == 0) return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = tabComplete(sender, args);
            completions.addAll(
                    this.subCommands.stream()
                            .filter(sc -> sc.getPermission().isEmpty() || sender.hasPermission(sc.getPermission()))
                            .map(SubCommand::getName)
                            .toList()
            );

            return MatcherUtil.getMatchingStrings(completions, args[args.length - 1], matcher);
        }

        Optional<SubCommand> optSubCommand = this.subCommands.stream()
                .filter(sc -> sc.getName().equalsIgnoreCase(args[0])).findAny();

        if (optSubCommand.isEmpty()) {
            return Collections.emptyList();
        }

        SubCommand subCommand = optSubCommand.get();
        List<String> completions = subCommand.runTab(sender, Arrays.copyOfRange(args, 1, args.length), matcher);
        return MatcherUtil.getMatchingStrings(completions, args[args.length - 1], matcher);
    }
}
