package xyz.mackan.Slabbo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.mackan.Slabbo.GUI.ShopDeletionGUI;
import xyz.mackan.Slabbo.Slabbo;
import xyz.mackan.Slabbo.manager.ChestLinkManager;
import xyz.mackan.Slabbo.manager.ShopManager;
import xyz.mackan.Slabbo.abstractions.ISlabboSound;
import xyz.mackan.Slabbo.importers.ImportResult;
import xyz.mackan.Slabbo.importers.UShopImporter;
import xyz.mackan.Slabbo.types.Shop;
import xyz.mackan.Slabbo.types.ShopLimit;
import xyz.mackan.Slabbo.utils.DataUtil;
import xyz.mackan.Slabbo.utils.ItemUtil;
import xyz.mackan.Slabbo.utils.Misc;
import xyz.mackan.Slabbo.manager.ShopManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("slabbo")
@Description("Base command for slabbo")
public class SlabboCommand extends BaseCommand {
	ISlabboSound slabboSound = Bukkit.getServicesManager().getRegistration(ISlabboSound.class).getProvider();

	public Shop getLookingAtShop (Player player) {
		Block lookingAt = player.getTargetBlock((Set<Material>) null, 6);

		String locationString = ShopManager.locationToString(lookingAt.getLocation());

		if (ShopManager.shops.containsKey(locationString)) {
			return ShopManager.shops.get(locationString);
		}

		return null;
	}

	@HelpCommand
	public static void onCommand(CommandSender sender, CommandHelp help) {
		help.showHelp();
	}

	@Subcommand("reload")
	@Description("Reloads Slabbo")
	@CommandPermission("slabbo.reload")
	public void onReload (Player player) {
		player.sendMessage(Slabbo.localeManager.getString("general.general.reloading")+" Slabbo");

		Slabbo.getInstance().reloadConfig();

		ItemUtil.removeShopItems(player.getWorld());

		ChestLinkManager.links.clear();

		ShopManager.clearShops();

		ShopManager.loadShops();

		for (Map.Entry<String, Shop> shopEntry : ShopManager.shops.entrySet()) {
			String key = shopEntry.getKey();
			Shop shop = shopEntry.getValue();

			ItemUtil.dropShopItem(shop.location, shop.item, shop.quantity);
		}


		player.sendMessage("Slabbo "+Slabbo.localeManager.getString("general.general.reloaded")+"!");
	}

	@Subcommand("info")
	@Description("Shows information about Slabbo")
	@CommandPermission("slabbo.info")
	public void onInfo (Player sender) {
		sender.sendMessage("=====[ Slabbo Info ]=====");

		sender.sendMessage("Version: "+ Slabbo.getInstance().getDescription().getVersion());
		sender.sendMessage("Total Shops: "+ShopManager.shops.size());
		sender.sendMessage("Economy Provider: "+Slabbo.getEconomy().getName());

		sender.sendMessage("=====[ Slabbo Info ]=====");
	}

	@Subcommand("admin")
	@Description("Admin shop commands")
	@CommandPermission("slabbo.admin" +
			"|slabbo.admin.toggle" +
			"|slabbo.admin.limit" +
			"|slabbo.admin.limit.toggle" +
			"|slabbo.admin.limit.time" +
			"|slabbo.admin.limit.stock" +
			"|slabbo.admin.limit.stock.sell" +
			"|slabbo.admin.limit.stock.buy"
	)
	public class SlabboAdminCommand extends BaseCommand {
		@Subcommand("toggle")
		@Description("Toggles the shop as being an admin shop")
		@CommandPermission("slabbo.admin.toggle")
		public void onToggleAdmin (Player player) {
			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
				return;
			}

			lookingAtShop.admin = !lookingAtShop.admin;

			if (lookingAtShop.admin) {
				player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.getString("success-message.general.admin-create"));
			} else {
				player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.getString("success-message.general.admin-destroy"));
			}

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
		}

		@Subcommand("limit")
		@Description("Commands for setting the shop to have a limited stock")
		@CommandPermission("slabbo.admin.limit|slabbo.admin.limit.toggle|slabbo.admin.limit.time|slabbo.admin.limit.stock|slabbo.admin.limit.stock.buy|slabbo.admin.limit.stock.sell")
		public class SlabboAdminLimitCommand extends BaseCommand {
			@Subcommand("toggle")
			@Description("Toggles the admin shop as having limited stock")
			@CommandPermission("slabbo.admin.limit.toggle")
			public void onToggleLimit (Player player) {
				Shop lookingAtShop = getLookingAtShop(player);
				if (lookingAtShop == null) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
					return;
				}

				if (!lookingAtShop.admin) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-admin-shop"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
					return;
				}

				ShopLimit limit = lookingAtShop.shopLimit;

				if (lookingAtShop.shopLimit == null) {
					limit = new ShopLimit(0, 0, 0, 0L, false);
				}

				limit.enabled = !limit.enabled;

				lookingAtShop.shopLimit = limit;

				if (limit.enabled) {
					limit.restock();
					player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.getString("success-message.general.limited-stock.create"));
				} else {
					player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.getString("success-message.general.limited-stock.destroy"));
				}

				ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

				DataUtil.saveShops();

				player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
			}

			@Subcommand("stock")
			@Description("Commands for setting the limited stocks")
			@CommandPermission("slabbo.admin.limit.stock|slabbo.admin.limit.stock.buy|slabbo.admin.limit.stock.sell")
			public class SlabboAdminLimitStockCommand extends BaseCommand {
				@Subcommand("buy")
				@Description("Sets the limited buy stock the shop has")
				@CommandPermission("slabbo.admin.limit.stock.buy")
				public void onSetBuyStock (Player player, int stock) {
					Shop lookingAtShop = getLookingAtShop(player);
					if (lookingAtShop == null) {
						player.sendMessage(ChatColor.RED + Slabbo.localeManager.getString("error-message.general.not-a-shop"));
						player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
						return;
					}

					if (!lookingAtShop.admin) {
						player.sendMessage(ChatColor.RED + Slabbo.localeManager.getString("error-message.general.not-admin-shop"));
						player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
						return;
					}

					ShopLimit limit = lookingAtShop.shopLimit;

					if (lookingAtShop.shopLimit == null) {
						limit = new ShopLimit(0, 0, 0, 0L, false);
					}

					limit.buyStock = stock;

					limit.restock();

					lookingAtShop.shopLimit = limit;

					player.sendMessage(ChatColor.GREEN + Slabbo.localeManager.replaceSingleKey("success-message.general.limited-stock.set-buy-stock", "stock", stock));

					ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

					DataUtil.saveShops();

					player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
				}

				@Subcommand("sell")
				@Description("Sets the limited sell stock the shop has")
				@CommandPermission("slabbo.admin.limit.stock.sell")
				public void onSetSellStock (Player player, int stock) {
					Shop lookingAtShop = getLookingAtShop(player);
					if (lookingAtShop == null) {
						player.sendMessage(ChatColor.RED + Slabbo.localeManager.getString("error-message.general.not-a-shop"));
						player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
						return;
					}

					if (!lookingAtShop.admin) {
						player.sendMessage(ChatColor.RED + Slabbo.localeManager.getString("error-message.general.not-admin-shop"));
						player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
						return;
					}

					ShopLimit limit = lookingAtShop.shopLimit;

					if (lookingAtShop.shopLimit == null) {
						limit = new ShopLimit(0, 0, 0, 0L, false);
					}

					limit.sellStock = stock;

					limit.restock();

					lookingAtShop.shopLimit = limit;

					player.sendMessage(ChatColor.GREEN + Slabbo.localeManager.replaceSingleKey("success-message.general.limited-stock.set-sell-stock", "stock", stock));

					ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

					DataUtil.saveShops();

					player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
				}
			}

			@Subcommand("time")
			@Description("Sets the time before the shop restocks, in seconds")
			@CommandPermission("slabbo.admin.limit.time")
			public void onSetTime (Player player, int time) {
				Shop lookingAtShop = getLookingAtShop(player);
				if (lookingAtShop == null) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
					return;
				}

				if (!lookingAtShop.admin) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-admin-shop"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);
					return;
				}

				ShopLimit limit = lookingAtShop.shopLimit;

				if (lookingAtShop.shopLimit == null) {
					limit = new ShopLimit(0, 0, 0, 0L, false);
				}

				limit.restockTime = time;

				lookingAtShop.shopLimit = limit;

				player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceSingleKey("success-message.general.limited-stock.set-time", "time", time));

				ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

				DataUtil.saveShops();

				player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
			}
		}
	}

	@Subcommand("destroy")
	@Description("Destroys a shop")
	@CommandPermission("slabbo.destroy")
	public void onDestroyShop(Player player) {
		Shop lookingAtShop = getLookingAtShop(player);
		if (lookingAtShop == null) {
			player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
			player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

			return;
		}

		boolean isShopOwner = lookingAtShop.ownerId.equals(player.getUniqueId());
		boolean canDestroyOthers = player.hasPermission("slabbo.destroy.others");

		if (!isShopOwner) {
			if (!canDestroyOthers) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-shop-owner"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}
		}

		ShopDeletionGUI deletionGUI = new ShopDeletionGUI(lookingAtShop);
		deletionGUI.openInventory(player);
	}

	@Subcommand("import")
	@Description("Imports shop from another plugin")
	@CommandPermission("slabbo.importshops")
	@CommandCompletion("ushops @importFiles")
	public void onImportShops(Player player, String type, String file) {
		File importFile = new File(Slabbo.getDataPath()+"/"+file);

		if (!importFile.exists()) {
			player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.import.file-not-found"));
			return;
		}

		ImportResult result;

		switch (type.toLowerCase()) {
			case "ushops":
				player.sendMessage(Slabbo.localeManager.getString("success-message.import.importing"));
				result = UShopImporter.importUShops(importFile);
				break;
			default:
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.import.plugin-not-supported"));
				return;
		}

		if (result == null) {
			player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.import.general-error"));
			return;
		}

		for (Shop shop : result.shops) {
			ItemUtil.dropShopItem(shop.location, shop.item, shop.quantity);

			ShopManager.put(shop.getLocationString(), shop);
		}

		DataUtil.saveShops();

		HashMap<String, Object> replacementMap = new HashMap<String, Object>();

		replacementMap.put("count", result.shops.size());
		replacementMap.put("skipped", result.skippedShops.size());

		player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.import.success", replacementMap));
	}

	@Subcommand("save")
	@Description("Saves slabbo shops")
	@CommandPermission("slabbo.save")
	public void onSave (Player player) {
		DataUtil.saveShops();

		player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.getString("success-message.general.shops-saved"));
	}

	@Subcommand("modify")
	@Description("Modifies the shop")
	@CommandPermission("slabbo.modify.self.buyprice" +
			"|slabbo.modify.self.sellprice" +
			"|slabbo.modify.self.quantity" +
			"|slabbo.modify.self.note" +
			"|slabbo.modify.others.buyprice" +
			"|slabbo.modify.others.sellprice" +
			"|slabbo.modify.others.quantity" +
			"|slabbo.modify.others.note" +
			"|slabbo.modify.admin.owner" +
			"|slabbo.modify.admin.stock")
	public class SlabboModifyCommand extends BaseCommand {
		@HelpCommand
		public void onCommand(CommandSender sender, CommandHelp help) {
			help.showHelp();
		}

		@Subcommand("buyprice")
		@Description("Sets the buying price for the shop")
		@CommandPermission("slabbo.modify.self.buyprice|slabbo.modify.others.buyprice")
		public void onModifyBuyPrice(Player player, int newBuyingPrice) {
			if (newBuyingPrice < -1) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.modify.invalid-buy-price"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			boolean isShopOwner = lookingAtShop.ownerId.equals(player.getUniqueId());
			boolean canModifyOthers = player.hasPermission("slabbo.modify.others.buyprice");

			if (!isShopOwner) {
				if (!canModifyOthers) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-shop-owner"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

					return;
				}
			}

			lookingAtShop.buyPrice = newBuyingPrice;

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("price", Slabbo.localeManager.getCurrencyString(newBuyingPrice));

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.buyprice-set", replacementMap));

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);

		}

		@Subcommand("sellprice")
		@Description("Sets the selling price for the shop")
		@CommandPermission("slabbo.modify.self.sellprice|slabbo.modify.others.sellprice")
		public void onModifySellPrice(Player player, int newSellingPrice) {
			if (newSellingPrice < -1) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.modify.invalid-sell-price"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			boolean isShopOwner = lookingAtShop.ownerId.equals(player.getUniqueId());
			boolean canModifyOthers = player.hasPermission("slabbo.modify.others.sellprice");

			if (!isShopOwner) {
				if (!canModifyOthers) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-shop-owner"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

					return;
				}
			}

			lookingAtShop.sellPrice = newSellingPrice;

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("price", Slabbo.localeManager.getCurrencyString(newSellingPrice));

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.sellprice-set", replacementMap));

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);

		}

		@Subcommand("quantity")
		@Description("Sets the quantity for the shop")
		@CommandPermission("slabbo.modify.self.quantity|slabbo.modify.others.quantity")
		public void onModifyQuantity(Player player, int newQuantity) {
			if (newQuantity < 0) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.modify.invalid-quantity"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			boolean isShopOwner = lookingAtShop.ownerId.equals(player.getUniqueId());
			boolean canModifyOthers = player.hasPermission("slabbo.modify.others.quantity");

			if (!isShopOwner) {
				if (!canModifyOthers) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-shop-owner"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

					return;
				}
			}

			lookingAtShop.quantity = newQuantity;

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("quantity", newQuantity);

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.quantity-set", replacementMap));

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
		}

		@Subcommand("owner")
		@Description("Sets the owner for the shop")
		@CommandPermission("slabbo.modify.admin.owner")
		@CommandCompletion("@players")
		public void onChangeOwner (Player player, OfflinePlayer newOwner) {

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			UUID newOwnerID = newOwner.getUniqueId();


			lookingAtShop.ownerId = newOwnerID;

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("owner", newOwner.getName());

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.owner-set", replacementMap));
		}

		@Subcommand("stock")
		@Description("Sets the stock for the shop")
		@CommandPermission("slabbo.modify.admin.stock")
		public void onSetStock (Player player, int newStock) {

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			lookingAtShop.stock = newStock;

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("stock", newStock);

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.stock-set", replacementMap));
		}

		@Subcommand("stock")
		@Description("Sets the sellers note for the shop")
		@CommandPermission("slabbo.modify.self.note|slabbo.modify.others.note")
		public void onSetNote (Player player, String note) {
			if (note.equals("")) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.modify.invalid-note"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			Shop lookingAtShop = getLookingAtShop(player);
			if (lookingAtShop == null) {
				player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-a-shop"));
				player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

				return;
			}

			boolean isShopOwner = lookingAtShop.ownerId.equals(player.getUniqueId());
			boolean canModifyOthers = player.hasPermission("slabbo.modify.others.note");

			if (!isShopOwner) {
				if (!canModifyOthers) {
					player.sendMessage(ChatColor.RED+Slabbo.localeManager.getString("error-message.general.not-shop-owner"));
					player.playSound(player.getLocation(), slabboSound.getSoundByKey("BLOCKED"), 1, 1);

					return;
				}
			}

			lookingAtShop.note = note;

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("note", note);

			player.sendMessage(ChatColor.GREEN+Slabbo.localeManager.replaceKey("success-message.modify.note-set", replacementMap));

			ShopManager.shops.put(lookingAtShop.getLocationString(), lookingAtShop);

			DataUtil.saveShops();

			player.playSound(player.getLocation(), slabboSound.getSoundByKey("MODIFY_SUCCESS"), 1, 1);
		}

	}

	@Subcommand("list")
	@Description("Commands for listing Slabbo shops")
	@CommandPermission("slabbo.list.all|slabbo.list.self")
	public class SlabboListCommand extends BaseCommand {
		public void sendShopList (Player player, List<Shop> shops, int page, String command) {
			if (shops.size() <= 0) {
				player.sendMessage(net.md_5.bungee.api.ChatColor.RED+Slabbo.localeManager.getString("error-message.general.no-shops-found"));
				return;
			}

			int perPage = 10;

			int pageCount = (int) Math.ceil((double)shops.size() / (double)perPage);

			List<Shop> subList = Misc.getPage(shops, page, perPage);

			TextComponent component = new TextComponent("=== [Slabbo Shops] === ");

			for (Shop shop : subList) {
				component.addExtra("\n"+shop.getInfoString());
			}

			TextComponent previousPage = new TextComponent("<<<");
			TextComponent nextPage = new TextComponent(">>>");

			String previousPageCommand = String.format("%s %s", command, page - 1);
			String nextPageCommand = String.format("%s %s", command, page + 1);

			if (page + 1 <= pageCount) {
				nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, nextPageCommand));

				BaseComponent[] hoverEventComponents = new BaseComponent[]{
						new TextComponent("Next Page")
				};

				nextPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventComponents));
			} else {
				nextPage.setColor(net.md_5.bungee.api.ChatColor.GRAY);
			}

			if (page > pageCount - 1) {
				previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, previousPageCommand));

				BaseComponent[] hoverEventComponents = new BaseComponent[]{
						new TextComponent("Previous Page")
				};

				previousPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventComponents));
			} else {
				previousPage.setColor(net.md_5.bungee.api.ChatColor.GRAY);
			}

			TextComponent pager = new TextComponent("\n");

			pager.addExtra(previousPage);

			HashMap<String, Object> replacementMap = new HashMap<String, Object>();

			replacementMap.put("page", page);
			replacementMap.put("pageCount", pageCount);

			String pagerString = Slabbo.localeManager.replaceKey("general.general.pager", replacementMap);

			pager.addExtra(" "+pagerString+" ");

			pager.addExtra(nextPage);

			component.addExtra(pager);

			player.spigot().sendMessage(component);
		}

		@HelpCommand
		public void onCommand(CommandSender sender, CommandHelp help) {
			help.showHelp();
		}

		@Subcommand("all radius")
		@Description("Lists all the Slabbo shops in a radius")
		@CommandPermission("slabbo.list.all")
		public void onListAllRadius (Player player, double radius, @Optional String page) {
			int listPage = 1;

			if (page == null || page.equals("")) {
				try { listPage = Integer.parseInt(page); } catch (Exception e) {}
			}

			Location playerLocation = player.getLocation();

			ArrayList<Shop> shopsInRadius = new ArrayList<Shop>();

			if (radius <= -1) {
				shopsInRadius = new ArrayList<Shop>(ShopManager.shops.values());
			}

			for (Shop shop : ShopManager.shops.values()) {
				double distance = playerLocation.distance(shop.location);

				if (distance <= radius) {
					shopsInRadius.add(shop);
				}
			}

			sendShopList(player, shopsInRadius, listPage, "/slabbo list all radius "+radius);
		}

		@Subcommand("all")
		@Description("Lists all the Slabbo shops")
		@CommandPermission("slabbo.list.all")
		public void onListAll (Player player, @Optional String page) {
			int listPage = 1;

			if (page != null && !page.equals("")) {
				try { listPage = Integer.parseInt(page); } catch (Exception e) {}
			}

			List<Shop> shops = new ArrayList<Shop>(ShopManager.shops.values());

			sendShopList(player, shops, listPage, "/slabbo list all");
		}

		@Subcommand("mine radius")
		@Description("Lists all the Slabbo shops you own in a radius")
		@CommandPermission("slabbo.list.self")
		public void onListMineRadius (Player player, double radius, @Optional String page) {
			int listPage = 1;

			if (page == null || page.equals("")) {
				try { listPage = Integer.parseInt(page); } catch (Exception e) {}
			}

			Location playerLocation = player.getLocation();

			ArrayList<Shop> shopsInRadius = new ArrayList<Shop>();

			if (radius <= -1) {
				shopsInRadius = new ArrayList<Shop>(ShopManager.shops.values());
			}

			for (Shop shop : ShopManager.shops.values()) {
				double distance = playerLocation.distance(shop.location);

				if (distance <= radius && shop.ownerId.equals(player.getUniqueId())) {
					shopsInRadius.add(shop);
				}
			}

			sendShopList(player, shopsInRadius, listPage, "/slabbo list mine radius "+radius);
		}

		@Subcommand("mine")
		@Description("Lists all the Slabbo shops you own")
		@CommandPermission("slabbo.list.self")
		public void onListMine (Player player, @Optional String page) {
			int listPage = 1;

			if (page == null || page.equals("")) {
				try { listPage = Integer.parseInt(page); } catch (Exception e) {}
			}

			List<Shop> shops = new ArrayList<Shop>(ShopManager.shops.values());

			List<Shop> myShops = shops.stream().filter(shop -> shop.ownerId.equals(player.getUniqueId())).collect(Collectors.toList());

			sendShopList(player, myShops, listPage, "/slabbo list mine");
		}
	}
}
