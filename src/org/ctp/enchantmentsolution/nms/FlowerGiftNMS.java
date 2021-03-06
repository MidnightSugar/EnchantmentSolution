package org.ctp.enchantmentsolution.nms;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.nms.flowergift.FlowerGiftChance_v1_13;
import org.ctp.enchantmentsolution.nms.flowergift.FlowerGiftChance_v1_14;

public class FlowerGiftNMS {

	public static boolean isItem(Material material) {
		switch (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber()) {
			case 1:
			case 2:
			case 3:
				return FlowerGiftChance_v1_13.isItem(material);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				return FlowerGiftChance_v1_14.isItem(material);
		}
		return false;
	}

	public static ItemStack getItem(Material material) {
		switch (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber()) {
			case 1:
			case 2:
			case 3:
				return FlowerGiftChance_v1_13.getItem(material);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				return FlowerGiftChance_v1_14.getItem(material);
		}
		return null;
	}

	public static boolean isDoubleFlower(Material material) {
		switch (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber()) {
			case 1:
			case 2:
			case 3:
				return FlowerGiftChance_v1_13.isDoubleFlower(material);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				return FlowerGiftChance_v1_14.isDoubleFlower(material);
		}
		return false;
	}

	public static boolean isWitherRose(Material material) {
		switch (EnchantmentSolution.getPlugin().getBukkitVersion().getVersionNumber()) {
			case 1:
			case 2:
			case 3:
				return FlowerGiftChance_v1_13.isWitherRose(material);
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				return FlowerGiftChance_v1_14.isWitherRose(material);
		}
		return false;
	}
}
