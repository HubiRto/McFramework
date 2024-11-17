package pl.pomoku.mcframework.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface EntityMapper<T> {
    T mapFromResultSet(ResultSet resultSet) throws Exception;
    void mapToParameters(PreparedStatement statement, T entity) throws Exception;
}
