package fr.eris.eristrade.manager.impl;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.manager.Manager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ImplementationManager extends Manager {

    @Getter private static Economy economy = null;

    public void start() {
        if(!setupEconomy()) {
            ErisTrade.info("Vault wasn't found ! (Economy in trade was disabled)");
        }
    }

    private boolean setupEconomy() {
        if (ErisTrade.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = ErisTrade.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
