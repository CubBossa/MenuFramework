package de.cubbossa.guiframework.inventory;

import de.cubbossa.guiframework.inventory.context.ContextConsumer;
import de.cubbossa.guiframework.util.ItemStackUtils;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import de.cubbossa.guiframework.util.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter
public class Button {

    private Supplier<ItemStack> stackSupplier;
    private Sound sound;
    private float pitch = 1f;
    private float volume = .8f;
    private final Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler = new HashMap<>();

    public static Button builder() {
        return new Button();
    }

    public @Nullable ItemStack getStack() {
        return stackSupplier == null ? null : stackSupplier.get();
    }

    public Button withItemStack(Supplier<ItemStack> stackSupplier) {
        this.stackSupplier = stackSupplier;
        return this;
    }

    /**
     * @param stack the icon itemstack
     * @return the builder instance
     */
    public Button withItemStack(ItemStack stack) {
        this.stackSupplier = () -> stack;
        return this;
    }

    /**
     * @param material the material of the icon
     * @return the builder instance
     */
    public Button withItemStack(Material material) {
        this.stackSupplier = () -> new ItemStack(material);
        return this;
    }

    /**
     * @param material the material of the icon
     * @param name     the name component of the icon
     * @return the builder instance
     */
    public Button withItemStack(Material material, Component name) {
        stackSupplier = () -> {
            ItemStack s = new ItemStack(material);
            ItemMeta meta = s.getItemMeta();
            meta.setDisplayName(ChatUtils.toGson(name));
            s.setItemMeta(meta);
            return s;
        };
        return this;
    }

    /**
     * @param material the material of the icon
     * @param name     the name component of the icon
     * @param lore     the lore of the icon
     * @return the builder instance
     */
    public Button withItemStack(Material material, Component name, List<Component> lore) {
        stackSupplier = () -> ItemStackUtils.createItemStack(material, name, lore);
        return this;
    }

    /**
     * Creates a player head item stack
     *
     * @param playerHeadOwner the skin to apply to the player head
     * @return the builder instance
     */
    public Button withItemStack(Player playerHeadOwner) {
        return withItemStack(playerHeadOwner, null);
    }

    /**
     * Creates a player head item stack
     *
     * @param playerHeadOwner the skin to apply to the player head
     * @param lore            the lore to add to the player head item
     * @return the builder instance
     */
    public Button withItemStack(Player playerHeadOwner, @Nullable List<Component> lore) {
        stackSupplier = () -> ItemStackUtils.createCustomHead(playerHeadOwner, GUIHandler.getInstance()
                .getAudiences().player(playerHeadOwner).getOrDefault(Identity.DISPLAY_NAME, Component.text(playerHeadOwner.getName())), lore);
        return this;
    }

    /**
     * @param sound a {@link Sound} to play when clicked
     * @return the builder instance
     */
    public Button withSound(Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Keep in mind that pitch only ranges from 0.5f to 2f.
     * Volume has its maximum at 1, from then on it only increases in range (falloff distance)
     *
     * @param sound  a {@link Sound} to play when clicked
     * @param volume the volume to play the sound with
     * @param pitch  the pitch to play the sound with
     * @return the builder instance
     */
    public Button withSound(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        return this;
    }

    /**
     * Keep in mind that pitch only ranges from 0.5f to 2f.
     * Volume has its maximum at 1, from then on it only increases in range (falloff distance)
     *
     * @param sound      a {@link Sound} to play when clicked
     * @param volumeFrom the lower limit for the random volume
     * @param volumeTo   the upper limit for the random volume
     * @param pitchFrom  the lower limit for the random pitch
     * @param pitchTo    the upper limit for the random pitch
     * @return the builder instance
     */
    public Button withSound(Sound sound, float volumeFrom, float volumeTo, float pitchFrom, float pitchTo) {
        this.sound = sound;
        this.volume = (float) (volumeFrom + Math.random() * (volumeTo - volumeFrom));
        this.pitch = (float) (pitchFrom + Math.random() * (pitchTo - pitchFrom));
        return this;
    }

    public <T extends TargetContext<?>> Button withClickHandler(Action<T> action, ContextConsumer<T> clickHandler) {
        this.clickHandler.put(action, clickHandler);
        return this;
    }

    /**
     * @param clickHandler a click handler to run
     * @param actions      all actions to run the click handler for
     * @return the builder instance
     */
    public Button withClickHandler(ContextConsumer<? extends TargetContext<?>> clickHandler, Action<?>... actions) {
        for (Action<?> action : actions) {
            this.clickHandler.put(action, clickHandler);
        }
        return this;
    }

    /**
     * @param clickHandler a map of click handlers for each action
     * @return the builder instance
     */
    public Button withClickHandler(Map<Action<?>, ContextConsumer<? extends TargetContext<?>>> clickHandler) {
        this.clickHandler.putAll(clickHandler);
        return this;
    }
}
