package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

public class VillagerMenu extends InventoryMenu {

	public static final Action<TargetContext<MerchantRecipe>> TRADE_SELECT = new Action<>();
	public static final Action<TargetContext<MerchantRecipe>> ATTEMPT_BUY = new Action<>();

	@Getter
	private @Nullable Merchant merchant;
	private final Stack<MerchantRecipe> offers;

	public VillagerMenu(ComponentLike title) {
		super(InventoryType.MERCHANT, title);
		this.offers = new Stack<>();
	}

	public void insertMerchantOffer(int slot, MerchantRecipe recipe) throws IndexOutOfBoundsException {
		offers.set(slot, recipe);
	}

	public void addMerchantOffer(MerchantRecipe recipe) {
		offers.push(recipe);
	}

	public void removeMerchantOffer(MerchantRecipe recipe) {
		offers.remove(recipe);
	}

	public void getIndex(MerchantRecipe recipe) {
		offers.indexOf(recipe);
	}

	@Override
	public Inventory createInventory(Player player, int page) {
		merchant = Bukkit.createMerchant(ChatUtils.toLegacy(getFallbackTitle()));
		merchant.setRecipes(offers);
		return player.openMerchant(merchant, true).getTopInventory();
	}

	@Override
	protected void openInventory(Player player, Inventory inventory) {

	}

	@Override
	public <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context) {
		MerchantRecipe selected = ((MerchantInventory) inventory).getSelectedRecipe();
		if (context.getSlot() == 2 && selected != null) {
			int index = ((MerchantInventory) inventory).getSelectedRecipeIndex();
			return super.handleInteract(ATTEMPT_BUY, new TargetContext<>(context.getPlayer(), context.getMenu(), index, ATTEMPT_BUY, context.isCancelled(), selected));
		}
		return super.handleInteract(action, context);
	}

	public static class MerchantBuilder {

		public static MerchantBuilder builder() {
			return new MerchantBuilder();
		}

		private ItemStack a = new ItemStack(Material.EMERALD);
		private ItemStack b;
		private ItemStack c = new ItemStack(Material.EMERALD);
		private int uses;
		private int maxUses;
		private int experience = -1;
		private float priceMultiplier = 1;
		private int specialPrice = -1;

		private MerchantBuilder() {
		}

		public MerchantBuilder withLeftCost(ItemStack stack) {
			a = stack;
			return this;
		}

		public MerchantBuilder withRightCost(ItemStack stack) {
			b = stack;
			return this;
		}

		public MerchantBuilder withArticle(ItemStack stack) {
			c = stack;
			return this;
		}

		public MerchantBuilder withUses(int uses, int maxUses) {
			this.uses = uses;
			this.maxUses = maxUses;
			return this;
		}

		public MerchantBuilder withExperience(int experience) {
			this.experience = experience;
			return this;
		}

		public MerchantBuilder withPriceMultiplier(float priceMultiplier) {
			this.priceMultiplier = priceMultiplier;
			return this;
		}

		public MerchantBuilder withSpecialPrice(int specialPrice) {
			this.specialPrice = specialPrice;
			return this;
		}

		public MerchantRecipe build() {
			MerchantRecipe recipe = new MerchantRecipe(c, uses, maxUses, experience != -1, experience, priceMultiplier);
			recipe.setIngredients(b == null ? List.of(a) : List.of(a, b));
			if (specialPrice != -1) {
				recipe.setSpecialPrice(specialPrice);
			}
			return recipe;
		}
	}
}
