package fr.eris.eristrade.utils;

import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageBuilder {

    private List<TextComponent> builder;

    private MessageBuilder() {
        builder = new ArrayList<>();
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public MessageBuilder addClickEvent(String messageToAdd, ClickEvent.Action clickAction, String clickActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setClickEvent(new ClickEvent(clickAction, clickActionValue));
        appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addHoverEvent(String messageToAdd, HoverEvent.Action hoverAction, List<BaseComponent> hoverActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setHoverEvent(new HoverEvent(hoverAction, hoverActionValue.toArray(new BaseComponent[]{})));
        appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addClickAndHoverEvent(String messageToAdd, ClickEvent.Action clickAction, String clickActionValue,
                                                HoverEvent.Action hoverAction, List<BaseComponent> hoverActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setHoverEvent(new HoverEvent(hoverAction, hoverActionValue.toArray(new BaseComponent[]{})));
        newText.setClickEvent(new ClickEvent(clickAction, clickActionValue));
        appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addText(String messageToAdd) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        appendToBuilder(newText);
        return this;
    }

    public MessageBuilder sendMessage(Player player) {
        player.spigot().sendMessage(builder.toArray(new BaseComponent[]{}));
        return this;
    }

    public MessageBuilder sendMessage(Collection<Player> players) {
        for(Player player : players) player.spigot().sendMessage(builder.toArray(new BaseComponent[]{}));
        return this;
    }

    public MessageBuilder reset() {
        this.builder = new ArrayList<>();
        return this;
    }

    public MessageBuilder reset(String defaultText) {
        this.builder = new ArrayList<TextComponent>(){{add(new TextComponent(defaultText));}};
        return this;
    }

    public void appendToBuilder(TextComponent toAppend) {
        builder.add(toAppend);
    }

}
