package pl.pomoku.mcframework.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configurations = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            this.plugin.saveResource(fileName + ".yml", false);
        }
        this.configurations.put(fileName, YamlConfiguration.loadConfiguration(file));
    }

    public FileConfiguration getConfig(String fileName) {
        return this.configurations.get(fileName);
    }

    public String getConfigValue(String fileName, String path) {
        return this.configurations.containsKey(fileName) ? this.configurations.get(fileName).getString(path) : null;
    }
}
