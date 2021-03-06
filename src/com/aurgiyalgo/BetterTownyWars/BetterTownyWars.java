package com.aurgiyalgo.BetterTownyWars;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.aurgiyalgo.BetterTownyWars.commands.BTWCommandExecutor;
import com.aurgiyalgo.BetterTownyWars.data.DataHandler;
import com.aurgiyalgo.BetterTownyWars.language.BTWLanguageHandler;
import com.aurgiyalgo.BetterTownyWars.listeners.BTWListener;
import com.aurgiyalgo.BetterTownyWars.listeners.BTWTabCompleter;
import com.aurgiyalgo.BetterTownyWars.wars.NationVsNationWar;
import com.aurgiyalgo.BetterTownyWars.wars.TownVsTownWar;
import com.aurgiyalgo.BetterTownyWars.wars.WarManager;
import com.aurgiyalgo.BetterTownyWars.wars.WarType;

public class BetterTownyWars extends JavaPlugin {

	private static BetterTownyWars instance;

	private WarManager warManager;
	private BTWListener listener;
	private DataHandler dataHandler;
	private BTWLanguageHandler languageHandler;

	private FileConfiguration languageFile;

	@Override
	public void onEnable() {
		instance = this;

		setupConfig();

		warManager = new WarManager();
		listener = new BTWListener();
		dataHandler = new DataHandler(getDataFolder());

		setupLanguage();

		WarType.addWarType("NATION_VS_NATION", "Nation VS Nation", NationVsNationWar.class);
		WarType.addWarType("TOWN_VS_TOWN", "Town VS Town", TownVsTownWar.class);

//		this._warManager.loadData();

		Bukkit.getPluginManager().registerEvents(listener, instance);
		getCommand("bettertownywars").setExecutor(new BTWCommandExecutor());
		getCommand("bettertownywars").setPermissionMessage(languageHandler.getMessage("no-permission"));
		getCommand("bettertownywars").setTabCompleter(new BTWTabCompleter());

		this.warManager.forcePvPInTownsAtWar();
		this.warManager.initTimeLimitCheck();

		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				warManager.loadData();
			}
		};
		runnable.runTaskLater(this, 1L);
	}

	@Override
	public void onDisable() {
		warManager.disableTimeLimitCheck();
		warManager.saveData();
		warManager.disablePvPInTownsAtWar();
		dataHandler.saveData();

		languageHandler = null;
		warManager = null;
		listener = null;
		dataHandler = null;
	}

	private void setupConfig() {
		getConfig().addDefault("language", String.valueOf("en_US"));
		getConfig().addDefault("max_war_duration", Long.valueOf(604800000L));
		getConfig().addDefault("nation_vs_nation_kill_percentage_to_finish", Double.valueOf(0.5));
		getConfig().addDefault("town_vs_town_kill_percentage_to_finish", Double.valueOf(0.5));
		getConfig().addDefault("lose_war_cost", Double.valueOf(100));
		getConfig().addDefault("war_ended_check_interval", Integer.valueOf(30));
		getConfig().addDefault("neutrality_cost", Double.valueOf(50));
		getConfig().addDefault("add_missing_messages_to_config", Boolean.valueOf(true));
		getConfig().addDefault("declare_war_cost", Double.valueOf(100));
		getConfig().options().copyDefaults(true);
		saveConfig();

		Configuration.LANGUAGE_FILE = getConfig().getString("language");
		Configuration.MAX_WAR_DURATION = getConfig().getLong("max_war_duration");
		Configuration.LOSE_WAR_COST = getConfig().getDouble("lose_war_cost");
		Configuration.NATION_VS_NATION_KILL_PERCENTAGE_TO_FINISH = getConfig().getDouble("nation_vs_nation_kill_percentage_to_finish");
		Configuration.TOWN_VS_TOWN_KILL_PERCENTAGE_TO_FINISH = getConfig().getDouble("town_vs_town_kill_percentage_to_finish");
		Configuration.WAR_ENDED_CHECK_INTERVAL = getConfig().getInt("war_ended_check_interval");
		Configuration.NEUTRALITY_COST = getConfig().getDouble("neutrality_cost");
		Configuration.ADD_MISSING_MESSAGES_TO_CONFIG = getConfig().getBoolean("add_missing_messages_to_config");
		Configuration.DECLARE_WAR_COST = getConfig().getDouble("declare_war_cost");
	}

	private void setupLanguage() {
		if (!new File(getDataFolder(), "en_US.yml").exists()) {
			saveResource("es_US.yml", false);
		}
		if (!new File(getDataFolder(), "it_IT.yml").exists()) {
			saveResource("it_IT.yml", false);
		}

		File file = new File(getDataFolder(), getConfig().getString("language") + ".yml");
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		languageFile = YamlConfiguration.loadConfiguration(file);
		languageHandler = new BTWLanguageHandler(languageFile, file);
	}

	public static class Configuration {
		public static String LANGUAGE_FILE = "en_US";
		public static double NATION_VS_NATION_KILL_PERCENTAGE_TO_FINISH = 0.5;
		public static double TOWN_VS_TOWN_KILL_PERCENTAGE_TO_FINISH = 0.5;
		public static double LOSE_WAR_COST = 100;
		public static long MAX_WAR_DURATION = 604800000L;
		public static int WAR_ENDED_CHECK_INTERVAL = 5;
		public static double NEUTRALITY_COST = 100;
		public static boolean ADD_MISSING_MESSAGES_TO_CONFIG = true;
		public static double DECLARE_WAR_COST = 100;
	}

	public WarManager getWarManager() {
		return warManager;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	public BTWLanguageHandler getLanguageHandler() {
		return languageHandler;
	}

	public static BetterTownyWars getInstance() {
		return instance;
	}

	public static class Permissions {
		public static final Permission NVN_DECLARE_PERMISSION = new Permission("btw.declare.nation_vs_nation");
		public static final Permission NVN_FINISH_PERMISSION = new Permission("btw.finish.nation_vs_nation");
		public static final Permission TVT_DECLARE_PERMISSION = new Permission("btw.declare.town_vs_town");
		public static final Permission TVT_FINISH_PERMISSION = new Permission("btw.finish.town_vs_town");
		public static final Permission TOGGLE_NEUTRALITY_PERMISSION = new Permission("btw.neutral");
	}

}
