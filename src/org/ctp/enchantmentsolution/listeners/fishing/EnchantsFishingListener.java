package org.ctp.enchantmentsolution.listeners.fishing;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.utils.GenerateUtils;
import org.ctp.enchantmentsolution.utils.config.ConfigString;

public class EnchantsFishingListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFishMonitor(PlayerFishEvent event) {
		if (!ConfigString.USE_LOOT.getBoolean("fishing.use")) return;
		switch (event.getState()) {
			case CAUGHT_FISH:
				handleFishing((Item) event.getCaught(), event.getPlayer());
				return;
			default:
				return;
		}
	}

	private void handleFishing(Item item, Player player) {
		ItemStack itemStack = item.getItemStack().clone();
		itemStack = GenerateUtils.generateFishingLoot(player, itemStack, ConfigString.LOOT_BOOKSHELVES.getInt("fishing.bookshelves"), ConfigString.LOOT_TREASURE.getBoolean("fishing.treasure"));

		item.setItemStack(itemStack);
	}
}
