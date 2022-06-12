package de.cubbossa.menuframework.testing;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageFile;
import de.cubbossa.translations.MessageMeta;

@MessageFile
public class Messages {

	@MessageMeta(value = "<#ff0000>Ein Prefix</#ff0000>")
	public static final Message PREFIX = new Message("prefix");
}
