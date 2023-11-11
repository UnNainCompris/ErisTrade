package fr.eris.eristrade.manager.trade.language;

import com.google.gson.annotations.Expose;
import fr.eris.erisutils.manager.language.data.Language;
import fr.eris.erisutils.manager.language.data.LanguageValue;
import lombok.Getter;

public class TradeLanguage extends Language {

    @Expose @Getter private LanguageValue receivedTradeRequestCanceled = new LanguageValue("&3[!] &7The trade request from &6%requester% &7was canceled !");
    @Expose @Getter private LanguageValue sentTradeRequestCanceled = new LanguageValue("&3[!] &7The trade request you sent to &6%target% &7was canceled !");
    @Expose @Getter private LanguageValue sendTradeRequest = new LanguageValue("&a[o] &7Successfully send trade request to %target% !");
    @Expose @Getter private LanguageValue receiveTradeRequest = new LanguageValue("&a[!] &7You get a trade request from %requester% !");

    @Expose @Getter private LanguageValue cannotTradeYourself = new LanguageValue("&c[x] &7You can't trade with yourself !");
    @Expose @Getter private LanguageValue alreadyInvitedToTrade = new LanguageValue("&c[x] &7%target% is already invited to trade !");
    @Expose @Getter private LanguageValue targetNotConnected = new LanguageValue("&c[x] &7The player %target% is not connected !");
    @Expose @Getter private LanguageValue targetDoesntSendRequest = new LanguageValue("&c[x] &7%target% doesn't send you (or you sent to him) a trade request !");
    @Expose @Getter private LanguageValue currentTradeIsCanceled = new LanguageValue("&c[x] &7The trade with %target% was canceled !");
    @Expose @Getter private LanguageValue finishTrade = new LanguageValue("&a[O] &7You successfully finished the trade with %target%! &7(Check /trade log for more information)");
    @Expose @Getter private LanguageValue targetDontHaveEnoughInventorySpace = new LanguageValue("&c[x] &7%target% don't have enough inventory space !");
    @Expose @Getter private LanguageValue selfDontHaveEnoughInventorySpace = new LanguageValue("&c[x] &7You don't have enough inventory space !");
    @Expose @Getter private LanguageValue tradeInventoryName = new LanguageValue("&7Trade with: &6%target%");
    @Expose @Getter private LanguageValue tradeInstructionItemName = new LanguageValue("&7How to use ?");
    @Expose @Getter private LanguageValue tradeInstructionItemLore = new LanguageValue("&2Right Click &7; &aShift + Right Click &7; &dMiddle Click &7; &4Left Click &7; &cShift + Left Click\n&7In your inventory :  \n&7Add: &2Half Stack &7; &aHalf Total &7; &dA Stack &7; &41 Item ; &cTotal \n&7In the trade : \n  &7Remove: &2Half Stack &7; &aHalf Total &7; &dA Stack &7; &41 Item ; &cTotal");
    @Expose @Getter private LanguageValue cannotTradeNoting = new LanguageValue("&7You cannot trade nothing on both side !");
    @Expose @Getter private LanguageValue clickHereCancelTrade = new LanguageValue("&cClick here to cancel the trade !");
    @Expose @Getter private LanguageValue clickHereAcceptTrade = new LanguageValue("&aClick here to accept the trade !");
    @Expose @Getter private LanguageValue targetDontHaveAcceptTrade = new LanguageValue("&c%target% doesn't have accepted the trade !");
    @Expose @Getter private LanguageValue targetHaveAcceptTrade = new LanguageValue("&a%target% has accepted the trade !");
    @Expose @Getter private LanguageValue clickHereEditAmount = new LanguageValue("&8Click to edit the amount !");
    @Expose @Getter private LanguageValue dontHaveEnoughMoney = new LanguageValue("&7You don't have enough money !");
    @Expose @Getter private LanguageValue dontHaveEnoughExperience = new LanguageValue("&7You don't have enough experience !");
    @Expose @Getter private LanguageValue inputMoney = new LanguageValue("&7Input the money you want to trade");
    @Expose @Getter private LanguageValue inputExperience = new LanguageValue("&7Input the experience you want to trade");

    @Expose @Getter private LanguageValue acceptTradeRequestButton = new LanguageValue("&a&l[ACCEPT]");
    @Expose @Getter private LanguageValue cancelTradeRequestButton = new LanguageValue("&c&l[CANCEL]");

    @Expose @Getter private LanguageValue tradedMoneyItemName = new LanguageValue("&eMoney: %value%");
    @Expose @Getter private LanguageValue tradedExperienceItemName = new LanguageValue("&dExperience: %value%");


    @Override
    public String getLanguageFileName() {
        return "trade";
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onValueGet(LanguageValue configValue) {

    }

    @Override
    public void onValueEdit(String oldValue, String newValue, LanguageValue configValue) {

    }
}
