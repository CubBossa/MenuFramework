package de.cubbossa.guiframework.chat;

import net.kyori.adventure.text.Component;

public class ComponentMenu extends ChatMenu<Component> {

	public ComponentMenu(Component message) {
		super(message);
	}

	@Override
	public Component toComponent(Component message) {
		return message;
	}
}
