package de.cubbossa.menuframework.inventory.implementations;

import de.cubbossa.menuframework.GUIHandler;
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
import java.util.logging.Level;
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
        super(InventoryType.MERCHANT, title, new int[]{0, 1, 2});
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
        merchant = Bukkit.createMerchant(ChatUtils.toLegacy(getTitle()));
        merchant.setRecipes(offers.stream().map(TradeButton::recipe).collect(Collectors.toList()));

        InventoryView view = player.openMerchant(merchant, true);
        return view == null ? null : view.getInventory(0);
    }

    @Override
    protected void openInventory(Player player, Inventory inventory) {
        if (merchant != null) {
            this.inventory = player.openMerchant(merchant, true).getTopInventory();
        }
    }

    @Override
    public <C extends TargetContext<?>> boolean handleInteract(Action<C> action, C context) {

        Action<TargetContext<MerchantRecipe>> a = action == TRADE_SELECT ? TRADE_SELECT : null;
        if (a == null && context.getSlot() == 2) {
            a = ATTEMPT_BUY;
        }
        if (a != null) {
            int selected = ((MerchantInventory) inventory).getSelectedRecipeIndex();
            TradeButton btn = offers.get(selected);

            if (btn == null) {
                return context.isCancelled();
            }
            MerchantRecipe target = merchant == null ? null : merchant.getRecipe(selected);
            if (target == null) {
                return context.isCancelled();
            }
            TargetContext<MerchantRecipe> tContext = new TargetContext<>(context.getPlayer(), context.getMenu(), selected, a, context.isCancelled(), target);
            try {
                ContextConsumer<TargetContext<MerchantRecipe>> handler = (ContextConsumer<TargetContext<MerchantRecipe>>) btn.clickHandler.get(a);
                handler.accept(tContext);
            } catch (Throwable t) {
                context.setCancelled(true);
                GUIHandler.getInstance().getLogger().log(Level.SEVERE, "Error while handling GUI interaction of player " + context.getPlayer().getName(), t);
            }
            return tContext.isCancelled();
        } else {
            return super.handleInteract(action, context);
        }
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
