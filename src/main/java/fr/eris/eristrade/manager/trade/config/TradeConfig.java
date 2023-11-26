package fr.eris.eristrade.manager.trade.config;

import com.google.gson.annotations.Expose;
import fr.eris.erisutils.manager.config.data.Config;
import fr.eris.erisutils.manager.config.data.value.ConfigBoolean;
import fr.eris.erisutils.manager.config.data.value.ConfigList;
import fr.eris.erisutils.manager.config.data.value.ConfigString;
import fr.eris.erisutils.manager.config.data.value.ConfigValue;
import fr.eris.erisutils.manager.config.data.value.number.ConfigDouble;
import fr.eris.erisutils.manager.config.data.value.number.ConfigInteger;
import lombok.Getter;

import java.util.ArrayList;

public class TradeConfig extends Config {

    @Getter @Expose private final ConfigInteger maxItemInTrade = new ConfigInteger(-1, "&7-1 for no limit !");
    @Getter @Expose private final ConfigDouble maxMoneyInTrade = new ConfigDouble(-1.0, "&7-1 for no limit !");
    @Getter @Expose private final ConfigBoolean isMoneyInTrade = new ConfigBoolean(true, "&7On true this will allow the player to trade money if the server has Vault.");

    @Getter @Expose private final ConfigInteger maxExperienceInTrade = new ConfigInteger(-1, "&7-1 for no limit !");
    @Getter @Expose private final ConfigBoolean isExperienceInTrade = new ConfigBoolean(true, "&7On true this will allow the player to trade money if the server has Vault.");

    @Getter @Expose private final ConfigBoolean allowDifferentWorldTrading = new ConfigBoolean(false, "&7If set to true player that are not in same world can trade between each other ! (This will bypass the distance between player if not in same world)");
    @Getter @Expose private final ConfigDouble distanceBetweenPlayer = new ConfigDouble(50.0d, "&7-1 for no limit !");
    @Getter @Expose private final ConfigString disabledInWorld = new ConfigString("", "&7Place world name here to disable trade in targeted world ! (Example: \"world;world_the_end\"");

    @Override
    public String getConfigFileName() {
        return "tradeConfig";
    }

    @Override
    public void onLoad() {

    }

    @Override
    public <T> void onValueGet(ConfigValue<T> configValue) {

    }

    @Override
    public <T> void onValueEdit(T t, T t1, ConfigValue<T> configValue) {

    }
}
