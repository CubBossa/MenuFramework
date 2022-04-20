package de.cubbossa.guiframework.inventory.implementations;

import com.google.common.base.Strings;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;

/**
 * A Crafting Table Menu that animates between all recepies for a certain ItemStack
 */
public class CraftMenu extends InventoryMenu {

    private final ItemStack[][] animationMap;

    /**
     * Creates an inventory with an animation that switches the crafting recipes for this itemstack.
     * use {@link #setClickHandler(int, Action, ContextConsumer)} to set ClickHandler for the
     * crafting slots. 0 = Result slot, 1 - 9 = Crafting Slots.
     * <p>
     * This renders only shaped and shapeless recipes but no furnace recipes.
     *
     * @param stack The stack to display all recipes for
     * @param ticks The tick count to wait before displaying the next recipe for this item
     */
    public CraftMenu(ItemStack stack, int ticks) {
        super(InventoryType.WORKBENCH, Component.text("Crafting"));

        List<Recipe> recipes = Bukkit.getRecipesFor(stack).stream()
                .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe).toList();
        animationMap = new ItemStack[9][recipes.size()];

        if (recipes.isEmpty()) {
            return;
        }

        int recipeIndex = 0;
        for (Recipe recipe : recipes) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                String combined = concatShape(shapedRecipe.getShape());
                for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
                    if (combined.charAt(slotIndex) == ' ') {
                        slotIndex++;
                        continue;
                    }
                    animationMap[slotIndex++][recipeIndex] = shapedRecipe.getIngredientMap().get(combined.charAt(slotIndex));
                }

            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                int slotIndex = 0;
                for (ItemStack s : shapelessRecipe.getIngredientList()) {
                    animationMap[slotIndex++][recipeIndex] = s;
                }
            }
            recipeIndex++;
        }
        if (recipes.size() > 1) {
            for (int i = 0; i < 9; i++) {
                int finalI = i;
                setItem(i + 1, animationMap[i][0]);
                playAnimation(i + 1, ticks, animationContext -> {
                    return animationMap[finalI][(animationContext.getTicks() % ticks) % recipes.size()];
                });
            }
        } else {
            for (int slot = 1; slot < 10; slot++) {
                setItem(slot, animationMap[slot - 1][0]);
            }
        }
        setItem(0, stack);
    }

    private String concatShape(String[] shape) {
        StringBuilder combined = new StringBuilder();
        for (String string : shape) {
            combined.append(string).append(Strings.repeat(" ", 3 - string.length()));
        }
        return combined.toString();
    }
}
