package org.ctp.enchantmentsolution.listeners.fishing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.enchantments.EnchantmentLevel;
import org.ctp.enchantmentsolution.enchantments.Enchantments;
import org.ctp.enchantmentsolution.enchantments.mcmmo.Fishing;
import org.ctp.enchantmentsolution.utils.config.YamlConfig;
import org.ctp.enchantmentsolution.utils.save.ConfigFiles;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.skills.SecondaryAbility;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.XPGainReason;
import com.gmail.nossr50.events.skills.fishing.McMMOPlayerFishingTreasureEvent;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;

public class McMMOFishingListener implements Listener {
	
	private static List<McMMOFishingThread> PLAYER_ITEMS = new ArrayList<McMMOFishingThread>();

	@EventHandler
	public void onMcMMOPlayerFishingTreasure(McMMOPlayerFishingTreasureEvent event) {
		if(!Enchantments.getFishingLoot()) return;
		FishingManager manager = UserManager.getPlayer(event.getPlayer()).getFishingManager();
		int tier = manager.getLootTier();
		Player player = event.getPlayer();
		ItemStack treasure = event.getTreasure();

		if (Permissions.secondaryAbilityEnabled(player, SecondaryAbility.MAGIC_HUNTER)
				&& ItemUtils.isEnchantable(treasure)) {
			List<EnchantmentLevel> enchantments = getEnchants(player, treasure, tier);
			event.setCancelled(true);
			treasure = event.getTreasure();
			int treasureXp = event.getXp();

			if (treasure != null) {
				boolean enchanted = false;

				if (!enchantments.isEmpty()) {
					treasure = Enchantments.addEnchantmentsToItem(treasure, enchantments);
					enchanted = true;
				}

				if (enchanted) {
					player.sendMessage(LocaleLoader.getString("Fishing.Ability.TH.MagicFound"));
				}
				McMMOFishingThread thread = new McMMOFishingThread(player, treasure, treasureXp);
				int scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(EnchantmentSolution.PLUGIN, thread, 20l, 20l);
				thread.setScheduler(scheduler);
				PLAYER_ITEMS.add(thread);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {
		switch (event.getState()) {
		case CAUGHT_FISH:
			// TODO Update to new API once available! Waiting for case CAUGHT_TREASURE:
			Item fishingCatch = (Item) event.getCaught();
			McMMOFishingThread thread = null;
			for(McMMOFishingThread t : PLAYER_ITEMS) {
				if(t.getPlayer().equals(event.getPlayer())) {
					int fishXp = ExperienceConfig.getInstance().getXp(SkillType.FISHING, fishingCatch.getItemStack().getType());
					fishingCatch.setItemStack(t.getItem());
					thread = t;

					FishingManager manager = UserManager.getPlayer(event.getPlayer()).getFishingManager();
					manager.applyXpGain(t.getXp() + fishXp, XPGainReason.PVE);
				}
			}
			if(thread != null) {
				remove(thread);
			}
			return;
		default:
			return;
		}
	}
	
	public static void remove(McMMOFishingThread thread) {
		if(PLAYER_ITEMS.contains(thread)) {
			Bukkit.getScheduler().cancelTask(thread.getScheduler());
			PLAYER_ITEMS.remove(thread);
		}
	}

	private List<EnchantmentLevel> getEnchants(Player player, ItemStack treasure, int tier) {
		HashMap<String, Double> chanceMap = new HashMap<String, Double>();
		YamlConfig config = ConfigFiles.getFishingConfig();
		String location = ConfigFiles.useLevel50() ? "Enchantments_Rarity_50"
				: "Enchantments_Rarity_30";
		
		for (String s : config.getConfigurationInfo(location)) {
			String type = s.substring(s.lastIndexOf(".") + 1);
			chanceMap.put(type, Fishing.getTierChances(tier, type, ConfigFiles.useLevel50()));
		}

		double random = Math.random() * 100;
		for(Iterator<java.util.Map.Entry<String, Double>> it = chanceMap.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry<String, Double> e = it.next();
			random -= e.getValue();
			if (random <= 0) {
				return Fishing.getEnchantsFromConfig(player, treasure, e.getKey(), ConfigFiles.useLevel50());
			}
		}
		return new ArrayList<EnchantmentLevel>();
	}

}
