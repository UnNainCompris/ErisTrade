package fr.eris.eristrade.manager.config;

import com.google.gson.annotations.Expose;
import fr.eris.erisutils.manager.config.data.Config;
import fr.eris.erisutils.manager.config.data.value.ConfigBoolean;
import fr.eris.erisutils.manager.config.data.value.ConfigString;
import fr.eris.erisutils.manager.config.data.value.ConfigValue;
import fr.eris.erisutils.manager.config.data.value.number.ConfigDouble;
import fr.eris.erisutils.manager.config.data.value.number.ConfigInteger;

public class TestConfig extends Config {

    @Expose private final ConfigBoolean testB = new ConfigBoolean(true, "Test1B", "Test2B", "Test3B");
    @Expose private final ConfigInteger testI = new ConfigInteger(1, "Test1I", "Test2I", "Test3I");
    @Expose private final ConfigDouble testD = new ConfigDouble(1.01, "Test1D", "Test2D", "Test3D");
    @Expose private final ConfigString testS = new ConfigString("TestValue", "Test1S", "Test2S", "Test3S");
    
    @Override
    public String getConfigFileName() {
        return "TestConfig";
    }

    @Override
    public void onLoad() {
        System.out.println(getConfigFileName() + " was loaded !");
    }

    @Override
    public <T> void onValueGet(ConfigValue<T> configValue) {
        System.out.println("The value " + configValue + " was get");
    }

    @Override
    public <T> void onValueEdit(T oldValue, T newValue, ConfigValue<T> configValue) {
        System.out.println("The value " + configValue + " was edit [" + oldValue + " -> " + newValue + "]");
    }
}
