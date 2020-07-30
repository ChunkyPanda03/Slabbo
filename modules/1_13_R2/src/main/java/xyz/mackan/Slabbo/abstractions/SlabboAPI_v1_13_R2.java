package xyz.mackan.Slabbo.abstractions;

import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xyz.mackan.Slabbo.types.MetaKey;
import xyz.mackan.Slabbo.types.SlabType;
import xyz.mackan.Slabbo.utils.ItemUtil;
import xyz.mackan.Slabbo.manager.ShopManager;

import java.util.Collection;

public class SlabboAPI_v1_13_R2 implements SlabboAPI {
	public SlabboAPI_v1_13_R2 () {}

	public String getItemName (ItemStack itemStack) {
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

		return LocaleLanguage.a().a(nmsStack.getItem().getName());
	}

	public ItemStack getInteractionItemInHand (PlayerInteractEvent e) {
		return e.getItem();
	}

	public boolean isSlab (Block block) {
		BlockData blockData = block.getBlockData();

		return (blockData instanceof Slab);
	}

	@Override
	public SlabType getSlabType (Block block) {
		if (!isSlab(block)) return SlabType.NONE;

		BlockData blockData = block.getBlockData();

		Slab slab = (Slab) blockData;

		Slab.Type slabType = slab.getType();

		switch (slabType) {
			case TOP:
				return SlabType.TOP;
			case BOTTOM:
				return SlabType.BOTTOM;
			case DOUBLE:
				return SlabType.DOUBLE;
		}

		return null;
	}

	public void setGravity (Item item, boolean gravity) {
		item.setGravity(gravity);
	}

	public Collection<Entity> getNearbyEntities (Location location, double x, double y, double z) {
		return location.getWorld().getNearbyEntities(location, x, y, z);
	}

	public boolean isItem (Entity entity) {
		return (entity instanceof Item) || (entity instanceof CraftItem);
	}

	public void setChestName (Block chestBlock, String name) {
		Chest chest = (Chest) chestBlock.getState();

		chest.setCustomName(name);

		chest.update();
	}

	public boolean getNoPickup (Item item) {
		ItemStack itemStack = item.getItemStack();

		return ItemUtil.getLoreValue(itemStack, MetaKey.NO_PICKUP.getKey()).equals("1");
	}

	public boolean getNoDespawn (Item item) {
		ItemStack itemStack = item.getItemStack();

		return ItemUtil.getLoreValue(itemStack, MetaKey.NO_DESPAWN.getKey()).equals("1");
	}

	public boolean getNoMerge (Item item) {
		ItemStack itemStack = item.getItemStack();

		return ItemUtil.getLoreValue(itemStack, MetaKey.NO_MERGE.getKey()).equals("1");
	}

	public String getShopLocation (Item item) {
		ItemStack itemStack = item.getItemStack();

		String value = ItemUtil.getLoreValue(itemStack, MetaKey.SHOP_LOCATION.getKey());

		if (value.equals("")) return null;

		return value;
	}

	public void setNoPickup (Item item, int value) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setLoreValue(itemStack, MetaKey.NO_PICKUP.getKey(), ""+value));
	}


	public void setNoDespawn (Item item, int value) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setLoreValue(itemStack, MetaKey.NO_DESPAWN.getKey(), ""+value));
	}

	public void setNoMerge (Item item, int value) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setLoreValue(itemStack, MetaKey.NO_MERGE.getKey(), ""+value));
	}

	public void setShopLocation (Item item, Location location) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setLoreValue(itemStack, MetaKey.SHOP_LOCATION.getKey(), ShopManager.locationToString(location)));
	}

	public boolean isSlabboItem (Item item) {
		return getNoPickup(item) && getNoDespawn(item);
	}

	public boolean isStair (Block block) {
		BlockData blockData = block.getBlockData();

		return (blockData instanceof Stairs);
	}

	public boolean isUpsideDownStair (Block block) {
		if (!isStair(block)) return false;

		Stairs stairs = (Stairs) block.getBlockData();

		return stairs.getHalf() == Bisected.Half.TOP;
	}

	public boolean isInteractionOffHand(PlayerInteractEvent e) {
		return e.getHand() == EquipmentSlot.OFF_HAND;
	}
}
