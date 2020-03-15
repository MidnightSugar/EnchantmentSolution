package org.ctp.enchantmentsolution.commands;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.enchantments.RegisterEnchantments;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EnchantInfo implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) player = (Player) sender;
		if (args.length == 0) sendEnchantInfo(sender, 1);
		else if (args.length == 1) {
			try {
				int page = Integer.parseInt(args[0]);
				sendEnchantInfo(sender, page);
				return true;
			} catch (NumberFormatException e) {

			}
			CustomEnchantment enchantment = null;
			for(CustomEnchantment enchant: RegisterEnchantments.getRegisteredEnchantments())
				if (enchant.getName().equalsIgnoreCase(args[0])) {
					enchantment = enchant;
					break;
				}
			if (enchantment == null) {
				HashMap<String, Object> codes = ChatUtils.getCodes();
				codes.put("%enchant%", args[0]);
				ChatUtils.sendMessage(sender, player, ChatUtils.getMessage(codes, "commands.enchant-not-found"), Level.WARNING);
			} else {
				String page = enchantment.getDetails();
				ChatUtils.sendMessage(sender, player, page, Level.INFO);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void sendEnchantInfo(CommandSender sender, int page) {
		Player player = null;
		if (sender instanceof Player) player = (Player) sender;
		if (page < 1) page = 1;
		List<CustomEnchantment> registered = RegisterEnchantments.getRegisteredEnchantments();
		while (registered.size() < (page - 1) * 10) {
			if (page == 1) break;
			page -= 1;
		}
		if (player != null) {
			JSONArray json = new JSONArray();
			JSONObject first = new JSONObject();
			first.put("text", "\n" + ChatColor.DARK_BLUE + "******");
			JSONObject second = new JSONObject();
			if (page > 1) {
				second.put("text", ChatColor.GREEN + "<<<");
				HashMap<Object, Object> action = new HashMap<Object, Object>();
				action.put("action", "run_command");
				action.put("value", "/enchantinfo " + (page - 1));
				second.put("clickEvent", action);
			} else
				second.put("text", ChatColor.DARK_BLUE + "***");
			JSONObject third = new JSONObject();
			third.put("text", ChatColor.DARK_BLUE + "******" + ChatColor.AQUA + " Enchantments Page " + page + ChatColor.DARK_BLUE + " ******");
			JSONObject fourth = new JSONObject();
			if (registered.size() > page * 10) {
				fourth.put("text", ChatColor.GREEN + ">>>");
				HashMap<Object, Object> action = new HashMap<Object, Object>();
				action.put("action", "run_command");
				action.put("value", "/enchantinfo " + (page + 1));
				fourth.put("clickEvent", action);
			} else
				fourth.put("text", ChatColor.DARK_BLUE + "***");
			JSONObject fifth = new JSONObject();
			fifth.put("text", ChatColor.DARK_BLUE + "******" + "\n");
			json.add(first);
			json.add(second);
			json.add(third);
			json.add(fourth);
			json.add(fifth);
			List<CustomEnchantment> alphabetical = RegisterEnchantments.getRegisteredEnchantmentsAlphabetical();
			for(int i = 0; i < 10; i++) {
				int num = i + (page - 1) * 10;
				if (num >= registered.size()) break;
				CustomEnchantment enchant = alphabetical.get(num);
				JSONObject name = new JSONObject();
				JSONObject desc = new JSONObject();
				JSONObject action = new JSONObject();
				action.put("action", "run_command");
				action.put("value", "/enchantinfo " + enchant.getName());
				name.put("text", shrink(ChatColor.GOLD + enchant.getDisplayName()));
				name.put("clickEvent", action);
				json.add(name);
				desc.put("text", shrink(ChatColor.GOLD + enchant.getDisplayName() + ChatColor.WHITE + ": " + ChatColor.WHITE + enchant.getDescription()).substring((ChatColor.GOLD + enchant.getDisplayName()).length()) + "\n");
				json.add(desc);
			}
			json.add(first);
			json.add(second);
			json.add(third);
			json.add(fourth);
			json.add(fifth);
			ChatUtils.sendRawMessage(player, json.toJSONString());
		} else {
			String message = "\n" + ChatColor.DARK_BLUE + "******" + (page > 1 ? "<<<" : "***") + "******" + ChatColor.AQUA + " Enchantments Page " + page + ChatColor.DARK_BLUE + " ******" + (registered.size() < (page - 1) * 10 ? ">>>" : "***") + "******" + "\n";
			for(int i = 0; i < 10; i++) {
				int num = i + (page - 1) * 10;
				if (num >= registered.size()) break;
				CustomEnchantment enchant = registered.get(num);
				message += shrink(ChatColor.GOLD + enchant.getDisplayName() + ": " + ChatColor.WHITE + enchant.getDescription()) + "\n";
			}
			message += "\n" + ChatColor.DARK_BLUE + "******" + (page > 1 ? "<<<" : "***") + "******" + ChatColor.AQUA + " Enchantments Page " + page + ChatColor.DARK_BLUE + " ******" + (registered.size() < (page - 1) * 10 ? ">>>" : "***") + "******" + "\n";
			ChatUtils.sendToConsole(Level.INFO, message);
		}
	}

	private String shrink(String s) {
		if (s.length() > 60) return s.substring(0, 58) + "...";
		return s;
	}

}
