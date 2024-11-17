package pl.pomoku.mcframework.annotation;

import pl.pomoku.mcframework.command.ArgumentMatcher;
import pl.pomoku.mcframework.command.argumentMatchers.StartingWithStringArgumentMatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Cmd {
    String name();
    String[] aliases() default {};
    String description() default "";
    Class<? extends ArgumentMatcher> matcher() default StartingWithStringArgumentMatcher.class;
}
