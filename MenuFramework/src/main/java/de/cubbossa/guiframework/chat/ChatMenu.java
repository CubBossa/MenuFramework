package de.cubbossa.guiframework.chat;

import com.google.common.collect.Lists;
import de.cubbossa.guiframework.GUIHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ChatMenu<T> implements ComponentLike {

    @Setter
    public static Component INDENT_COMPONENT = Component.text("\u00BB");

    @Getter
    @Setter
    protected T message;
    protected final List<ChatMenu<?>> subMenus = Lists.newArrayList();

    @Getter
    @Setter
    protected @Nullable Component indentComponent = null;

    public ChatMenu(T message) {
        this.message = message;
    }

    public ChatMenu<T> addSub(ChatMenu<?> menu) {
        if (!menu.equals(this)) {
            subMenus.add(menu);
        }
        return this;
    }

    public boolean removeSub(ChatMenu<?> menu) {
        return subMenus.remove(menu);
    }

    public void clearSubs() {
        subMenus.clear();
    }

    public List<ChatMenu<?>> getSubs() {
        return subMenus;
    }

    public boolean hasSubs() {
        return !subMenus.isEmpty();
    }

    public abstract Component toComponent(T message);

    public List<Component> toComponents() {
        return toComponents(-1, 0, 1024);
    }

    public List<Component> toComponents(int page, int menusPerPage) {
        return toComponents(-1, page, menusPerPage);
    }

    public List<Component> toComponents(int indentation, int page, int menusPerPage) {

        List<Component> components = Lists.newArrayList();
        components.add(indentation(indentation).append(toComponent(message)));
        subMenus.forEach(subMenu -> components.addAll(subMenu.toComponents(indentation + 1, 0, 1024)));
        return components.subList(Integer.min(page * menusPerPage, components.size() - 1), Integer.min(page * menusPerPage + menusPerPage, components.size()));
    }

    public Component indentation(int ind) {
        if (ind < 0) {
            return Component.empty();
        }
        TextComponent.Builder componentBuilder = Component.text().content("");
        componentBuilder.append(Component.text(" ".repeat(ind)));
        componentBuilder.append(indentComponent == null ? INDENT_COMPONENT : indentComponent).append(Component.text(" "));
        return componentBuilder.build();
    }

    public void send(Player player) {
        Audience audience = GUIHandler.getInstance().getAudiences().player(player);
        toComponents(0, 1024).forEach(audience::sendMessage);
    }

    public void send(Player player, int page, int linesPerPage) {
        Audience audience = GUIHandler.getInstance().getAudiences().player(player);
        toComponents(page, linesPerPage).forEach(audience::sendMessage);
    }

    @Override
    public @NotNull Component asComponent() {
        List<Component> components = toComponents();
        Component c = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            c = c.append(Component.newline()).append(components.get(i));
        }
        return c;
    }
}