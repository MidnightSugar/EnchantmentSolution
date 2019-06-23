package org.ctp.enchantmentsolution.utils.save;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.advancements.ESAdvancement;
import org.ctp.enchantmentsolution.api.ApiEnchantmentWrapper;
import org.ctp.enchantmentsolution.api.Language;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.enchantments.DefaultEnchantments;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.enchantments.helper.PlayerLevels;
import org.ctp.enchantmentsolution.enchantments.helper.Weight;
import org.ctp.enchantmentsolution.enchantments.mcmmo.Fishing;
import org.ctp.enchantmentsolution.enchantments.wrappers.CustomEnchantmentWrapper;
import org.ctp.enchantmentsolution.utils.AdvancementUtils;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.ctp.enchantmentsolution.utils.config.YamlConfig;
import org.ctp.enchantmentsolution.utils.config.YamlConfigBackup;
import org.ctp.enchantmentsolution.utils.config.YamlInfo;
import org.ctp.enchantmentsolution.utils.items.ItemUtils;

public class ConfigFiles {

	private File abilityFile, mainFile, fishingFile, enchantmentFile, enchantmentAdvancedFile;
	private File dataFolder;
	private YamlConfig abilityConfig;
	private YamlConfigBackup config, fishing, enchantment, enchantmentAdvanced;
	private LanguageFiles languageFiles;
	private List<String> enchantingTypes = 
			Arrays.asList("vanilla_30", "vanilla_30_custom", "enhanced_30", "enhanced_30_custom", "enhanced_50", "enhanced_50_custom");

	public ConfigFiles(EnchantmentSolution plugin) {
		dataFolder = plugin.getDataFolder();
	}

	public YamlConfigBackup getDefaultConfig() {
		return config;
	}

	public YamlConfigBackup getFishingConfig() {
		return fishing;
	}

	public YamlConfig getAbilityConfig() {
		return abilityConfig;
	}

	public YamlConfigBackup getLanguageFile() {
		return languageFiles.getLanguageConfig();
	}

	public YamlConfigBackup getEnchantmentConfig() {
		return enchantment;
	}

	public YamlConfigBackup getEnchantmentAdvancedConfig() {
		return enchantmentAdvanced;
	}

	public void createConfigFiles() {
		try {
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}
			File extras = new File(dataFolder + "/extras/");
			if (!extras.exists()) {
				extras.mkdirs();
			}
			abilityFile = new File(dataFolder + "/extras/ability-enchantments.yml");
			if (!abilityFile.exists()) {
				abilityFile.createNewFile();
			}
			YamlConfiguration.loadConfiguration(abilityFile);
			abilityConfig();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		try {
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}
			mainFile = new File(dataFolder + "/config.yml");
			YamlConfiguration.loadConfiguration(mainFile);
			fishingFile = new File(dataFolder + "/extras/fishing.yml");
			YamlConfiguration.loadConfiguration(fishingFile);
			enchantmentFile = new File(dataFolder + "/enchantments.yml");
			YamlConfiguration.loadConfiguration(enchantmentFile);
			enchantmentAdvancedFile = new File(dataFolder + "/enchantments_advanced.yml");
			YamlConfiguration.loadConfiguration(enchantmentAdvancedFile);
			defaultFile();
			mcMMOFishing();
			enchantmentFile();
			enchantmentAdvancedFile();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		loadLangFile(dataFolder);
		save();
	}

	public void revert() {
		config.revert();
		fishing.revert();
		getLanguageFile().revert();
		enchantment.revert();
		enchantmentAdvanced.revert();
	}

	public void revert(YamlConfigBackup config, int backup) {
		config.revert();
		List<YamlInfo> info = EnchantmentSolution.getPlugin().getDb().getBackup(config, backup);
		for(YamlInfo i: info) {
			if (i.getValue() != null) {
				config.set(i.getPath(), i.getValue());
			}
		}

		save();
	}

	public void save() {
		getDefaultConfig().setComments(getDefaultConfig().getBoolean("use_comments"));
		getFishingConfig().setComments(getDefaultConfig().getBoolean("use_comments"));
		getLanguageFile().setComments(getDefaultConfig().getBoolean("use_comments"));
		getEnchantmentConfig().setComments(getDefaultConfig().getBoolean("use_comments"));
		getEnchantmentAdvancedConfig().setComments(getDefaultConfig().getBoolean("use_comments"));

		updateEnchantments();
		
		getDefaultConfig().saveConfig();
		getFishingConfig().saveConfig();
		getEnchantmentConfig().saveConfig();
		getEnchantmentAdvancedConfig().saveConfig();
		
		PlayerLevels.resetPlayerLevels();
		DefaultEnchantments.setEnchantments();
		loadLangFile(dataFolder);

		EnchantmentSolution.getPlugin().getDb().updateConfig(getDefaultConfig());
		EnchantmentSolution.getPlugin().getDb().updateConfig(getFishingConfig());
		EnchantmentSolution.getPlugin().getDb().updateConfig(getLanguageFile());
		EnchantmentSolution.getPlugin().getDb().updateConfig(getEnchantmentConfig());
		EnchantmentSolution.getPlugin().getDb().updateConfig(getEnchantmentAdvancedConfig());
		
		if(!EnchantmentSolution.getPlugin().isInitializing()) {
			EnchantmentSolution.getPlugin().setVersionCheck(config.getBoolean("version.get_latest"), config.getBoolean("version.get_experimental"));
			AdvancementUtils.createAdvancements();
		}
	}

	public void reload() {
		try {
			defaultFile();
			mcMMOFishing();
			enchantmentFile();
			enchantmentAdvancedFile();
			String setType = config.getString("enchanting_table.enchanting_type");
			if(!enchantingTypes.contains(setType)) {
				config.set("enchanting_table.enchanting_type", "enhanced_50");
			}
			config.saveConfig();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		save();
	}

	private void loadLangFile(File dataFolder) {
		String langFile = config.getString("language_file");
		if (languageFiles == null) {
			languageFiles = new LanguageFiles(new File(dataFolder + "/" + langFile), 
					Language.getLanguage(EnchantmentSolution.getPlugin().getConfigFiles().getDefaultConfig().getString("language")));
		} else {
			languageFiles.setLanguage(new File(dataFolder + "/" + langFile), 
					Language.getLanguage(EnchantmentSolution.getPlugin().getConfigFiles().getDefaultConfig().getString("language")));
		}
	}

	private void defaultFile() {
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Loading default config...");
		}

		String[] header = { "Enchantment Solution", "Plugin by", "crashtheparty" };
		config = new YamlConfigBackup(mainFile, header);

		config.getFromConfig();

		config.addDefault("starter",
				(ChatColor.DARK_GRAY + "[" + ChatColor.LIGHT_PURPLE + "Enchantment Solution" + ChatColor.DARK_GRAY
						+ "]").replace(ChatColor.COLOR_CHAR, '&'),
				new String[] { "What to display in front of messages" });
		config.addDefault("language_file", "language.yml", new String[] { "The yml language file" });
		config.addDefault("language", Language.US.getLocale(),
				new String[] { "The default language of the language file" });
		config.addEnum("language", Language.getValues());
		config.addDefault("reset_language", false, new String[] { "Reload the entire language file" });
		config.addDefault("max_enchantments", 0, new String[] { "Max enchantments on each item. 0 allows infinite" });
		config.addDefault("use_comments", true, new String[] { "Show helpful comments in the config files" });
		config.addDefault("version.get_latest", true, new String[] { "Check github for plugin releases (available on github and spigot)" });
		config.addDefault("version.get_experimental", false, new String[] { "Check github for plugin experimental versions (available only on github)" });
		config.addDefault("disable_enchant_method", "visible", new String[] {
				"How disabling an enchantment in enchantments.yml or enchantments_advanced.yml will work.", "Options:",
				"vanish - removes enchantment from items",
				"visible - keeps enchantment on item, but custom effects will not work and anvil will remove enchant",
				"repairable - same as above but anvil will not remove enchant" });
		config.addEnum("disable_enchant_method", Arrays.asList("vanish", "visible", "repairable"));
		
		config.addDefault("enchanting_table.enchanting_type", "enhanced_50", new String[] {
				"How enchanting works with the plugin", "Options:",
				"vanilla_30 - level 30 max, no higher level enchantments, use vanilla GUI, uses enchantments.yml",
				"vanilla_30_custom - level 30 max, no higher level enchantments, use vanilla GUI, uses enchantments_advanced.yml",
				"enhanced_30 - level 30 max, no higher level enchantments, use Enchantment Solution GUI, uses enchantments.yml",
				"enhanced_30_custom - level 30 max, no higher level enchantments, use Enchantment Solution GUI, uses enchantments_advanced.yml",
				"enhanced_50 - level 50 max, higher level enchantments, use Enchantment Solution GUI, uses enchantments.yml",
				"enhanced_50_custom - level 50 max, higher level enchantments, use Enchantment Solution GUI, uses enchantments_advanced.yml"
		});
		config.addEnum("enchanting_table.enchanting_type", enchantingTypes);
		config.addDefault("enchanting_table.lapis_in_table", true, new String[]{ 
				"Lapis must be placed in the enchantment table before items can be enchanted.", "Only used when enchanting type is enhanced."
		});
		config.addDefault("enchanting_table.reset_enchantments_advanced", false, new String[] { "Resets the enchantments_advanced.yml file." });
		config.addDefault("enchanting_table.use_enchanted_books", false, new String[] { 
				"Uses the vanilla Enchanted Books rather than Books to store enchantments." 
		});
		config.addDefault("enchanting_table.decay", false,
				new String[] { "Multiple enchantments generated on items will have lower levels" });
		config.addDefault("anvil.level_divisor", 4, new String[] { "Greater numbers allow more anvil uses." });
		config.addDefault("anvil.max_repair_level", 60, new String[] { "The highest repair level that will be allowed in the anvil", 
				"Only used when enchanting type is enhanced. "});
		config.addMinMax("anvil.max_repair_level", 40, 1000000);
		config.addDefault("anvil.default_use", false, new String[] {
				"Allow default use of anvil GUI via option at bottom right of custom GUI.", "Only used when enchanting type is enhanced.", 
				"Should only be true if anvil is used for custom recipes." });
		config.addDefault("protection_conflicts", true,
				new String[] { "All protection types conflict with each other" });
		if (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber() < 4) {
			config.addDefault("grindstone.use_legacy", false,
					new String[] { "Use the grindstone from within the anvil in version < 1.14" });
		}
		config.addDefault("grindstone.take_enchantments", false,
				new String[] { "Use the grindstone to add enchantments from items to books.", "Only used when enchanting type is enhanced." });
		config.addDefault("grindstone.set_repair_cost", true,
				new String[] { "When grindstone takes enchantments, set repair cost of the generated book to the item used's repair cost" });
		config.addDefault("grindstone.destroy_take_item", true,
				new String[] { "When grindstone takes enchantments, destroy the item used" });
		config.addDefault("update_legacy_enchantments", false,
				new String[] { "Update any enchantments generated in EnchantmentSolutionLegacy" });
		config.addDefault("chest_loot", true,
				new String[] { "Allow custom and/or high level enchants to spawn in chests" });
		config.addDefault("mob_loot", true,
				new String[] { "Allow custom and/or high level enchantments to spawn on mobs" });
		config.addDefault("fishing_loot", true,
				new String[] { "Allow custom and/or high level enchantments to appear while fishing" });
		config.addDefault("villager_trades", false,
				new String[] { "Allow custom and/or high level enchants to appear in villager trades" });
		config.addDefault("loots.mobs.bookshelves", 0,
				new String[] { "Modify types of enchantments generated by setting the minimum amount of bookshelves" });
		config.addDefault("loots.mobs.levels", 0,
				new String[] { "Modify types of enchantments generated by setting the minimum lapis level" });
		config.addDefault("loots.mobs.treasure", false, new String[] {
				"Whether the enchantments generated from this format should contain treasure enchantments" });
		config.addDefault("loots.fishing.bookshelves", 0);
		config.addDefault("loots.fishing.levels", 0);
		config.addDefault("loots.fishing.treasure", true);
		config.addDefault("loots.end_city_treasure.bookshelves", 15);
		config.addDefault("loots.end_city_treasure.levels", 3);
		config.addDefault("loots.end_city_treasure.treasure", true);
		config.addDefault("loots.simple_dungeon.bookshelves", 0);
		config.addDefault("loots.simple_dungeon.levels", 0);
		config.addDefault("loots.simple_dungeon.treasure", true);
		config.addDefault("loots.shipwreck_supply.bookshelves", 0);
		config.addDefault("loots.shipwreck_supply.levels", 0);
		config.addDefault("loots.shipwreck_supply.treasure", true);
		config.addDefault("loots.woodland_mansion.bookshelves", 10);
		config.addDefault("loots.woodland_mansion.levels", 1);
		config.addDefault("loots.woodland_mansion.treasure", true);
		config.addDefault("loots.stronghold_library.bookshelves", 10);
		config.addDefault("loots.stronghold_library.levels", 1);
		config.addDefault("loots.stronghold_library.treasure", true);
		config.addDefault("loots.stronghold_crossing.bookshelves", 10);
		config.addDefault("loots.stronghold_crossing.levels", 1);
		config.addDefault("loots.stronghold_crossing.treasure", true);
		config.addDefault("loots.stronghold_corridor.bookshelves", 10);
		config.addDefault("loots.stronghold_corridor.levels", 1);
		config.addDefault("loots.stronghold_corridor.treasure", true);
		config.addDefault("loots.underwater_ruin_big.bookshelves", 0);
		config.addDefault("loots.underwater_ruin_big.levels", 0);
		config.addDefault("loots.underwater_ruin_big.treasure", true);
		config.addDefault("loots.underwater_ruin_small.bookshelves", 0);
		config.addDefault("loots.underwater_ruin_small.levels", 0);
		config.addDefault("loots.underwater_ruin_small.treasure", true);
		if (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber() > 3) {
			config.addDefault("loots.pillager_outpost.bookshelves", 10);
			config.addDefault("loots.pillager_outpost.levels", 1);
			config.addDefault("loots.pillager_outpost.treasure", true);
		}

		for(ESAdvancement advancement : ESAdvancement.values()) {
			if(advancement == ESAdvancement.ENCHANTMENT_SOLUTION) {
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".enable", false);
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".toast", false);
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".announce", false);
			} else if(advancement.getActivatedVersion() < EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber()) {
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".enable", true);
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".toast", true);
				config.addDefault("advancements." + advancement.getNamespace().getKey() + ".announce", true);
			}
		}
		
		migrateDefaultFile();
		
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Default config initialized!");
		}
	}

	private void enchantmentFile() {
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Loading enchantment config...");
		}

		String[] header = { "Enchantment Solution", "Plugin by", "crashtheparty" };
		enchantment = new YamlConfigBackup(enchantmentFile, header);

		enchantment.getFromConfig();

		for(CustomEnchantment enchant: DefaultEnchantments.getEnchantments()) {
			if (enchant.getRelativeEnchantment() instanceof ApiEnchantmentWrapper) {
				JavaPlugin plugin = ((ApiEnchantmentWrapper) enchant.getRelativeEnchantment()).getPlugin();
				if (plugin == null) {
					ChatUtils.sendToConsole(Level.WARNING,
							"Enchantment " + enchant.getName() + " (Display Name " + enchant.getDisplayName() + ")"
									+ " does not have a JavaPlugin set. Refusing to set config defaults.");
					continue;
				}
				enchantment.addDefault(plugin.getName().toLowerCase() + "." + enchant.getName() + ".enabled", true);
				enchantment.addDefault(plugin.getName().toLowerCase() + "." + enchant.getName() + ".treasure",
						enchant.isTreasure());
			} else if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				enchantment.addDefault("custom_enchantments." + enchant.getName() + ".enabled", true);
				enchantment.addDefault("custom_enchantments." + enchant.getName() + ".treasure", enchant.isTreasure());
			}
		}
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Enchantment config initialized!");
		}
	}

	private void enchantmentAdvancedFile() {
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Loading advanced enchantment config...");
		}

		String[] header = { "Enchantment Solution", "Plugin by", "crashtheparty" };
		enchantmentAdvanced = new YamlConfigBackup(enchantmentAdvancedFile, header);

		if (!config.getBoolean("enchanting_table.reset_enchantments_advanced")) {
			enchantmentAdvanced.getFromConfig();
		}

		enchantmentAdvanced.addDefault("use_starting_level", true, new String[] {
				"Enchantments will not be available unless the enchanting level is the set value or above" });
		enchantmentAdvanced.addDefault("use_lapis_modifier", true,
				new String[] { "Enchanting with higher amounts of lapis give higher enchantability" });
		enchantmentAdvanced.addDefault("lapis_modifiers.constant", -1,
				new String[] { "Extra enchantability: (lapis + constant) * modifier" });
		enchantmentAdvanced.addDefault("lapis_modifiers.modifier", 2);
		enchantmentAdvanced.addDefault("multi_enchant_divisor", 75.0D,
				new String[] { "Chance of multiple enchantments on one item. Lower value = more enchantments." });
		enchantmentAdvanced.addDefault("use_permissions", false,
				new String[] { "Use the permission system per player for all enchantments.",
						"Permissions use the system \"enchantmentsolution.<enchant_name>.<type>.level<int>\"",
						"enchant_name: Enchantment name as used below",
						"type: either table (for enchanting items) or anvil (for combining items)",
						"int: the enchantment level", "Override permission: enchantmentsolution.permissions.ignore" });

		for(CustomEnchantment enchant: DefaultEnchantments.getEnchantments()) {
			if (enchant.getRelativeEnchantment() instanceof ApiEnchantmentWrapper) {
				JavaPlugin plugin = ((ApiEnchantmentWrapper) enchant.getRelativeEnchantment()).getPlugin();
				if (plugin == null) {
					ChatUtils.sendToConsole(Level.WARNING,
							"Enchantment " + enchant.getName() + " (Display Name " + enchant.getDisplayName() + ")"
									+ " does not have a JavaPlugin set. Refusing to set config defaults.");
					continue;
				}
				String namespace = plugin.getName().toLowerCase();
				enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".enabled",
						true);
				enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".treasure",
						enchant.isTreasure());
				enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".weight",
						enchant.getDefaultWeightName());
				enchantmentAdvanced.addEnum(namespace + "." + enchant.getName() + ".weight",
						Arrays.asList(Weight.VERY_RARE.getName(), Weight.RARE.getName(), Weight.UNCOMMON.getName(),
								Weight.COMMON.getName(), Weight.NULL.getName()));
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".enchantability_constant",
						enchant.getDefaultConstant());
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".enchantability_modifier",
						enchant.getDefaultModifier());
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".enchantability_start_level",
						enchant.getDefaultStartLevel());
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".enchantability_max_level",
						enchant.getDefaultMaxLevel());
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".conflicting_enchantments",
						enchant.conflictingDefaultList());
				enchantmentAdvanced.addEnum(
						namespace + "." + enchant.getName() + ".conflicting_enchantments",
						DefaultEnchantments.getEnchantmentNames());
				enchantmentAdvanced.addDefault(
						namespace + "." + enchant.getName() + ".disabled_items",
						enchant.getDisabledItemsStrings());
				enchantmentAdvanced.addEnum(
						namespace + "." + enchant.getName() + ".disabled_items",
						ItemUtils.getRepairMaterialsStrings());
			} else if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".enabled", true);
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".treasure",
						enchant.isTreasure());
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".weight",
						enchant.getDefaultWeightName());
				enchantmentAdvanced.addEnum("custom_enchantments." + enchant.getName() + ".weight",
						Arrays.asList(Weight.VERY_RARE.getName(), Weight.RARE.getName(), Weight.UNCOMMON.getName(),
								Weight.COMMON.getName(), Weight.NULL.getName()));
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".enchantability_constant",
						enchant.getDefaultConstant());
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".enchantability_modifier",
						enchant.getDefaultModifier());
				enchantmentAdvanced.addDefault(
						"custom_enchantments." + enchant.getName() + ".enchantability_start_level",
						enchant.getDefaultStartLevel());
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".enchantability_max_level",
						enchant.getDefaultMaxLevel());
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".conflicting_enchantments",
						enchant.conflictingDefaultList());
				enchantmentAdvanced.addEnum("custom_enchantments." + enchant.getName() + ".conflicting_enchantments",
						DefaultEnchantments.getEnchantmentNames());
				enchantmentAdvanced.addDefault("custom_enchantments." + enchant.getName() + ".disabled_items",
						enchant.getDisabledItemsStrings());
				enchantmentAdvanced.addEnum("custom_enchantments." + enchant.getName() + ".disabled_items",
						ItemUtils.getRepairMaterialsStrings());
			} else {
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".enabled", true);
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".treasure",
						enchant.isTreasure());
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".weight",
						enchant.getDefaultWeightName());
				enchantmentAdvanced.addEnum("default_enchantments." + enchant.getName() + ".weight",
						Arrays.asList(Weight.VERY_RARE.getName(), Weight.RARE.getName(), Weight.UNCOMMON.getName(),
								Weight.COMMON.getName(), Weight.NULL.getName()));
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".enchantability_constant",
						enchant.getDefaultConstant());
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".enchantability_modifier",
						enchant.getDefaultModifier());
				enchantmentAdvanced.addDefault(
						"default_enchantments." + enchant.getName() + ".enchantability_start_level",
						enchant.getDefaultStartLevel());
				enchantmentAdvanced.addDefault(
						"default_enchantments." + enchant.getName() + ".enchantability_max_level",
						enchant.getDefaultMaxLevel());
				enchantmentAdvanced.addDefault(
						"default_enchantments." + enchant.getName() + ".conflicting_enchantments",
						enchant.conflictingDefaultList());
				enchantmentAdvanced.addEnum("default_enchantments." + enchant.getName() + ".conflicting_enchantments",
						DefaultEnchantments.getEnchantmentNames());
				enchantmentAdvanced.addDefault("default_enchantments." + enchant.getName() + ".disabled_items",
						enchant.getDisabledItemsStrings());
				enchantmentAdvanced.addEnum("default_enchantments." + enchant.getName() + ".disabled_items",
						ItemUtils.getRepairMaterialsStrings());
			}
		}
		
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Advanced enchantment config initialized!");
		}
	}

	public void updateExternalEnchantments(JavaPlugin plugin) {
		for(CustomEnchantment enchant: DefaultEnchantments.getEnchantments()) {
			if (enchant.getRelativeEnchantment() instanceof ApiEnchantmentWrapper) {
				if (plugin.equals(((ApiEnchantmentWrapper) enchant.getRelativeEnchantment()).getPlugin())) {
					String namespace = plugin.getName().toLowerCase();
					enchantment.addDefault(namespace + "." + enchant.getName() + ".enabled", true);
					enchantment.addDefault(namespace + "." + enchant.getName() + ".treasure",
							enchant.isTreasure());
					enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".enabled", true);
					enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".treasure",
							enchant.isTreasure());
					enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".weight",
							enchant.getDefaultWeightName());
					enchantmentAdvanced.addEnum(namespace + "." + enchant.getName() + ".weight",
							Arrays.asList(Weight.VERY_RARE.getName(), Weight.RARE.getName(), Weight.UNCOMMON.getName(),
									Weight.COMMON.getName(), Weight.NULL.getName()));
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".enchantability_constant",
							enchant.getDefaultConstant());
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".enchantability_modifier",
							enchant.getDefaultModifier());
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".enchantability_start_level",
							enchant.getDefaultStartLevel());
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".enchantability_max_level",
							enchant.getDefaultMaxLevel());
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".conflicting_enchantments",
							enchant.conflictingDefaultList());
					enchantmentAdvanced.addEnum(
							namespace + "." + enchant.getName() + ".conflicting_enchantments",
							DefaultEnchantments.getEnchantmentNames());
					enchantmentAdvanced.addDefault(namespace + "." + enchant.getName() + ".disabled_items",
							enchant.getDisabledItemsStrings());
					enchantmentAdvanced.addEnum(namespace + "." + enchant.getName() + ".disabled_items",
							ItemUtils.getRepairMaterialsStrings());
				}
			}
		}

		enchantment.saveConfig();
		enchantmentAdvanced.saveConfig();
	}

	public void updateEnchantments() {
		for(CustomEnchantment enchant: DefaultEnchantments.getEnchantments()) {
			if (enchant.getRelativeEnchantment() instanceof ApiEnchantmentWrapper) {
				JavaPlugin plugin = ((ApiEnchantmentWrapper) enchant.getRelativeEnchantment()).getPlugin();
				if (plugin == null) {
					ChatUtils.sendToConsole(Level.WARNING,
							"Enchantment " + enchant.getName() + " (Display Name " + enchant.getDisplayName() + ")"
									+ " does not have a JavaPlugin set. Refusing to set config defaults.");
					continue;
				}
				String namespace = plugin.getName().toLowerCase();
				for(int i = 0; i < enchant.getMaxLevel(); i++) {
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".permissions.table.level" + (i + 1), false);
					enchantmentAdvanced.addDefault(
							namespace + "." + enchant.getName() + ".permissions.anvil.level" + (i + 1), false);
				}
				languageFiles.addDefault(
						"enchantment.display_names." + namespace + "." + enchant.getName(),
						enchant, "display_name");
				languageFiles.addDefault(
						"enchantment.descriptions." + namespace + "." + enchant.getName(), enchant,
						"description");
			} else if (enchant.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				String displayName = enchantmentAdvanced
						.getString("custom_enchantments." + enchant.getName() + ".display_name");
				if (displayName != null) {
					getLanguageFile().set("enchantment.descriptions.custom_enchantments." + enchant.getName(),
							displayName);
					enchant.setDisplayName(displayName);
					enchantmentAdvanced.removeKey("custom_enchantments." + enchant.getName() + ".display_name");
				}
				for(int i = 0; i < enchant.getMaxLevel(); i++) {
					enchantmentAdvanced.addDefault(
							"custom_enchantments." + enchant.getName() + ".permissions.table.level" + (i + 1), false);
					enchantmentAdvanced.addDefault(
							"custom_enchantments." + enchant.getName() + ".permissions.anvil.level" + (i + 1), false);
				}
				languageFiles.addDefault("enchantment.display_names.custom_enchantments." + enchant.getName(), enchant,
						"display_name");
				languageFiles.addDefault("enchantment.descriptions.custom_enchantments." + enchant.getName(), enchant,
						"description");
			} else {
				for(int i = 0; i < enchant.getMaxLevel(); i++) {
					enchantmentAdvanced.addDefault(
							"default_enchantments." + enchant.getName() + ".permissions.table.level" + (i + 1), false);
					enchantmentAdvanced.addDefault(
							"default_enchantments." + enchant.getName() + ".permissions.anvil.level" + (i + 1), false);
				}
				languageFiles.addDefault("enchantment.descriptions.default_enchantments." + enchant.getName(), enchant,
						"description");
			}
		}
	}

	private void abilityConfig() {
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Loading ability enchantment file...");
		}
		
		abilityConfig = new YamlConfig(abilityFile, new String[0]);

		abilityConfig.getFromConfig();

		abilityConfig.saveConfig();
		
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Ability enchantment file initialized!");
		}
	}

	private void mcMMOFishing() {
		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Loading fishing config...");
		}

		String[] header = { "Enchantment Solution", "Plugin by", "crashtheparty" };
		fishing = new YamlConfigBackup(fishingFile, header);

		fishing.getFromConfig();

		fishing.addDefault("Enchantments_Rarity_30.COMMON.enchants", Fishing.enchantmentDefaults("COMMON", false));
		fishing.addDefault("Enchantments_Rarity_50.COMMON.enchants", Fishing.enchantmentDefaults("COMMON", true));
		fishing.addDefault("Enchantments_Rarity_30.COMMON.multiple_enchants_chance", .10);
		fishing.addDefault("Enchantments_Rarity_50.COMMON.multiple_enchants_chance", .10);
		fishing.addDefault("Enchantments_Rarity_30.UNCOMMON.enchants", Fishing.enchantmentDefaults("UNCOMMON", false));
		fishing.addDefault("Enchantments_Rarity_50.UNCOMMON.enchants", Fishing.enchantmentDefaults("UNCOMMON", true));
		fishing.addDefault("Enchantments_Rarity_30.UNCOMMON.multiple_enchants_chance", .20);
		fishing.addDefault("Enchantments_Rarity_50.UNCOMMON.multiple_enchants_chance", .18);
		fishing.addDefault("Enchantments_Rarity_30.RARE.enchants", Fishing.enchantmentDefaults("RARE", false));
		fishing.addDefault("Enchantments_Rarity_50.RARE.enchants", Fishing.enchantmentDefaults("RARE", true));
		fishing.addDefault("Enchantments_Rarity_30.RARE.multiple_enchants_chance", .33);
		fishing.addDefault("Enchantments_Rarity_50.RARE.multiple_enchants_chance", .28);
		fishing.addDefault("Enchantments_Rarity_30.EPIC.enchants", Fishing.enchantmentDefaults("EPIC", false));
		fishing.addDefault("Enchantments_Rarity_50.EPIC.enchants", Fishing.enchantmentDefaults("EPIC", true));
		fishing.addDefault("Enchantments_Rarity_30.EPIC.multiple_enchants_chance", .50);
		fishing.addDefault("Enchantments_Rarity_50.EPIC.multiple_enchants_chance", .42);
		fishing.addDefault("Enchantments_Rarity_30.LEGENDARY.enchants",
				Fishing.enchantmentDefaults("LEGENDARY", false));
		fishing.addDefault("Enchantments_Rarity_50.LEGENDARY.enchants", Fishing.enchantmentDefaults("LEGENDARY", true));
		fishing.addDefault("Enchantments_Rarity_30.LEGENDARY.multiple_enchants_chance", .75);
		fishing.addDefault("Enchantments_Rarity_50.LEGENDARY.multiple_enchants_chance", .60);
		fishing.addDefault("Enchantments_Rarity_50.ANCIENT.enchants", Fishing.enchantmentDefaults("ANCIENT", true));
		fishing.addDefault("Enchantments_Rarity_50.ANCIENT.multiple_enchants_chance", .85);

		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_1.COMMON", 5.00);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_1.UNCOMMON", 1.00);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_1.RARE", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_1.EPIC", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_1.LEGENDARY", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_2.COMMON", 7.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_2.UNCOMMON", 1.00);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_2.RARE", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_2.EPIC", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_2.LEGENDARY", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_3.COMMON", 7.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_3.UNCOMMON", 2.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_3.RARE", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_3.EPIC", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_3.LEGENDARY", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_4.COMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_4.UNCOMMON", 2.75);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_4.RARE", 0.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_4.EPIC", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_4.LEGENDARY", 0.05);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_5.COMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_5.UNCOMMON", 4.00);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_5.RARE", 0.75);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_5.EPIC", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_5.LEGENDARY", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_6.COMMON", 9.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_6.UNCOMMON", 5.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_6.RARE", 1.75);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_6.EPIC", 0.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_6.LEGENDARY", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_7.COMMON", 8.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_7.UNCOMMON", 7.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_7.RARE", 2.75);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_7.EPIC", 0.75);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_7.LEGENDARY", 0.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_8.COMMON", 7.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_8.UNCOMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_8.RARE", 5.25);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_8.EPIC", 1.50);
		fishing.addDefault("Enchantment_Drop_Rates_30.Tier_8.LEGENDARY", 0.75);

		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.COMMON", 5.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.UNCOMMON", 1.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.RARE", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.EPIC", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.LEGENDARY", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_1.ANCIENT", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.COMMON", 8.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.UNCOMMON", 1.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.RARE", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.EPIC", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.LEGENDARY", 0.02);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_2.ANCIENT", 0.01);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.COMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.UNCOMMON", 2.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.RARE", 0.75);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.EPIC", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.LEGENDARY", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_3.ANCIENT", 0.05);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.COMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.UNCOMMON", 3.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.RARE", 1.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.EPIC", 0.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.LEGENDARY", 0.15);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_4.ANCIENT", 0.05);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.COMMON", 9.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.UNCOMMON", 5.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.RARE", 2.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.EPIC", 0.75);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.LEGENDARY", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_5.ANCIENT", 0.10);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.COMMON", 6.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.UNCOMMON", 8.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.RARE", 3.75);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.EPIC", 1.75);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.LEGENDARY", 0.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_6.ANCIENT", 0.15);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.COMMON", 5.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.UNCOMMON", 10.0);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.RARE", 4.75);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.EPIC", 2.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.LEGENDARY", 1.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_7.ANCIENT", 0.25);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.COMMON", 4.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.UNCOMMON", 8.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.RARE", 8.00);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.EPIC", 3.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.LEGENDARY", 1.50);
		fishing.addDefault("Enchantment_Drop_Rates_50.Tier_8.ANCIENT", 0.50);

		fishing.saveConfig();

		if(EnchantmentSolution.getPlugin().isInitializing()) {
			ChatUtils.sendInfo("Fishing config initialized!");
		}
	}
	
	public void generateDebug() {
		String[] header = { "Enchantment Solution", "Plugin by", "crashtheparty" };
		YamlConfig backup = new YamlConfig(new File(dataFolder + "/debug.yml"), header);

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z Z");
		backup.set("time", format.format(new Date()));
		backup.set("version.bukkit", EnchantmentSolution.getPlugin().getBukkitVersion().getVersion());
		backup.set("version.bukkit_num", EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber());
		backup.set("version.plugin", EnchantmentSolution.getPlugin().getPluginVersion().getCurrent());
		backup.set("plugins.jobs_reborn", EnchantmentSolution.getPlugin().isJobsEnabled());
		backup.set("plugins.mcmmo", EnchantmentSolution.getPlugin().getMcMMOType());
		backup.set("plugins.mcmmo_version", EnchantmentSolution.getPlugin().getMcMMOVersion());
		
		Iterator<Entry<PlayerLevels, List<Integer>>> iterator = PlayerLevels.PLAYER_LEVELS.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<PlayerLevels, List<Integer>> entry = iterator.next();
			List<List<EnchantmentLevel>> enchantmentLevels = entry.getKey().getEnchants();
			for(int i = 0; i < enchantmentLevels.size(); i++) {
				List<EnchantmentLevel> enchantments = enchantmentLevels.get(i);
				for(int j = 0; j < enchantments.size(); j++) {
					EnchantmentLevel enchantment = enchantments.get(j);
					backup.set("players.levels." + entry.getKey().getPlayer().getName() + ".item." + entry.getKey().getMaterial().name().toLowerCase()
							+ ".books." + entry.getKey().getBooks() + "." + i + "." + j, enchantment.getEnchant().getName() + " " + enchantment.getLevel());
				}
			}
		}
		
		for(String s : config.getAllEntryKeys()) {
			if(config.contains(s)) {
				backup.set("config." + s, config.get(s));
			}
		}
		
		for(String s : enchantment.getAllEntryKeys()) {
			if(enchantment.contains(s)) {
				backup.set("enchantment." + s, enchantment.get(s));
			}
		}
		
		for(String s : enchantmentAdvanced.getAllEntryKeys()) {
			if(enchantmentAdvanced.contains(s)) {
				backup.set("enchantmentAdvanced." + s, enchantmentAdvanced.get(s));
			}
		}
		
		for(String s : languageFiles.getLanguageConfig().getAllEntryKeys()) {
			if(languageFiles.getLanguageConfig().contains(s)) {
				backup.set("language." + s, languageFiles.getLanguageConfig().get(s));
			}
		}
		
		for(String s : fishing.getAllEntryKeys()) {
			if(fishing.contains(s)) {
				backup.set("fishing." + s, fishing.get(s));
			}
		}
		
		backup.saveConfig();
	}
	
	private void migrateDefaultFile() {
		if (config.getInt("anvil.level_divisor") <= 0) {
			config.set("anvil.level_divisor", 4);
		}
		if(config.getBoolean("level_50_enchants")) {
			if(config.getBoolean("use_advanced_file")) {
				config.set("enchanting_table.enchanting_type", "enhanced_50_custom");
			} else {
				config.set("enchanting_table.enchanting_type", "enhanced_50");
			}
			config.removeKey("level_50_enchants");
			config.removeKey("use_advanced_file");
		} else if (config.getBooleanValue("level_50_enchants") != null){
			if(config.getBoolean("use_advanced_file")) {
				config.set("enchanting_table.enchanting_type", "enhanced_30_custom");
			} else {
				config.set("enchanting_table.enchanting_type", "enhanced_30");
			}
			config.removeKey("level_50_enchants");
			config.removeKey("use_advanced_file");
		}
		if(config.getBooleanValue("lapis_in_table") != null) {
			config.set("enchanting_table.lapis_in_table", config.getBoolean("lapis_in_table"));
			config.removeKey("lapis_in_table");
		}
		if(config.getBooleanValue("use_enchanted_books") != null) {
			config.set("enchanting_table.use_enchanted_books", config.getBoolean("use_enchanted_books"));
			config.removeKey("use_enchanted_books");
		}
		if(config.getBooleanValue("enchantability_decay") != null) {
			config.set("enchanting_table.decay", config.getBoolean("enchantability_decay"));
			config.removeKey("enchantability_decay");
		}
		if(config.getInteger("max_repair_level") != null) {
			config.set("anvil.max_repair_level", config.getInt("max_repair_level"));
			config.removeKey("max_repair_level");
		}
		if(config.getBooleanValue("get_latest_version") != null) {
			config.set("version.get_latest", config.getBoolean("get_latest_version"));
			config.removeKey("get_latest_version");
		}
		String setType = config.getString("enchanting_table.enchanting_type");
		if(!enchantingTypes.contains(setType)) {
			config.set("enchanting_table.enchanting_type", "enhanced_50");
		}
		config.saveConfig();
	}
}
