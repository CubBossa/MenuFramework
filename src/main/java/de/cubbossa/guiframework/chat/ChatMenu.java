package de.cubbossa.guiframework.chat;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ChatMenu<T> {

	public static final Component INDENT_COMPONENT = Component.text("\u00BB");

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
		if (menu.equals(this)) {
			return this;
		}
		subMenus.add(menu);
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
		return toComponents(0, 0, Integer.MAX_VALUE);
	}

	public List<Component> toComponents(int page, int menusPerPage) {
		return toComponents(0, page, menusPerPage);
	}

	public List<Component> toComponents(int indentation, int page, int menusPerPage) {

		List<Component> components = Lists.newArrayList();
		components.add(toComponent(message));
		subMenus.forEach(subMenu -> components.addAll(subMenu.toComponents(indentation + 1, 0, Integer.MAX_VALUE)));
		return components.subList(page * menusPerPage, page * (menusPerPage + 1));
	}

	public Component indentation(int ind) {
		TextComponent.Builder componentBuilder = Component.text().content("");
		componentBuilder.append(Component.text(" ".repeat(ind)));
		componentBuilder.append(indentComponent == null ? INDENT_COMPONENT : indentComponent).append(Component.text("  "));
		return componentBuilder.build();
	}

	public void send(Player player) {
		toComponents(0, Integer.MAX_VALUE).forEach(player::sendMessage);
	}

	public void send(Player player, int page, int linesPerPage) {
		toComponents(page, linesPerPage).forEach(player::sendMessage);
	}
}
