package fr.eris.eristrade.utils;

import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageBuilder {
    private List<TextComponent> builder = new ArrayList();

    private MessageBuilder() {
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public MessageBuilder addClickEvent(String messageToAdd, ClickEvent.Action clickAction, String clickActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setClickEvent(new ClickEvent(clickAction, clickActionValue));
        this.appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addHoverEvent(String messageToAdd, HoverEvent.Action hoverAction, List<BaseComponent> hoverActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setHoverEvent(new HoverEvent(hoverAction, hoverActionValue.toArray(new BaseComponent[0])));
        this.appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addClickAndHoverEvent(String messageToAdd, ClickEvent.Action clickAction, String clickActionValue, HoverEvent.Action hoverAction, List<BaseComponent> hoverActionValue) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        newText.setHoverEvent(new HoverEvent(hoverAction, hoverActionValue.toArray(new BaseComponent[0])));
        newText.setClickEvent(new ClickEvent(clickAction, clickActionValue));
        this.appendToBuilder(newText);
        return this;
    }

    public MessageBuilder addText(String messageToAdd) {
        TextComponent newText = new TextComponent(ColorUtils.translate(messageToAdd));
        this.appendToBuilder(newText);
        return this;
    }

    public MessageBuilder sendMessage(Player player) {
        player.spigot().sendMessage(this.builder.toArray(new BaseComponent[0]));
        return this;
    }

    public MessageBuilder sendMessage(Collection<Player> players) {
        for(Player player : players) {
            player.spigot().sendMessage(this.builder.toArray(new BaseComponent[0]));
        }
        return this;
    }

    public MessageBuilder reset() {
        this.builder = new ArrayList();
        return this;
    }

    public MessageBuilder reset(String defaultText) {
        this.builder.clear();
        this.builder.add(new TextComponent(defaultText));
        return this;
    }

    public void appendToBuilder(TextComponent toAppend) {
        this.builder.add(toAppend);
    }
}
