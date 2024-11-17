package pl.pomoku.mcframework.annotation;

import pl.pomoku.mcframework.command.AbstractCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCmd {
    String name();
    String permission() default "";
    Class<? extends AbstractCommand> parent();
}
