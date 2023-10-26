package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.manager.commands.SubCommandExecutor;

public class TradeExecutor extends SubCommandExecutor {
    public TradeExecutor() {
        super("trade", null);
        registerCommands();
    }

    public void registerCommands() {
        setDefaultSubCommand(new TradeAskCommand(), true);
        addSubCommand(new TradeCancelCommand());
    }
}
