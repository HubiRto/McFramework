package pl.pomoku.mcframework.database;

import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import pl.pomoku.mcframework.annotation.Column;
import pl.pomoku.mcframework.annotation.Entity;
import pl.pomoku.mcframework.annotation.Id;
import pl.pomoku.mcframework.utils.TextUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

public class TableGenerator {
    public static void createTables(Connection connection, String packageName, JavaPlugin plugin) {
        try {
            Reflections reflections = new Reflections(packageName, Scanners.TypesAnnotated);
            Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
            plugin.getServer().getConsoleSender().sendMessage(TextUtil.textToComponent("<gray>Znaleziono <dark_gray>(<aqua>%d</aqua>)</dark_gray> klas encji: ".formatted(entities.size())));

            for (Class<?> entityClass : entities) {
                createTable(connection, entityClass, plugin);
            }
        } catch (Exception e) {
            plugin.getServer().getConsoleSender().sendMessage(TextUtil.textToComponent("<red>Problem przy wczytywaniu klas encji @Entity"));
        }
    }

    private static void createTable(Connection connection, Class<?> entityClass, JavaPlugin plugin) throws Exception {
        Entity entity = entityClass.getAnnotation(Entity.class);
        StringBuilder createQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + entity.table() + " (");

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                createQuery.append(column.name()).append(" ").append(column.type());

                if (field.isAnnotationPresent(Id.class)) {
                    Id idAnnotation = field.getAnnotation(Id.class);
                    if (idAnnotation.isUUID()) {
                        createQuery.append(" PRIMARY KEY DEFAULT gen_random_uuid()");
                    } else {
                        createQuery.append(" PRIMARY KEY");
                    }
                }

                createQuery.append(", ");
            }
        }
        createQuery.setLength(createQuery.length() - 2);
        createQuery.append(");");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createQuery.toString());
            plugin.getServer().getConsoleSender().sendMessage(TextUtil.textToComponent("<gray>Stworzono tablę <light_purple>" + TextUtil.capitalize(entity.table())));
        }
    }
}
