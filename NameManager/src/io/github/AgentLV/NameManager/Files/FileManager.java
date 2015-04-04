package io.github.AgentLV.NameManager.Files;

import io.github.AgentLV.NameManager.NameManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager {
	
	static NameManager plugin;
	private static String configVersion = "1.1";
	
	public FileManager(NameManager plugin) {
		FileManager.plugin = plugin;
	}

	//config.yml
	
	public static FileConfiguration getFileConfiguration(String fileName) {
		
		File file = new File(plugin.getDataFolder(), fileName + ".yml");
        FileConfiguration fileConfiguration = new YamlConfiguration();
    	
        try {
            fileConfiguration.load(file);
            
            if (fileName == "config") {
	            String version = fileConfiguration.getString("version");
	
	            if (version != null && version.equals(configVersion)) {
	                return fileConfiguration;
	            }
	
	            if (version == null) {
	                version = "backup";
	            }
	
	            if (file.renameTo(new File(plugin.getDataFolder(), "old-" + fileName + "-" + version + ".yml"))) {
	            	plugin.getLogger().info("Found outdated config, creating backup...");
	                plugin.getLogger().info("Created a backup for: " + fileName + ".yml");
	            }
            } else {
            	return fileConfiguration;
            }
        } catch (IOException|InvalidConfigurationException e) {
            plugin.getLogger().info("Generating fresh configuration file: " + fileName + ".yml");
        }

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                InputStream in = plugin.getResource(fileName + ".yml");
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                out.close();
                in.close();
            }
            fileConfiguration.load(file);
        } catch(IOException|InvalidConfigurationException ex) {
            plugin.getLogger().severe("Plugin unable to write configuration file " + fileName + ".yml!");
            plugin.getLogger().severe("Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            ex.printStackTrace();
        }
        
        return fileConfiguration;
    }
	
	//Groups.yml
	
    private static List<String> allGroups = new ArrayList<>();
    public static File groupFile = new File("plugins/NameManager/Groups.yml");
    public static FileConfiguration groups = YamlConfiguration.loadConfiguration(groupFile);
    
    public static void loadFromFile() {
    	
    	FileConfiguration groupsFile = getFileConfiguration("Groups");
        allGroups.clear();
        allGroups = groupsFile.getStringList("GroupList");

        for (String s : allGroups) {
        	NameManager.team = NameManager.board.registerNewTeam(s);
        	NameManager.team.setPrefix(ChatColor.translateAlternateColorCodes('&', groupsFile.getString("Groups." + s + ".Prefix")));
            NameManager.team.setSuffix(ChatColor.translateAlternateColorCodes('&', groupsFile.getString("Groups." + s + ".Suffix")));
        }
    }
    
    public static void unloadFromFile() {
    	
    	FileConfiguration groupsFile = getFileConfiguration("Groups");
        allGroups = groupsFile.getStringList("GroupList");

        for (String s : allGroups) {
        	NameManager.team = NameManager.board.getTeam(s);
        	NameManager.team.unregister();
        }
    }
	
	
}