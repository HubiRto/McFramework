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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class DIContainer {

    private final Map<Class<?>, Object> components = new HashMap<>();
    private final JavaPlugin plugin;

    public static void initialize(JavaPlugin plugin, String basePackage) {
        DIContainer container = new DIContainer(plugin);
        container.scanAndRegisterComponents(basePackage);
        container.registerListeners();
        container.registerCommands();
    }

    public void scanAndRegisterComponents(String basePackage) {
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);
        componentClasses.forEach(this::registerComponent);

        Set<Class<?>> listenerClasses = reflections.getTypesAnnotatedWith(ListenerComponent.class);
        listenerClasses.forEach(this::registerComponent);

        Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(CommandComponent.class);
        commandClasses.forEach(this::registerComponent);
    }

    public void registerComponent(Class<?> clazz) {
        if (this.components.containsKey(clazz)) return;

        try {
            Object instance = createInstanceWithDependencies(clazz);
            this.components.put(clazz, instance);
        } catch (Exception e) {
            throw new RuntimeException("Error while registering component: %s");
        }
    }

    private Object createInstanceWithDependencies(Class<?> clazz) throws Exception {
        var constructor = clazz.getConstructors()[0];
        var parameterTypes = constructor.getParameterTypes();
        var parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = resolveDependency(parameterTypes[i]);
        }

        return constructor.newInstance(parameters);
    }

    private Object resolveDependency(Class<?> dependencyClass) {
        return this.components.computeIfAbsent(dependencyClass, clazz -> {
            try {
                registerComponent(clazz);
                return this.components.get(clazz);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Unable to create an instance for class: %s", clazz.getName()), e);
            }
        });
    }

    private void registerListeners() {
        this.components.values().stream()
                .filter(component -> component.getClass().isAnnotationPresent(ListenerComponent.class))
                .forEach(listener -> registerListener((Listener) listener));
    }

    private void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
    }

    private void registerCommands() {
        this.components.values().stream()
                .filter(component -> component.getClass().isAnnotationPresent(CommandComponent.class))
                .forEach(this::handleCommandObject);
    }

    public void handleCommandObject(Object command) {
        if (command instanceof CommandExecutor commandExecutor) {
            CommandComponent annotation = command.getClass().getAnnotation(CommandComponent.class);
            String commandName = annotation.name();

            if (commandName.isEmpty()) {
                throw new NullPointerException("The 'name' attribute in @CommandComponent cannot be empty");
            }

            registerCommand(commandName, commandExecutor);
        }
    }

    public void registerCommand(String name, CommandExecutor executor) {
        PluginCommand pluginCommand = this.plugin.getCommand(name);

        if (pluginCommand == null) {
            throw new NullPointerException("Command name is missing in plugin.yml for command: %s" + name);
        }

        pluginCommand.setExecutor(executor);
    }
}
