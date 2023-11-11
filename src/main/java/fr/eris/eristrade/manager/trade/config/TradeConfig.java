package fr.eris.eristrade.manager.trade.config;

import com.google.gson.annotations.Expose;
import fr.eris.erisutils.manager.config.data.Config;
import fr.eris.erisutils.manager.config.data.value.ConfigBoolean;
import fr.eris.erisutils.manager.config.data.value.ConfigValue;
import fr.eris.erisutils.manager.config.data.value.number.ConfigDouble;
import fr.eris.erisutils.manager.config.data.value.number.ConfigInteger;
import lombok.Getter;

public class TradeConfig extends Config {

    @Getter @Expose private final ConfigInteger maxItemInTrade = new ConfigInteger(-1, "&7-1 for no limit !");
    @Getter @Expose private final ConfigDouble maxMoneyInTrade = new ConfigDouble(-1.0, "&7-1 for no limit !");
    @Getter @Expose private final ConfigBoolean isMoneyInTrade = new ConfigBoolean(true, "&7On true this will allow the player to trade money if the server has Vault.");

    @Getter @Expose private final ConfigInteger maxExperienceInTrade = new ConfigInteger(-1, "&7-1 for no limit !");
    @Getter @Expose private final ConfigBoolean isExperienceInTrade = new ConfigBoolean(true, "&7On true this will allow the player to trade money if the server has Vault.");

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
