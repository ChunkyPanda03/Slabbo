package xyz.mackan.Slabbo.GUI.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.mackan.Slabbo.types.Shop;
import xyz.mackan.Slabbo.utils.Misc;

import java.util.Arrays;

public class GUIItems {
	public static ItemStack getBuyPriceItem (int buyPrice) {
		ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN+"Buy price");

		meta.setLore(Arrays.asList("§r$"+buyPrice, "Click to set", "§r(Zero means not for sale)"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getSellPriceItem (int sellPrice) {
		ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.RED+"Sell price");

		meta.setLore(Arrays.asList("§r$"+sellPrice, "Click to set", "§r(Zero means not buying)"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getAmountItem (int quantity) {
		ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.YELLOW+"Quantity");

		meta.setLore(Arrays.asList("§rAmount per transaction: "+quantity, "Click to set", "§r(Amount of items per buy / sell)"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getCancelItem () {
		ItemStack item = new ItemStack(Material.BARRIER, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.RED+"Cancel");

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getConfirmItem (String locationString) {
		ItemStack item = new ItemStack(Material.NETHER_STAR, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN+"Confirm");

		meta.setLore(Arrays.asList("New Shop", locationString));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getUserBuyItem (String itemName, int quantity, int price, int stock) {
		ItemStack item = new ItemStack(Material.GOLD_INGOT, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD+"Buy '"+itemName+"' * "+quantity);

		meta.setLore(Arrays.asList("§rBuy for: $"+price, "In Stock: "+stock, "("+ Misc.countStacks(stock)+" stacks)"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getUserSellItem (String itemName, int quantity, int price, int stock) {
		ItemStack item = new ItemStack(Material.IRON_INGOT, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD+"Sell '"+itemName+"' * "+quantity);

		meta.setLore(Arrays.asList("§rSell for: $"+price, "In Stock: "+stock, "("+ Misc.countStacks(stock)+" stacks)"));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getUserFundsItem (double funds) {
		ItemStack item = new ItemStack(Material.PAPER, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD+"Current funds");

		meta.setLore(Arrays.asList("§rFunds: §a$"+funds));

		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack getUserInfoItem (Shop shop) {
		ItemStack item = new ItemStack(Material.COMMAND_BLOCK, 1);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.GOLD+"Slabbo Shop");

		double buyPerItem = shop.quantity / shop.buyPrice;
		double sellPerItem = shop.quantity / shop.sellPrice;

		OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.ownerId);

		meta.setLore(Arrays.asList(
				"§rOwned by "+owner.getName(),
				"",
				"§rSelling: "+shop.item.getType(),
				"Buy "+shop.quantity+" for $"+shop.buyPrice+" ($"+buyPerItem+" each)",
				"Sell "+shop.quantity+" for $"+shop.sellPrice+" ($"+sellPerItem+" each)"
		));

		item.setItemMeta(meta);

		return item;
	}
}
