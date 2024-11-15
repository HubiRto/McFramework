package pl.pomoku.mcframework;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import pl.pomoku.mcframework.annotation.CommandComponent;
import pl.pomoku.mcframework.annotation.Component;
import pl.pomoku.mcframework.annotation.ListenerComponent;
import pl.pomoku.mcframework.config.ConfigInjector;
import pl.pomoku.mcframework.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class McFramework {

    private static final Map<Class<?>, Object> components = new HashMap<>();
    private final JavaPlugin plugin;

    private ConfigManager configManager;
    private ConfigInjector configInjector;

    public static void initialize(JavaPlugin plugin, String basePackage) {
        McFramework container = new McFramework(plugin);

        container.configManager = new ConfigManager(plugin);
        container.configInjector = new ConfigInjector(container.configManager);

        container.registerComponents(basePackage);
        container.instantiateComponents();
        container.registerListeners();
        container.registerCommands();
    }

    public static  <T> T getComponent(Class<T> clazz) {
        if (components.containsKey(clazz)) {
            return clazz.cast(components.get(clazz));
        } else {
            throw new IllegalArgumentException("Component not found for class: " + clazz.getName());
        }
    }

    private void registerComponents(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        componentClasses.forEach(clazz -> components.put(clazz, null));
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

    private Object createInstanceWithDependencies(Class<?> clazz) throws Exception {
        var constructor = clazz.getConstructors()[0];
        var parameterTypes = constructor.getParameterTypes();
        var parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = getDependency(parameterTypes[i]);
        }

        return constructor.newInstance(parameters);
    }

    private Object getDependency(Class<?> dependencyClass) {
        return components.computeIfAbsent(dependencyClass, clazz -> {
            createInstanceIfAbsent(clazz);
            return components.get(clazz);
        });
    }

    private void registerListeners() {
        components.values().stream()
                .filter(component -> component.getClass().isAnnotationPresent(ListenerComponent.class))
                .forEach(listener -> registerListener((Listener) listener));
    }

    private void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    private void registerCommands() {
        components.values().stream()
                .filter(component -> component.getClass().isAnnotationPresent(CommandComponent.class))
                .forEach(this::handleCommandObject);
    }

    private void handleCommandObject(Object command) {
        if (command instanceof CommandExecutor commandExecutor) {
            CommandComponent annotation = command.getClass().getAnnotation(CommandComponent.class);
            String commandName = annotation.name();

            if (commandName.isEmpty()) {
                throw new NullPointerException("The 'name' attribute in @CommandComponent cannot be empty");
            }

            registerCommand(commandName, commandExecutor);
        }
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand pluginCommand = this.plugin.getCommand(name);
        if (pluginCommand == null) {
            throw new NullPointerException("Command name is missing in plugin.yml for command: " + name);
        }
        pluginCommand.setExecutor(executor);
    }
}
