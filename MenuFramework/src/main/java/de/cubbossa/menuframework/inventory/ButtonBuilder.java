package de.cubbossa.menuframework.inventory;

import de.cubbossa.menuframework.GUIHandler;
import de.cubbossa.menuframework.Panel;
import de.cubbossa.menuframework.inventory.context.ContextConsumer;
import de.cubbossa.menuframework.inventory.context.TargetContext;
import de.cubbossa.menuframework.util.ChatUtils;
import de.cubbossa.menuframework.util.ItemStackUtils;
import lombok.Getter;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ButtonBuilder {

    private Supplier<ItemStack> stackSupplier;
    private Sound sound;
    private float pitch = 1f;
    private float volume = .8f;
    private final Map<Class<? extends Event>, Collection<Consumer<? extends Event>>> clickHandler = new HashMap<>();

    public static ButtonBuilder builder() {
        return new ButtonBuilder();
    }

    public <E extends Event> Panel build() {
        Panel panel = new SingleItemPanel(stackSupplier);
        panel.addListener(InventoryClickEvent.class, inventoryClickEvent -> {
            if (inventoryClickEvent.getWhoClicked() instanceof Player player) {
                player.playSound(player.getLocation(), sound, pitch, volume);
            }
        });
        clickHandler.forEach((aClass, consumers) -> {
            consumers.forEach(consumer -> panel.addListener((Class<E>) aClass, (Consumer<E>) consumer));
        });
        return panel;
    }

    public ButtonBuilder withItemStack(Supplier<ItemStack> stackSupplier) {
        this.stackSupplier = stackSupplier;
        return this;
    }

    /**
     * @param stack the icon itemstack
     * @return the builder instance
     */
    public ButtonBuilder withItemStack(ItemStack stack) {
        this.stackSupplier = () -> stack;
        return this;
    }

    /**
     * @param material the material of the icon
     * @return the builder instance
     */
    public ButtonBuilder withItemStack(Material material) {
        this.stackSupplier = () -> new ItemStack(material);
        return this;
    }

    /**
     * @param material the material of the icon
     * @param name     the name component of the icon
     * @return the builder instance
     */
    public ButtonBuilder withItemStack(Material material, Component name) {
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
    public ButtonBuilder withItemStack(Material material, Component name, List<Component> lore) {
        stackSupplier = () -> ItemStackUtils.createItemStack(material, name, lore);
        return this;
    }

    /**
     * Creates a player head item stack
     *
     * @param playerHeadOwner the skin to apply to the player head
     * @return the builder instance
     */
    public ButtonBuilder withItemStack(Player playerHeadOwner) {
        return withItemStack(playerHeadOwner, null);
    }

    /**
     * Creates a player head item stack
     *
     * @param playerHeadOwner the skin to apply to the player head
     * @param lore            the lore to add to the player head item
     * @return the builder instance
     */
    public ButtonBuilder withItemStack(Player playerHeadOwner, @Nullable List<Component> lore) {
        stackSupplier = () -> ItemStackUtils.createCustomHead(playerHeadOwner, GUIHandler.getInstance()
                .getAudiences().player(playerHeadOwner).getOrDefault(Identity.DISPLAY_NAME, Component.text(playerHeadOwner.getName())), lore);
        return this;
    }

    /**
     * @param sound a {@link Sound} to play when clicked
     * @return the builder instance
     */
    public ButtonBuilder withSound(Sound sound) {
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
    public ButtonBuilder withSound(Sound sound, float volume, float pitch) {
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
    public ButtonBuilder withSound(Sound sound, float volumeFrom, float volumeTo, float pitchFrom, float pitchTo) {
        this.sound = sound;
        this.volume = (float) (volumeFrom + Math.random() * (volumeTo - volumeFrom));
        this.pitch = (float) (pitchFrom + Math.random() * (pitchTo - pitchFrom));
        return this;
    }

    public <E extends Event> ButtonBuilder withClickHandler(Class<E> action, Consumer<E> clickHandler) {
        this.clickHandler.computeIfAbsent(action, aClass -> new LinkedList<>()).add(clickHandler);
        return this;
    }
}
