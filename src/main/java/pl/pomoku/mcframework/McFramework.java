package pl.pomoku.mcframework;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import pl.pomoku.mcframework.annotation.*;
import pl.pomoku.mcframework.command.AbstractCommand;
import pl.pomoku.mcframework.command.Command;
import pl.pomoku.mcframework.command.SubCommand;
import pl.pomoku.mcframework.config.ConfigInjector;
import pl.pomoku.mcframework.config.ConfigManager;
import pl.pomoku.mcframework.config.ConfigurablePlugin;
import pl.pomoku.mcframework.database.DatabaseManager;
import pl.pomoku.mcframework.database.TableGenerator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class McFramework<T extends JavaPlugin & ConfigurablePlugin> {

    private static final Map<Class<?>, Object> components = new HashMap<>();
    private final T plugin;

    private ConfigManager<T> configManager;
    private ConfigInjector<T> configInjector;

    public static <T extends JavaPlugin & ConfigurablePlugin> void run(T plugin) {
        McFramework<T> container = new McFramework<>(plugin);
        String basePackage = plugin.getClass().getPackage().getName();

        container.configManager = new ConfigManager<>(plugin);
        container.configInjector = new ConfigInjector<>(container.configManager);

        container.initializeDatabase(basePackage);
        container.registerComponents(basePackage);
        container.registerConfigurationBeans(basePackage);
        container.instantiateComponents();
        container.registerListeners(basePackage);
        container.registerCommands(basePackage);
    }

    public static <T> T getComponent(Class<T> clazz) {
        if (components.containsKey(clazz)) {
            return clazz.cast(components.get(clazz));
        } else {
            throw new IllegalArgumentException("Component or bean not found for class: " + clazz.getName());
        }
    }

    private void initializeDatabase(String basePackage) {
        DatabaseConfig dbConfig = plugin.getClass().getAnnotation(DatabaseConfig.class);
        if (dbConfig != null) {
            DatabaseManager dbManager = new DatabaseManager(
                    dbConfig,
                    this.configManager.getConfig(dbConfig.configFile()),
                    this.plugin.getLogger()
            );
            components.put(DatabaseManager.class, dbManager);
            TableGenerator.createTables(dbManager.getConnection(), basePackage, plugin);
        }
    }

    private void registerComponents(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        componentClasses.forEach(clazz -> components.put(clazz, null));
    }

    private void registerConfigurationBeans(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);

        for (Class<?> configClass : configurationClasses) {
            Object configInstance = createInstanceWithDependencies(configClass);

            for (Method method : configClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Bean.class)) {
                    try {
                        Object bean = method.invoke(configInstance);
                        components.put(method.getReturnType(), bean);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create bean from method: " + method.getName(), e);
                    }
                }
            }
        }
    }

    private void instantiateComponents() {
        components.keySet().forEach(this::createInstanceIfAbsent);
    }

    private synchronized void createInstanceIfAbsent(Class<?> clazz) {
        if (components.get(clazz) != null) return;

        try {
            Object instance = createInstanceWithDependencies(clazz);
            components.put(clazz, instance);

            this.configInjector.injectConfigValues(instance);
        } catch (Exception e) {
            throw new RuntimeException("Error creating instance for: " + clazz.getName(), e);
        }
    }

    private Object createInstanceWithDependencies(Class<?> clazz) {
        try {
            var constructor = clazz.getConstructors()[0];
            var parameterTypes = constructor.getParameterTypes();
            var parameters = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == JavaPlugin.class) {
                    parameters[i] = plugin;
                } else {
                    parameters[i] = resolveDependency(parameterTypes[i]);
                }
            }

            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for class: " + clazz.getName(), e);
        }
    }

    private Object resolveDependency(Class<?> dependencyClass) {
        if (components.containsKey(dependencyClass)) {
            return components.get(dependencyClass);
        }
        return createInstanceWithDependencies(dependencyClass);
    }

    private void registerListeners(String basePackage) {
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> listenerClasses = reflections.getTypesAnnotatedWith(EventListener.class);

        for (Class<?> listenerClass : listenerClasses) {
            Object listenerInstance = createInstanceWithDependencies(listenerClass);
            this.configInjector.injectConfigValues(listenerInstance);
            registerListener((Listener) listenerInstance);
        }
    }

    private void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    private void registerCommand(Command command) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            if (command.getAliases() == null || command.getAliases().length == 0) {
                commands.register(
                        command.getName(),
                        command.getDescription(),
                        command
                );
            } else {
                commands.register(
                        command.getName(),
                        command.getDescription(),
                        Arrays.stream(command.getAliases()).toList(),
                        command
                );
            }
        });
    }

    private void registerCommands(String basePackage) {
        Reflections reflections = new Reflections(basePackage);

        Map<Class<?>, Command> commandMap = new HashMap<>();
        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Cmd.class);
        for (Class<?> cmdClass : commandClasses) {
            Command cmdInstance = (Command) createInstanceWithDependencies(cmdClass);
            this.configInjector.injectConfigValues(cmdInstance);
            commandMap.put(cmdClass, cmdInstance);
        }

        Map<Class<?>, SubCommand> subCommandMap = new HashMap<>();
        Set<Class<?>> subCommandClasses = reflections.getTypesAnnotatedWith(SubCmd.class);
        for (Class<?> subCmdClass : subCommandClasses) {
            SubCommand subCmdInstance = (SubCommand) createInstanceWithDependencies(subCmdClass);
            this.configInjector.injectConfigValues(subCmdInstance);
            subCommandMap.put(subCmdClass, subCmdInstance);
        }

        for (SubCommand subCmdInstance : subCommandMap.values()) {
            Class<? extends AbstractCommand> parentClass = subCmdInstance.getParent();
            if (commandMap.containsKey(parentClass)) {
                Command parentCommand = commandMap.get(parentClass);
                parentCommand.addSubCommand(subCmdInstance);
            } else if (subCommandMap.containsKey(parentClass)) {
                SubCommand parentSubCommand = subCommandMap.get(parentClass);
                parentSubCommand.addSubCommand(subCmdInstance);
            } else {
                throw new RuntimeException("Parent not found for SubCommand: " + subCmdInstance.getClass().getName());
            }
        }

        for (Command cmdInstance : commandMap.values()) {
            registerCommand(cmdInstance);
        }
    }
}
