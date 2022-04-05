package de.cubbossa.guiframework.inventory.implementations;

import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.List;

public class CraftMenu extends InventoryMenu {

    private final ItemStack[][] animationMap;

    /**
     * Creates an inventory with an animation that switches the crafting recipes for this itemstack.
     * use {@link #setClickHandler(Object, ContextConsumer, int...)} to set ClickHandler for the
     * crafting slots. 0 = Result slot, 1 - 9 = Crafting Slots.
     * <p>
     * This renders only shaped and shapeless recipes but no furnace recipes.
     *
     * @param stack The stack to display all recipes for
     * @param ticks The tick count to wait before displaying the next recipe for this item
     */
    public CraftMenu(ItemStack stack, int ticks) {
        super(InventoryType.CRAFTING, Component.text("Crafting"));

        List<Recipe> recipes = Bukkit.getRecipesFor(stack).stream()
                .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe).toList();
        animationMap = new ItemStack[recipes.size()][9];

        int recipeIndex = 0;
        for (Recipe recipe : recipes) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {

                String combined = String.join("", shapedRecipe.getShape());
                int slotIndex = 1;
                for (char c : combined.toCharArray()) {
                    animationMap[slotIndex][recipeIndex] = shapedRecipe.getIngredientMap().get(c);
                    slotIndex++;
                }

            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                int slotIndex = 0;
                for (ItemStack s : shapelessRecipe.getIngredientList()) {
                    animationMap[slotIndex][recipeIndex] = s;
                }
            }
            recipeIndex++;
        }
        if (recipes.size() > 1) {
            for (int i = 0; i < 9; i++) {
                int finalI = i;
                playAnimation(ticks, animationContext -> animationMap[finalI][(animationContext.getTicks() / ticks) % recipes.size()], i + 1);
            }
        } else {
            int slot = 1;
            for (ItemStack i : animationMap[0]) {
                setItem(i, slot);
                slot++;
            }
        }
        setItem(stack, 0);
    }
}
