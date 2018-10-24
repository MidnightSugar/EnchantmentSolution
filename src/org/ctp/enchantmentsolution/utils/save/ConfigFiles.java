package org.ctp.enchantmentsolution.utils.save;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.enchantments.DefaultEnchantments;
import org.ctp.enchantmentsolution.enchantments.mcmmo.Fishing;
import org.ctp.enchantmentsolution.enchantments.wrappers.CustomEnchantmentWrapper;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.ctp.enchantmentsolution.utils.config.SimpleConfig;
import org.ctp.enchantmentsolution.utils.config.SimpleConfigManager;

public class ConfigFiles {

	private static File MAGMA_WALKER_FILE, MAIN_FILE, FISHING_FILE, LANGUAGE_FILE, ENCHANTMENT_FILE;
	private static File DATA_FOLDER = EnchantmentSolution.PLUGIN.getDataFolder();
	private static SimpleConfig CONFIG, FISHING, MAGMA_WALKER, LANGUAGE, ENCHANTMENT;
	
	public static SimpleConfig getDefaultConfig() {
		return CONFIG;
	}
	
	public static SimpleConfig getFishingConfig() {
		return FISHING;
	}
	
	public static SimpleConfig getMagmaWalkerConfig() {
		return MAGMA_WALKER;
	}
	
	public static SimpleConfig getLanguageFile() {
		return LANGUAGE;
	}
	
	public static SimpleConfig getEnchantmentConfig() {
		return ENCHANTMENT;
	}

	public static void createConfigFiles() {
		File dataFolder = DATA_FOLDER;
		try {
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}
			File extras = new File(dataFolder + "/extras/");
			if (!extras.exists()) {
				extras.mkdirs();
			}
			magmaWalker();
			MAGMA_WALKER_FILE = new File(dataFolder + "/extras/magma-walker.yml");
			if (!MAGMA_WALKER_FILE.exists()) {
				MAGMA_WALKER_FILE.createNewFile();
			}
			YamlConfiguration.loadConfiguration(MAGMA_WALKER_FILE);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}
			defaultFile();
			mcMMOFishing();
			enchantmentFile();
			MAIN_FILE = new File(dataFolder + "/config.yml");
			YamlConfiguration.loadConfiguration(MAIN_FILE);
			FISHING_FILE = new File(dataFolder + "/extras/fishing.yml");
			YamlConfiguration.loadConfiguration(FISHING_FILE);
			ENCHANTMENT_FILE = new File(dataFolder + "/enchantments.yml");
			YamlConfiguration.loadConfiguration(ENCHANTMENT_FILE);
			if(CONFIG.getInt("level_divisor") <= 0) {
				CONFIG.set("level_divisor", 4);
			}
			CONFIG.saveConfig();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		loadLangFile(dataFolder, 0);
	}
	
	public static void reload() {
		File dataFolder = DATA_FOLDER;
		try {
			defaultFile();
			mcMMOFishing();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		loadLangFile(dataFolder, 0);
	}
	
	private static void loadLangFile(File dataFolder, int tries) {
		if(tries > 5) {
			ChatUtils.sendToConsole(Level.SEVERE, "Failed to load language file. Disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(EnchantmentSolution.PLUGIN);
			return;
		}
		try {
			String langFile = CONFIG.getString("language_file");
			LANGUAGE_FILE = new File(dataFolder + "/" + langFile);
			YamlConfiguration.loadConfiguration(LANGUAGE_FILE);
			
			language(langFile);
		} catch (final Exception e) {
			tries++;
			CONFIG.set("language_file", "language.yml");
			CONFIG.saveConfig();
			
			loadLangFile(dataFolder, tries);
		}
	}
	
	private static void defaultFile() {
		SimpleConfigManager manager = new SimpleConfigManager(EnchantmentSolution.PLUGIN);
		String[] header = { "Enchantment Solution",
				"Plugin by", "crashtheparty"};
		CONFIG = manager.getNewConfig("config.yml", header);
		
		CONFIG.addDefault("starter", (ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "Enchantment Solution" + ChatColor.DARK_GRAY + "]").replace(ChatColor.COLOR_CHAR, '&'), new String[] {"What to display in front of messages"});
		CONFIG.addDefault("max_enchantments", 0, new String[] {"Max enchantments on each item. 0 allows infinite"});
		CONFIG.addDefault("level_divisor", 4, new String[] {"Greater numbers allow more anvil uses"});
		CONFIG.addDefault("level_50_enchants", true, new String[] {"Allow enchantments up to level 50"});
		CONFIG.addDefault("custom_enchant_names", false, new String[] {"Change enchantment names in the", "enchantment config."});
		CONFIG.addDefault("chest_loot", true, new String[] {"Allow custom and/or high level enchants", "to spawn in chests"});
		CONFIG.addDefault("mob_loot", true, new String[] {"Allow custom and/or high level enchantments", "to spawn on mobs"});
		CONFIG.addDefault("fishing_loot", true, new String[] {"Allow custom and/or high level enchantments", "to appear while fishing"});
		CONFIG.addDefault("language_file", "language.yml", new String[] {"Allow custom and/or high level enchantments", "to appear while fishing"});
		
		CONFIG.saveConfig();
		
		EnchantmentSolution.PLUGIN.getLogger().info("Default config initialized...");
	}
	
	private static void enchantmentFile() {
		SimpleConfigManager manager = new SimpleConfigManager(EnchantmentSolution.PLUGIN);
		String[] header = { "Enchantment Solution",
				"Plugin by", "crashtheparty"};
		ENCHANTMENT = manager.getNewConfig("enchantments.yml", header);
		
		for(CustomEnchantment enchant: DefaultEnchantments.getAddedEnchantments()) {
			if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				ENCHANTMENT.addDefault("custom_enchantments." + enchant.getName() + ".enabled", true);
				ENCHANTMENT.addDefault("custom_enchantments." + enchant.getName() + ".treasure", enchant.isTreasure());
				if(CONFIG.getBoolean("custom_enchant_names")) {
					ENCHANTMENT.addDefault("custom_enchantments." + enchant.getName() + ".display_name", enchant.getDisplayName());
				}
			}
		}
		ENCHANTMENT.saveConfig();
		
		EnchantmentSolution.PLUGIN.getLogger().info("Enchantment config initialized...");
	}
	
	public static void updateEnchantments() {
		if(CONFIG.getBoolean("custom_enchant_names")) {
			for(CustomEnchantment enchant: DefaultEnchantments.getEnchantments()) {
				if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
					enchant.setDisplayName(ENCHANTMENT.getString("custom_enchantments." + enchant.getName() + ".display_name"));
				}
			}
			for(CustomEnchantment enchant: DefaultEnchantments.getAddedEnchantments()) {
				if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
					enchant.setDisplayName(ENCHANTMENT.getString("custom_enchantments." + enchant.getName() + ".display_name"));
				}
			}
		}
	}
	
	private static void magmaWalker() {
		SimpleConfigManager manager = new SimpleConfigManager(EnchantmentSolution.PLUGIN);
		MAGMA_WALKER = manager.getNewConfig("extras/magma-walker.yml");
		
		MAGMA_WALKER.saveConfig();
		
		EnchantmentSolution.PLUGIN.getLogger().info("Magma Walker file initialized...");
	}
	
	private static void mcMMOFishing() {
		SimpleConfigManager manager = new SimpleConfigManager(EnchantmentSolution.PLUGIN);
		String[] header = { "Enchantment Solution",
				"Plugin by", "crashtheparty"};
		FISHING = manager.getNewConfig("extras/fishing.yml", header);
		
		FISHING.addDefault("Enchantments_Rarity_30.COMMON.enchants", Fishing.enchantmentDefaults("COMMON", false));
		FISHING.addDefault("Enchantments_Rarity_50.COMMON.enchants", Fishing.enchantmentDefaults("COMMON", true));
		FISHING.addDefault("Enchantments_Rarity_30.COMMON.multiple_enchants_chance", .10);
		FISHING.addDefault("Enchantments_Rarity_50.COMMON.multiple_enchants_chance", .10);
		FISHING.addDefault("Enchantments_Rarity_30.UNCOMMON.enchants", Fishing.enchantmentDefaults("UNCOMMON", false));
		FISHING.addDefault("Enchantments_Rarity_50.UNCOMMON.enchants", Fishing.enchantmentDefaults("UNCOMMON", true));
		FISHING.addDefault("Enchantments_Rarity_30.UNCOMMON.multiple_enchants_chance", .20);
		FISHING.addDefault("Enchantments_Rarity_50.UNCOMMON.multiple_enchants_chance", .18);
		FISHING.addDefault("Enchantments_Rarity_30.RARE.enchants", Fishing.enchantmentDefaults("RARE", false));
		FISHING.addDefault("Enchantments_Rarity_50.RARE.enchants", Fishing.enchantmentDefaults("RARE", true));
		FISHING.addDefault("Enchantments_Rarity_30.RARE.multiple_enchants_chance", .33);
		FISHING.addDefault("Enchantments_Rarity_50.RARE.multiple_enchants_chance", .28);
		FISHING.addDefault("Enchantments_Rarity_30.EPIC.enchants", Fishing.enchantmentDefaults("EPIC", false));
		FISHING.addDefault("Enchantments_Rarity_50.EPIC.enchants", Fishing.enchantmentDefaults("EPIC", true));
		FISHING.addDefault("Enchantments_Rarity_30.EPIC.multiple_enchants_chance", .50);
		FISHING.addDefault("Enchantments_Rarity_50.EPIC.multiple_enchants_chance", .42);
		FISHING.addDefault("Enchantments_Rarity_30.LEGENDARY.enchants", Fishing.enchantmentDefaults("LEGENDARY", false));
		FISHING.addDefault("Enchantments_Rarity_50.LEGENDARY.enchants", Fishing.enchantmentDefaults("LEGENDARY", true));
		FISHING.addDefault("Enchantments_Rarity_30.LEGENDARY.multiple_enchants_chance", .75);
		FISHING.addDefault("Enchantments_Rarity_50.LEGENDARY.multiple_enchants_chance", .60);
		FISHING.addDefault("Enchantments_Rarity_50.ANCIENT.enchants", Fishing.enchantmentDefaults("ANCIENT", true));
		FISHING.addDefault("Enchantments_Rarity_50.ANCIENT.multiple_enchants_chance", .85);
		
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_1.COMMON", 5.00);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_1.UNCOMMON", 1.00);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_1.RARE", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_1.EPIC", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_1.LEGENDARY", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_2.COMMON", 7.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_2.UNCOMMON", 1.00);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_2.RARE", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_2.EPIC", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_2.LEGENDARY", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_3.COMMON", 7.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_3.UNCOMMON", 2.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_3.RARE", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_3.EPIC", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_3.LEGENDARY", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_4.COMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_4.UNCOMMON", 2.75);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_4.RARE", 0.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_4.EPIC", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_4.LEGENDARY", 0.05);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_5.COMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_5.UNCOMMON", 4.00);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_5.RARE", 0.75);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_5.EPIC", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_5.LEGENDARY", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_6.COMMON", 9.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_6.UNCOMMON", 5.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_6.RARE", 1.75);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_6.EPIC", 0.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_6.LEGENDARY", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_7.COMMON", 8.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_7.UNCOMMON", 7.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_7.RARE", 2.75);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_7.EPIC", 0.75);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_7.LEGENDARY", 0.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_8.COMMON", 7.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_8.UNCOMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_8.RARE", 5.25);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_8.EPIC", 1.50);
		FISHING.addDefault("Enchantment_Drop_Rates_30.Tier_8.LEGENDARY", 0.75);
		
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.COMMON", 5.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.UNCOMMON", 1.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.RARE", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.EPIC", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.LEGENDARY", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_1.ANCIENT", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.COMMON", 8.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.UNCOMMON", 1.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.RARE", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.EPIC", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.LEGENDARY", 0.02);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_2.ANCIENT", 0.01);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.COMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.UNCOMMON", 2.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.RARE", 0.75);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.EPIC", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.LEGENDARY", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_3.ANCIENT", 0.05);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.COMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.UNCOMMON", 3.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.RARE", 1.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.EPIC", 0.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.LEGENDARY", 0.15);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_4.ANCIENT", 0.05);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.COMMON", 9.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.UNCOMMON", 5.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.RARE", 2.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.EPIC", 0.75);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.LEGENDARY", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_5.ANCIENT", 0.10);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.COMMON", 6.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.UNCOMMON", 8.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.RARE", 3.75);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.EPIC", 1.75);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.LEGENDARY", 0.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_6.ANCIENT", 0.15);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.COMMON", 5.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.UNCOMMON", 10.0);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.RARE", 4.75);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.EPIC", 2.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.LEGENDARY", 1.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_7.ANCIENT", 0.25);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.COMMON", 4.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.UNCOMMON", 8.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.RARE", 8.00);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.EPIC", 3.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.LEGENDARY", 1.50);
		FISHING.addDefault("Enchantment_Drop_Rates_50.Tier_8.ANCIENT", 0.50);
		
		FISHING.saveConfig();
		
		EnchantmentSolution.PLUGIN.getLogger().info("Fishing config initialized...");
	}
	
	private static void language(String fileName) {
		SimpleConfigManager manager = new SimpleConfigManager(EnchantmentSolution.PLUGIN);
		LANGUAGE = manager.getNewConfig(fileName);
		
		LANGUAGE.addDefault("anvil.name", (ChatColor.BLUE + "Anvil").replace("�", "&"));
		LANGUAGE.addDefault("anvil.mirror", (ChatColor.WHITE + "").replace("�", "&"));
		LANGUAGE.addDefault("anvil.rename", (ChatColor.GREEN + "Rename Items").replace("�", "&"));
		LANGUAGE.addDefault("anvil.repair-cost", (ChatColor.GREEN + "Level Cost: " + ChatColor.BLUE + "%repairCost%").replace("�", "&"));
		LANGUAGE.addDefault("anvil.repair-cost-high", (ChatColor.RED + "Level Cost: " + ChatColor.BLUE + "%repairCost%").replace("�", "&"));
		LANGUAGE.addDefault("anvil.cannot-repair", (ChatColor.RED + "Level Cost: " + ChatColor.BLUE + "Cannot Repair This Item").replace("�", "&"));
		LANGUAGE.addDefault("anvil.combine", (ChatColor.GREEN + "Combine Items").replace("�", "&"));
		LANGUAGE.addDefault("anvil.cannot-combine", (ChatColor.RED + "Can't Combine Items").replace("�", "&"));
		LANGUAGE.addDefault("anvil.cannot-rename", (ChatColor.RED + "Can't Rename Items").replace("�", "&"));
		LANGUAGE.addDefault("anvil.message-cannot-combine", "You may not combine these items!");
		
		LANGUAGE.addDefault("table.name", (ChatColor.BLUE + "Enchantment Table").replace("�", "&"));
		LANGUAGE.addDefault("table.black-mirror", (ChatColor.WHITE + "").replace("�", "&"));
		LANGUAGE.addDefault("table.red-mirror", (ChatColor.WHITE + "").replace("�", "&"));
		LANGUAGE.addDefault("table.instructions-title", ("Enchantment Instructions.").replace("�", "&"));
		LANGUAGE.addDefault("table.instructions", Arrays.asList(
				"Click items to put them on the left.",
				"You will see a list of books with the level",
				" and lapis needed to enchant.", "Select a book to enchant.",
				"Select the item again to remove.",
				"You may see up to 4 items at a time."));
		LANGUAGE.addDefault("table.generate-enchants-error", ("There was an error generating enchantments.").replace("�", "&"));
		LANGUAGE.addDefault("table.enchant-level", ("Level %level% Enchant.").replace("�", "&"));
		LANGUAGE.addDefault("table.enchant-level-lore", Arrays.asList(
				"Lvl Req: %levelReq%.", "Lvl Cost: %level%."));
		LANGUAGE.addDefault("table.level-fifty-disabled", ("Level 50 enchantments disabled.").replace("�", "&"));
		LANGUAGE.addDefault("table.level-fifty-lack", ("Requires 15 bookshelves around table.").replace("�", "&"));
		LANGUAGE.addDefault("table.lapis-cost-okay", (ChatColor.GREEN + "Lapis Cost: %cost%").replace("�", "&"));
		LANGUAGE.addDefault("table.lapis-cost-lack", (ChatColor.RED + "Lapis Cost: %cost%").replace("�", "&"));
		LANGUAGE.addDefault("table.level-cost-okay", (ChatColor.GREEN + "Level Req: %levelReq%").replace("�", "&"));
		LANGUAGE.addDefault("table.level-cost-lack", (ChatColor.RED + "Level Req: %levelReq%").replace("�", "&"));
		LANGUAGE.addDefault("table.item-enchant-name", ("%name% Level %level% Enchants").replace("�", "&"));
		LANGUAGE.addDefault("table.enchant-name", ("%enchant%...").replace("�", "&"));
		LANGUAGE.addDefault("table.lack-reqs", ("You do not meet the requirements to enchant this item.").replace("�", "&"));
		LANGUAGE.addDefault("table.lack-enchants", ("This item does not have enchantments generated.").replace("�", "&"));
		
		LANGUAGE.addDefault("commands.no-permission", (ChatColor.RED + "You do not have permission to use this command!").replace("�", "&"));
		LANGUAGE.addDefault("commands.invalid-level", ("%level% is not a valid level. Setting level to 1.").replace("�", "&"));
		LANGUAGE.addDefault("commands.level-too-low", ("Cannot set a negative or 0 level. Setting level to 1.").replace("�", "&"));
		LANGUAGE.addDefault("commands.level-too-high", ("%level% is too high of a level. Setting level to %maxLevel%.").replace("�", "&"));
		LANGUAGE.addDefault("commands.add-enchant", ("Enchantment with name %enchant% with level %level% has been added to the item.").replace("�", "&"));
		LANGUAGE.addDefault("commands.cannot-enchant-item", ("Enchantment does not work with this item.").replace("�", "&"));
		LANGUAGE.addDefault("commands.too-many-enchants", ("This item has too many enchantments already.").replace("�", "&"));
		LANGUAGE.addDefault("commands.enchant-fail", ("You must try to enchant an item.").replace("�", "&"));
		LANGUAGE.addDefault("commands.enchant-not-found", ("Enchantment with name %enchant% not found.").replace("�", "&"));
		LANGUAGE.addDefault("commands.enchant-not-specified", ("You must specify an enchantment.").replace("�", "&"));
		LANGUAGE.addDefault("commands.enchant-removed", ("Enchantment with name %enchant% has been removed from the item.").replace("�", "&"));
		LANGUAGE.addDefault("commands.enchant-remove-from-item", ("You must specify an enchantment.").replace("�", "&"));
		LANGUAGE.addDefault("commands.reload", ("Config files have been reloaded. Please note that the enchantments.yml file requires a server restart to take effect.").replace("�", "&"));

		LANGUAGE.addDefault("items.stole-soulbound", ("You have stolen the player's soulbound items!").replace("�", "&"));
		LANGUAGE.addDefault("items.soulbound-stolen", ("Your soulbound items have been stolen!").replace("�", "&"));
		
		LANGUAGE.saveConfig();
		
		EnchantmentSolution.PLUGIN.getLogger().info("Language file initialized...");
	}
}