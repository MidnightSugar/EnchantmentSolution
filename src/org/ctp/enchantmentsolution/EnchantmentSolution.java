package org.ctp.enchantmentsolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ctp.enchantmentsolution.commands.Enchant;
import org.ctp.enchantmentsolution.commands.EnchantInfo;
import org.ctp.enchantmentsolution.commands.Reload;
import org.ctp.enchantmentsolution.commands.RemoveEnchant;
import org.ctp.enchantmentsolution.commands.UnsafeEnchant;
import org.ctp.enchantmentsolution.enchantments.DefaultEnchantments;
import org.ctp.enchantmentsolution.enchantments.EnchantmentLevel;
import org.ctp.enchantmentsolution.inventory.InventoryData;
import org.ctp.enchantmentsolution.listeners.InventoryClick;
import org.ctp.enchantmentsolution.listeners.InventoryClose;
import org.ctp.enchantmentsolution.listeners.PlayerChatTabComplete;
import org.ctp.enchantmentsolution.listeners.PlayerInteract;
import org.ctp.enchantmentsolution.listeners.abilities.BeheadingListener;
import org.ctp.enchantmentsolution.listeners.abilities.BrineListener;
import org.ctp.enchantmentsolution.listeners.abilities.DrownedListener;
import org.ctp.enchantmentsolution.listeners.abilities.ExpShareListener;
import org.ctp.enchantmentsolution.listeners.abilities.FishingListener;
import org.ctp.enchantmentsolution.listeners.abilities.FrequentFlyerListener;
import org.ctp.enchantmentsolution.listeners.abilities.KnockUpListener;
import org.ctp.enchantmentsolution.listeners.abilities.LifeListener;
import org.ctp.enchantmentsolution.listeners.abilities.MagmaWalkerListener;
import org.ctp.enchantmentsolution.listeners.abilities.SacrificeListener;
import org.ctp.enchantmentsolution.listeners.abilities.ShockAspectListener;
import org.ctp.enchantmentsolution.listeners.abilities.SmelteryListener;
import org.ctp.enchantmentsolution.listeners.abilities.SniperListener;
import org.ctp.enchantmentsolution.listeners.abilities.SoulboundListener;
import org.ctp.enchantmentsolution.listeners.abilities.TankListener;
import org.ctp.enchantmentsolution.listeners.abilities.TelepathyListener;
import org.ctp.enchantmentsolution.listeners.abilities.WarpListener;
import org.ctp.enchantmentsolution.listeners.chestloot.ChestLootListener;
import org.ctp.enchantmentsolution.listeners.fishing.EnchantsFishingListener;
import org.ctp.enchantmentsolution.listeners.fishing.McMMOFishingListener;
import org.ctp.enchantmentsolution.listeners.mobs.MobSpawning;
//import org.ctp.enchantmentsolution.listeners.legacy.UpdateEnchantments;
import org.ctp.enchantmentsolution.nms.Version;
import org.ctp.enchantmentsolution.utils.save.ConfigFiles;
import org.ctp.enchantmentsolution.utils.save.SaveUtils;

public class EnchantmentSolution extends JavaPlugin {

	public static EnchantmentSolution PLUGIN;
	public static List<InventoryData> INVENTORIES = new ArrayList<InventoryData>();
	public static HashMap<Material, HashMap<List<EnchantmentLevel>, Integer>> DEBUG = new HashMap<Material, HashMap<List<EnchantmentLevel>, Integer>>();

	public void onEnable() {
		PLUGIN = this;
		if(!Version.VERSION_ALLOWED) {
			Bukkit.getLogger().log(Level.WARNING, "Version " + Version.VERSION + " is not compatible with this plugin. Please use a version that is compatible.");
			Bukkit.getPluginManager().disablePlugin(PLUGIN);
			return;
		}
		
		ConfigFiles.createConfigFiles();

		DefaultEnchantments.addDefaultEnchantments();

		SaveUtils.getData();
		
		DefaultEnchantments.setEnchantments();

		getServer().getPluginManager().registerEvents(new PlayerInteract(),
				this);
		getServer().getPluginManager().registerEvents(new InventoryClick(),
				this);
		getServer().getPluginManager().registerEvents(new InventoryClose(),
				this);
		getServer().getPluginManager().registerEvents(new SoulboundListener(),
				this);
		getServer().getPluginManager().registerEvents(
				new ShockAspectListener(), this);
		getServer().getPluginManager().registerEvents(new WarpListener(), this);
		getServer().getPluginManager().registerEvents(new KnockUpListener(),
				this);
		getServer().getPluginManager().registerEvents(new BeheadingListener(),
				this);
		getServer().getPluginManager().registerEvents(new LifeListener(null),
				this);
		getServer().getPluginManager().registerEvents(new WarpListener(), this);
		getServer().getPluginManager().registerEvents(new ExpShareListener(),
				this);
		getServer().getPluginManager().registerEvents(
				new MagmaWalkerListener(), this);
		getServer().getPluginManager().registerEvents(new SniperListener(),
				this);
		getServer().getPluginManager().registerEvents(new TelepathyListener(),
				this);
		getServer().getPluginManager().registerEvents(new SmelteryListener(),
				this);
		getServer().getPluginManager().registerEvents(new SacrificeListener(),
				this);
		getServer().getPluginManager().registerEvents(new FishingListener(), this);
		getServer().getPluginManager().registerEvents(new FrequentFlyerListener(), this);
		getServer().getPluginManager().registerEvents(new TankListener(), this);
		getServer().getPluginManager().registerEvents(new BrineListener(), this);
		getServer().getPluginManager().registerEvents(new DrownedListener(), this);
		getServer().getPluginManager().registerEvents(new ChestLootListener(), this);
		getServer().getPluginManager().registerEvents(new MobSpawning(), this);
		if(Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
			getServer().getPluginManager().registerEvents(new McMMOFishingListener(), this);
		} else {
			getServer().getPluginManager().registerEvents(new EnchantsFishingListener(), this);
		}

		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
				new MagmaWalkerListener(), 20l, 20l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
				new FrequentFlyerListener(), 20l, 20l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
						new DrownedListener(), 1l, 1l);

		getCommand("Enchant").setExecutor(new Enchant());
		getCommand("Info").setExecutor(new EnchantInfo());
		getCommand("RemoveEnchant").setExecutor(new RemoveEnchant());
		getCommand("EnchantUnsafe").setExecutor(new UnsafeEnchant());
		getCommand("ESReload").setExecutor(new Reload());
		getCommand("Enchant").setTabCompleter(new PlayerChatTabComplete());
		getCommand("Info").setTabCompleter(new PlayerChatTabComplete());
		getCommand("RemoveEnchant").setTabCompleter(new PlayerChatTabComplete());
		getCommand("EnchantUnsafe").setTabCompleter(new PlayerChatTabComplete());
		
		ConfigFiles.updateEnchantments();
	}

	public void onDisable() {
		SaveUtils.setMagmaWalkerData();
		
		for(int i = INVENTORIES.size() - 1; i >= 0; i--) {
			InventoryData inv = INVENTORIES.get(i);
			inv.close(true);
		}
	}
	
	public static InventoryData getInventory(Player player) {
		for(InventoryData inv : INVENTORIES) {
			if(inv.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				return inv;
			}
		}
		
		return null;
	}
	
	public static void addInventory(InventoryData inv) {
		INVENTORIES.add(inv);
	}
	
	public static void removeInventory(InventoryData inv) {
		INVENTORIES.remove(inv);
	}
}