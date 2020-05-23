package io.zentae.hammer;

import io.zentae.hammer.commandslistener.HCommandListener;
import io.zentae.hammer.events.HEventListener;
import io.zentae.hammer.utils.HUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class H extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");

    private final File config = new File(getDataFolder(), "config.yml");
    private YamlConfiguration configuration;

    private static H instance;

    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        HUtils.registerCraft();

        getCommand("hammer").setExecutor(new HCommandListener());
        getServer().getPluginManager().registerEvents(new HEventListener(), this);

        logger.info(String.format("[%s] Activated successfully ! Created by Zentae (Ver: %s)", getDescription().getName(), getDescription().getVersion()));
    }

    public void onDisable() {
        logger.info(String.format("[%s] DeActivated successfully ! Created by Zentae (Ver: %s)", getDescription().getName(), getDescription().getVersion()));
    }

    public void reloadConfig() { this.configuration = YamlConfiguration.loadConfiguration(config); }

    public YamlConfiguration getConfiguration() { return configuration; }
    public static H getInstance() { return instance; }
}
