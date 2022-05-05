package de.cubbossa.menuframework.chat;

import de.cubbossa.menuframework.util.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import javax.annotation.Nullable;

public class TextMenu extends ChatMenu<String> {

	@Getter
	@Setter
	private ClickEvent.Action action = null;

	@Getter
	@Setter
	private String actionString = "";

	@Getter
	@Setter
	private String description;

	public TextMenu(String message) {
		super(message);
	}

	public TextMenu(String message, ClickEvent.Action action, String actionString) {
		super(message);
		this.action = action;
		this.actionString = actionString;
	}

	public TextMenu(String message, String description) {
		super(message);
		this.description = description;
	}

	public TextMenu(String message, ClickEvent.Action action, String actionString, @Nullable String description) {
		super(message);
		this.action = action;
		this.actionString = actionString;
		this.description = description;
	}

	public Component toComponent(String message) {
		Component localComponent = ChatUtils.fromLegacy(message);
		if (action != null) {
			localComponent = localComponent.clickEvent(ClickEvent.clickEvent(action, actionString));
		}
		if (description != null) {
			localComponent = localComponent.hoverEvent(HoverEvent.showText(ChatUtils.fromLegacy(description)));
		}
		return localComponent;
	}

}
