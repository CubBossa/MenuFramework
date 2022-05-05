package de.cubbossa.menuframework.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class PlayerContext extends Context {

	private final Player player;
}
