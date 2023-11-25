package fr.eris.eristrade;

import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.manager.trade.TradeManager;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.ErisConfiguration;
import fr.eris.erisutils.ErisUtils;
import fr.eris.erisutils.manager.commands.CommandManager;
import fr.eris.erisutils.manager.config.ConfigManager;
import fr.eris.erisutils.manager.language.LanguageManager;
import fr.eris.erisutils.manager.playerdata.PlayerDataManager;
import fr.eris.erisutils.manager.playerdata.data.PlayerData;
import fr.eris.erisutils.utils.error.exception.ErisPluginException;
import fr.eris.erisutils.utils.manager.ManagerEnabler;
import fr.eris.erisutils.utils.manager.ManagerPriority;
import fr.eris.erisutils.utils.manager.Priority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

@ErisConfiguration(name = "ErisTrade", permissionPrefix = "eristrade", version = "0.5-BETA")
public class ErisTrade extends JavaPlugin {

    @Getter private static ErisTrade instance;
    @Getter private static ErisConfiguration configuration;
    @Getter private static boolean running;

    /* <Manager> */

    /* Is loaded in priority with ErisUtils.setup() */ @Getter private static CommandManager commandManager;
    /* Is loaded in priority with ErisUtils.setup() */ @Getter private static ConfigManager configManager;
    /* Is loaded in priority with ErisUtils.setup() */ @Getter private static LanguageManager languageManager;
    /* Is loaded in priority with ErisUtils.setup() */ @Getter private static PlayerDataManager<PlayerData> playerDataManager; // unused
    @ManagerPriority(initPriority = Priority.HIGH)  @Getter private static ImplementationManager implementationManager;
    @ManagerPriority(initPriority = Priority.NORMAL)  @Getter private static TradeManager tradeManager;

    public static void info(String info) {
        instance.getLogger().info(info);
    }

    public static void error(String error, Throwable throwable) {
        instance.getLogger().log(Level.SEVERE, error, throwable);
    }

    /* </Manager> */

    public void onEnable() {
        running = true;
        if(!vitalSetup()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            ErisUtils.setup(this);
            ErisUtils.getPluginLanguageManager().loadLanguage(TradeLanguage.class);
        } catch (ErisPluginException erisPluginException) {
            erisPluginException.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ManagerEnabler.init(this);
    }

    private boolean vitalSetup() {
        instance = this;
        ErisConfiguration configurationAnnotation = this.getClass().getAnnotation(ErisConfiguration.class);
        if(configurationAnnotation == null)
            return false;

        configuration = configurationAnnotation;
        return true;
    }

    public void onDisable() {
        running = false;
        ManagerEnabler.stop(this);
    }
}
