package pl.pomoku.mcframework.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager<T extends JavaPlugin & ConfigurablePlugin> {
    private final T plugin;
    private final Map<String, YamlConfiguration> configurations = new HashMap<>();

    public ConfigManager(T plugin) {
        this.plugin = plugin;
        this.loadAllConfigs();
    }

    public void loadAllConfigs() {
        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            boolean successfullyCreated = dataFolder.mkdirs();
            if (!successfullyCreated) {
                throw new RuntimeException("Failed to create data folder");
            }
        }

        for (String fileName : plugin.getConfigFiles()) {
            loadConfig(fileName);
        }
    }

    public void loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        configurations.put(fileName.replace(".yml", ""), YamlConfiguration.loadConfiguration(file));
    }

    public YamlConfiguration getConfig(String fileName) {
        return this.configurations.get(fileName);
    }

    public <V> V getConfigValue(String fileName, String path, Class<V> type) {
        if (!this.configurations.containsKey(fileName)) return null;
        return type.cast(this.configurations.get(fileName).getString(path));
    }
}
