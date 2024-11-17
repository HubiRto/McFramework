package pl.pomoku.mcframework.command;

import pl.pomoku.mcframework.annotation.SubCmd;
import pl.pomoku.mcframework.exceptions.MissingAnnotationException;

public abstract class SubCommand extends AbstractCommand {
    private final SubCmd annotation;

    public SubCommand() {
        SubCmd annotation = getClass().getAnnotation(SubCmd.class);
        if(annotation == null) throw new MissingAnnotationException(SubCmd.class);
        this.annotation = annotation;
    }

    public String getName() {
        return this.annotation.name();
    }

    public String getPermission() {
        return this.annotation.permission();
    }

    public Class<? extends AbstractCommand> getParent() {
        return this.annotation.parent();
    }
}
