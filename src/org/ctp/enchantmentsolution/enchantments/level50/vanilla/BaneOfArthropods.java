package org.ctp.enchantmentsolution.enchantments.level50.vanilla;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.utils.ItemUtils;

public class BaneOfArthropods extends CustomEnchantment{

	@Override
	public boolean canEnchantItem(Material item) {
		if(item.equals(Material.BOOK)){
			return true;
		}
		if(ItemUtils.getItemTypes().get("swords").contains(item)){
			return true;
		}
		return false;
	}

	@Override
	public boolean canAnvilItem(Material item) {
		if(item.equals(Material.BOOK)){
			return true;
		}
		if(ItemUtils.getItemTypes().get("swords").contains(item)){
			return true;
		}
		if(ItemUtils.getItemTypes().get("axes").contains(item)){
			return true;
		}
		return false;
	}

	@Override
	public boolean conflictsWith(CustomEnchantment ench) {
		if(ench.getName().equalsIgnoreCase(getName())){
			return true;
		}
		if(ench.getName().equalsIgnoreCase("sharpness")){
			return true;
		}
		if(ench.getName().equalsIgnoreCase("smite")){
			return true;
		}
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 6;
	}

	@Override
	public String getName() {
		return "bane_of_arthropods";
	}
	
	@Override
	public String getDisplayName() {
		return "Bane of Arthropods";
	}

	@Override
	public int getStartLevel() {
		return 1;
	}

	@Override
	public int getWeight() {
		return 5;
	}

	@Override
	public int[] enchantability(int level) {
		int[] levels = new int[2];
		levels[0] = 9 * level - 4;
		levels[1] = levels[0] + 18;
		return levels;
	}

	@Override
	public Enchantment getRelativeEnchantment() {
		return Enchantment.DAMAGE_ARTHROPODS;
	}
	
	public int multiplier(Material material) {
		if(!(material.equals(Material.BOOK) || material.equals(Material.ENCHANTED_BOOK))) {
			return 2;
		}
		return 1;
	}
	
	@Override
	public String[] getPage() {
		String pageOne = "Name: " + getDisplayName() + StringUtils.LF + StringUtils.LF;
		pageOne += "Description: Increases damage to \"arthropod\" mobs (spiders, cave spiders, silverfish and endermites)." + 
				StringUtils.LF + 
				"Each level separately adds 2.5 (hearts × 1 1�?�4) extra damage to each hit, to \"arthropods\" only." + 
				StringUtils.LF + 
				"The enchantment will also cause \"arthropods\" to have the Slowness IV effect when hit." + StringUtils.LF;
		String pageTwo = "Max Level: " + getMaxLevel() + "."+ StringUtils.LF;
		pageTwo += "Weight: " + getWeight() + "."+ StringUtils.LF;
		pageTwo += "Start Level: " + getStartLevel() + "."+ StringUtils.LF;
		pageTwo += "Enchantable Items: Swords, Books." + StringUtils.LF;
		pageTwo += "Anvilable Items: Swords, Axes, Books." + StringUtils.LF;
		pageTwo += "Treasure Enchantment: " + isTreasure() + ". " + StringUtils.LF;
		return new String[] {pageOne, pageTwo};
	}

}