package xyz.mackan.Slabbo.abstractions;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.mackan.Slabbo.Slabbo;
import xyz.mackan.Slabbo.types.AttributeKey;
import xyz.mackan.Slabbo.types.MetaKey;
import xyz.mackan.Slabbo.types.SlabType;
import xyz.mackan.Slabbo.utils.ItemUtil;
import xyz.mackan.Slabbo.utils.ShopUtil;

import java.util.Collection;
import java.util.List;

public class SlabboAPI_v1_15_R1 implements SlabboAPI {
	public SlabboAPI_v1_15_R1 () {}

	public String getItemName (ItemStack itemStack) {
		net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);

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

		int noPickup = ItemUtil.getContainerIntValue(itemStack, AttributeKey.NO_PICKUP.getKey());

		if (noPickup <= -1) {
			String value = ItemUtil.getLoreValue(itemStack, MetaKey.NO_PICKUP.getKey());

			return value.equals("1");
		}

		return noPickup == 1;
	}

	public boolean getNoDespawn (Item item) {
		ItemStack itemStack = item.getItemStack();

		int noDespawn = ItemUtil.getContainerIntValue(itemStack, AttributeKey.NO_DESPAWN.getKey());

		return noDespawn == 1;
	}

	public boolean getNoMerge (Item item) {
		ItemStack itemStack = item.getItemStack();

		int noMerge = ItemUtil.getContainerIntValue(itemStack, AttributeKey.NO_MERGE.getKey());

		return noMerge == 1;
	}

	public String getShopLocation (Item item) {
		ItemStack itemStack = item.getItemStack();

		String shopLocation = ItemUtil.getContainerStringValue(itemStack, AttributeKey.SHOP_LOCATION.getKey());

		return shopLocation;
	}

	public void setNoPickup (Item item, int value) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setContainerIntValue(itemStack, AttributeKey.NO_PICKUP.getKey(), value));
	}


	public void setNoDespawn (Item item, int value) {
		ItemStack itemStack = item.getItemStack();
		item.setItemStack(ItemUtil.setContainerIntValue(itemStack, AttributeKey.NO_DESPAWN.getKey(), value));
	}

	public void setNoMerge (Item item, int value) {
		ItemStack itemStack = item.getItemStack();
		item.setItemStack(ItemUtil.setContainerIntValue(itemStack, AttributeKey.NO_MERGE.getKey(), value));
	}

	public void setShopLocation (Item item, Location location) {
		ItemStack itemStack = item.getItemStack();

		item.setItemStack(ItemUtil.setContainerStringValue(itemStack, AttributeKey.SHOP_LOCATION.getKey(), ShopUtil.locationToString(location)));
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
