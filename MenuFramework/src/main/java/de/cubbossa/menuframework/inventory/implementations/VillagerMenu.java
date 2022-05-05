package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.inventory.Action;
import de.cubbossa.menuframework.inventory.Button;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class VillagerMenu extends InventoryMenu {

    public static final Action<TargetContext<MerchantRecipe>> TRADE_SELECT = new Action<>();
    public static final Action<TargetContext<MerchantRecipe>> ATTEMPT_BUY = new Action<>();

    public record TradeButton(MerchantRecipe recipe,
                              Map<Action<? extends TargetContext<?>>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
    }

    @Getter
    private @Nullable Merchant merchant;
    private final Stack<TradeButton> offers;

    public VillagerMenu(ComponentLike title) {
        super(InventoryType.MERCHANT, title);
        this.offers = new Stack<>();
    }

    public @Nullable TradeButton getMerchantOffer(MerchantRecipe recipe) {
        return offers.stream().filter(tradeButton -> tradeButton.recipe.equals(recipe)).findFirst().orElse(null);
    }

    public void insertMerchantOffer(int slot, MerchantRecipe recipe, Button clickHandler) throws IndexOutOfBoundsException {
        offers.set(slot, new TradeButton(recipe, clickHandler.getClickHandler()));
    }

    public TradeButton addMerchantOffer(MerchantRecipe recipe, Button clickHandler) {
        TradeButton button = new TradeButton(recipe, clickHandler.getClickHandler());
        offers.push(button);
        return button;
    }

    public void removeMerchantOffer(TradeButton tradeButton) {
        offers.remove(tradeButton);
    }

    public int getIndex(TradeButton button) {
        return offers.indexOf(button);
    }

    @Override
    public Inventory createInventory(Player player, int page) {
        merchant = Bukkit.createMerchant(ChatUtils.toLegacy(getFallbackTitle()));
        merchant.setRecipes(offers.stream().map(TradeButton::recipe).collect(Collectors.toList()));
        return player.openMerchant(merchant, true).getTopInventory();
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
        if(merchant != null) {
            player.openMerchant(merchant, true).getTopInventory();
        }
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
