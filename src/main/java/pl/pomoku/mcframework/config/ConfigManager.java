package pl.pomoku.mcframework.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configurations = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
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

        for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String fileName = file.getName().replace(".yml", "");
                loadConfig(fileName);
            }
        }
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
