package pl.pomoku.mcframework.database;

import pl.pomoku.mcframework.annotation.Entity;

import java.sql.Connection;

public abstract class BaseService<T> {
    protected final Connection connection;
    protected final String tableName;

    public BaseService(Connection connection, Class<T> entityClass) {
        this.connection = connection;
        this.tableName = entityClass.getAnnotation(Entity.class).table();
    }
}
