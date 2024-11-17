package pl.pomoku.mcframework.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager<T extends JavaPlugin & ConfigurablePlugin> {
    private final T plugin;
    private final Map<String, FileConfiguration> configurations = new HashMap<>();

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

    public FileConfiguration getConfig(String fileName) {
        return this.configurations.get(fileName);
    }

    public String getConfigValue(String fileName, String path) {
        return this.configurations.containsKey(fileName) ? this.configurations.get(fileName).getString(path) : null;
    }
}
