package pl.pomoku.mcframework.annotation;

import pl.pomoku.mcframework.database.DatabaseType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatabaseConfig {
    DatabaseType type();
    String configFile();
    boolean autoReconnect() default true;
    String characterEncoding() default "utf8";
    boolean useSSL() default false;
    int maxPoolSize() default 10;
    int minIdleConnections() default 1;
    int connectionTimeout() default 30000;
    String additionalOptions() default "";

    String hostPath() default "database.host";
    String portPath() default "database.port";
    String dbPath() default "database.db";
    String userPath() default "database.user";
    String passwordPath() default "database.password";
}
