package org.ctp.enchantmentsolution.events.player;

import org.bukkit.entity.Player;
import org.ctp.enchantmentsolution.enchantments.CERegister;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.events.ESPlayerEvent;

public class IcarusRefreshEvent extends ESPlayerEvent {

	public IcarusRefreshEvent(Player who, int level) {
		super(who, new EnchantmentLevel(CERegister.ICARUS, level));
	}

}
