package pl.pomoku.mcframework.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DatabaseType {
    POSTGRESQL("org.postgresql.Driver");

    private final String driverClassName;
}
