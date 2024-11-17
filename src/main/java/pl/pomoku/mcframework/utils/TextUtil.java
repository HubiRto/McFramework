package pl.pomoku.mcframework.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextUtil {
    public static Component textToComponent(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
