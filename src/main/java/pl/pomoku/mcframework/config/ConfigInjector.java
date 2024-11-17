package pl.pomoku.mcframework.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.pomoku.mcframework.annotation.Component;
import pl.pomoku.mcframework.annotation.ConfigProperty;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ConfigInjector<T extends JavaPlugin & ConfigurablePlugin> {
    private final ConfigManager<T> configManager;

    public void injectConfigValues(Object instance) {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
                FileConfiguration config = configManager.getConfig(configProperty.fileName());

                Object value = config.get(configProperty.path());
                if (value != null) {
                    field.setAccessible(true);
                    try {
                        Object convertedValue = convertValue(value, field.getType());
                        field.set(instance, convertedValue);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(String.format("Failed to inject config value for %s in class %s",
                                field.getName(), clazz.getName()), e);
                    }
                }
            }
        }
    }


    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isAssignableFrom(value.getClass())) {
            return value; // Typy już się zgadzają
        }

        if (targetType == String.class) {
            return value.toString();
        }

        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString());
        }

        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value.toString());
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }

        if (targetType == Map.class && value instanceof Map) {
            return value;
        }
        if (targetType == List.class && value instanceof List) {
            return value;
        }

        if (targetType.isAnnotationPresent(Component.class)) {
            try {
                Object customObject = targetType.getDeclaredConstructor().newInstance();
                injectConfigValues(customObject);
                return customObject;
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate custom object for config injection: " + targetType.getName(), e);
            }
        }

        throw new UnsupportedOperationException("Unsupported config injection type: " + targetType.getName());
    }
}
