package org.ctp.enchantmentsolution.nms.animalmob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.utils.items.ItemSerialization;
import org.ctp.enchantmentsolution.utils.yaml.YamlConfig;

public class AnimalMob_v1_15_R1 extends AnimalMob {

	private Type catType;
	private Gene pandaHiddenGene, pandaMainGene;
	private DyeColor collarColor;

	public AnimalMob_v1_15_R1(Animals mob, ItemStack item) {
		super(mob, item);
		if (mob instanceof Cat) {
			Cat cat = (Cat) mob;
			setCatType(cat.getCatType());
			setCollarColor(cat.getCollarColor());
		}
		if (mob instanceof Panda) {
			Panda panda = (Panda) mob;
			setPandaHiddenGene(panda.getHiddenGene());
			setPandaMainGene(panda.getMainGene());
		}
	}

	public Type getCatType() {
		return catType;
	}

	public void setCatType(Type catType) {
		this.catType = catType;
	}

	public Gene getPandaHiddenGene() {
		return pandaHiddenGene;
	}

	public void setPandaHiddenGene(Gene pandaHiddenGene) {
		this.pandaHiddenGene = pandaHiddenGene;
	}

	public Gene getPandaMainGene() {
		return pandaMainGene;
	}

	public void setPandaMainGene(Gene pandaMainGene) {
		this.pandaMainGene = pandaMainGene;
	}

	public void editProperties(Entity e) {
		super.editProperties(e);
		try {
			if (e instanceof Cat) {
				Cat cat = (Cat) e;
				cat.setCatType(getCatType());
				cat.setCollarColor(getCollarColor());
			}
			if (e instanceof Panda) {
				Panda panda = (Panda) e;
				panda.setHiddenGene(getPandaHiddenGene());
				panda.setMainGene(getPandaMainGene());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setConfig(YamlConfig config, int i) {
		super.setConfig(config, i);

		config.set("animals." + i + ".cat_type", getCatType() != null ? getCatType().name() : null);
		config.set("animals." + i + ".collar_color", getCatType() != null ? getCollarColor().name() : null);
		config.set("animals." + i + ".panda_main_gene", getPandaMainGene() != null ? getPandaMainGene().name() : null);
		config.set("animals." + i + ".panda_hidden_gene", getPandaHiddenGene() != null ? getPandaHiddenGene().name() : null);
	}

	public static AnimalMob createFromConfig(YamlConfig config, int i) {
		AnimalMob mob = new AnimalMob();

		mob.setName(config.getString("animals." + i + ".name"));
		mob.setAge(config.getInt("animals." + i + ".age"));
		mob.setHealth(config.getDouble("animals." + i + ".health"));
		mob.setEntityID(config.getInt("animals." + i + ".entity_id"), false);
		mob.setOwner(config.getString("animals." + i + ".owner"));
		mob.setDomestication(config.getInt("animals." + i + ".domestication"));
		mob.setMaxDomestication(config.getInt("animals." + i + ".max_domestication"));
		mob.setJumpStrength(config.getDouble("animals." + i + ".jump_strength"));
		mob.setMovementSpeed(config.getDouble("animals." + i + ".movement_speed"));
		mob.setMaxHealth(config.getDouble("animals." + i + ".max_health"));
		mob.setCarryingChest(config.getBoolean("animals." + i + ".carrying_chest"));
		mob.setLlamaStrength(config.getInt("animals." + i + ".llama_strength"));
		mob.setPigSaddle(config.getBoolean("animals." + i + ".pig_saddle"));
		mob.setSheared(config.getBoolean("animals." + i + ".sheared"));

		try {
			mob.setMob(EntityType.valueOf(config.getString("animals." + i + ".entity_type")));
		} catch (Exception ex) {}
		try {
			mob.setSheepColor(DyeColor.valueOf(config.getString("animals." + i + ".sheep_color")));
		} catch (Exception ex) {}
		try {
			mob.setWolfCollar(DyeColor.valueOf(config.getString("animals." + i + ".wolf_collar")));
		} catch (Exception ex) {}
		try {
			mob.setHorseStyle(Style.valueOf(config.getString("animals." + i + ".horse_style")));
		} catch (Exception ex) {}
		try {
			mob.setHorseColor(Color.valueOf(config.getString("animals." + i + ".horse_color")));
		} catch (Exception ex) {}
		try {
			mob.setLlamaColor(org.bukkit.entity.Llama.Color.valueOf(config.getString("animals." + i + ".llama_color")));
		} catch (Exception ex) {}
		try {
			mob.setParrotVariant(Variant.valueOf(config.getString("animals." + i + ".parrot_variant")));
		} catch (Exception ex) {}
		try {
			((AnimalMob_v1_15_R1) mob).setCatType(Type.valueOf(config.getString("animals." + i + ".cat_type")));
		} catch (Exception ex) {}
		try {
			((AnimalMob_v1_15_R1) mob).setPandaMainGene(Gene.valueOf(config.getString("animals." + i + ".panda_main_gene")));
		} catch (Exception ex) {}
		try {
			((AnimalMob_v1_15_R1) mob).setPandaHiddenGene(Gene.valueOf(config.getString("animals." + i + ".panda_hidden_gene")));
		} catch (Exception ex) {}
		try {
			((AnimalMob_v1_15_R1) mob).setCollarColor(DyeColor.valueOf(config.getString("animals." + i + ".collar_color")));
		} catch (Exception ex) {}
		try {
			mob.setRabbitType(org.bukkit.entity.Rabbit.Type.valueOf(config.getString("animals." + i + ".rabbit_type")));
		} catch (Exception ex) {}
		try {
			mob.setSaddle(ItemSerialization.stringToItem(config.getString("animals." + i + ".saddle")));
		} catch (Exception ex) {}
		try {
			mob.setArmor(ItemSerialization.stringToItem(config.getString("animals." + i + ".armor")));
		} catch (Exception ex) {}

		Map<Integer, ItemStack> inventoryItems = new HashMap<Integer, ItemStack>();
		List<String> inventoryKeys = config.getConfigurationInfo("animals." + i + ".inventory_items");
		for(String key: inventoryKeys) {
			String keyNum = key.substring(key.lastIndexOf('.') + 1);
			try {
				int num = Integer.parseInt(keyNum);
				inventoryItems.put(num, ItemSerialization.stringToItem(config.getString(key)));
				config.removeKey(key);
			} catch (Exception ex) {}
		}
		mob.setInventoryItems(inventoryItems);

		for(String key: config.getConfigurationInfo("animals." + i))
			config.removeKey(key);
		EnchantmentSolution.addAnimals(mob);
		return mob;
	}

	public DyeColor getCollarColor() {
		return collarColor;
	}

	public void setCollarColor(DyeColor collarColor) {
		this.collarColor = collarColor;
	}
}
