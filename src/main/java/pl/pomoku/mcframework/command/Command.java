package pl.pomoku.mcframework.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import pl.pomoku.mcframework.annotation.Cmd;
import pl.pomoku.mcframework.command.argumentMatchers.StartingWithStringArgumentMatcher;
import pl.pomoku.mcframework.exceptions.MissingAnnotationException;

import java.util.Collection;

public abstract class Command extends AbstractCommand implements BasicCommand {
    private final Cmd annotation;

    public Command() {
        Cmd annotation = getClass().getAnnotation(Cmd.class);
        if (annotation == null) throw new MissingAnnotationException(Cmd.class);
        this.annotation = annotation;
    }

    public String getName() {
        return this.annotation.name();
    }

    public String[] getAliases() {
        return this.annotation.aliases();
    }

    public String getDescription() {
        return this.annotation.description();
    }

    public ArgumentMatcher getMatcher() {
        try {
            return this.annotation.matcher().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new StartingWithStringArgumentMatcher();
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return runTab(stack.getSender(), args, getMatcher());
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        run(stack.getSender(), args);
    }
}
