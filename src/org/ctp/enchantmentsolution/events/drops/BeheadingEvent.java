package org.ctp.enchantmentsolution.events.drops;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.enchantments.CERegister;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.events.player.DropEvent;

public class BeheadingEvent extends DropEvent {

	private boolean keepInventoryOverride;
	private final LivingEntity skullOwner;

	public BeheadingEvent(Player who, LivingEntity skullOwner, int level, List<ItemStack> newDrops, List<ItemStack> originalDrops,
	boolean override, boolean keepInventoryOverride) {
		super(who, new EnchantmentLevel(CERegister.BEHEADING, level), newDrops, originalDrops, override);
		setKeepInventoryOverride(keepInventoryOverride);
		this.skullOwner = skullOwner;
	}

	public boolean isKeepInventoryOverride() {
		return keepInventoryOverride;
	}

	public void setKeepInventoryOverride(boolean keepInventoryOverride) {
		this.keepInventoryOverride = keepInventoryOverride;
	}

	public LivingEntity getSkullOwner() {
		return skullOwner;
	}

}
