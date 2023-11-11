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

    @Expose @Getter private LanguageValue acceptTradeButton = new LanguageValue("&a&l[ACCEPT]");
    @Expose @Getter private LanguageValue cancelTradeButton = new LanguageValue("&c&l[CANCEL]");

    @Expose @Getter private LanguageValue a = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue z = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue e = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue r = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue t = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue y = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue u = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue i = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue o = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue p = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue aa = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue zz = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ee = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue rr = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue tt = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue yy = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue uu = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ii = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue oo = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue pp = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue az = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ae = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ar = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue at = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ay = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue au = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ai = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ao = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ap = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue qa = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue qz = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue azdef = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue aefzec = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue zerf = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue btr = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue gbtrv = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue xegf = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue bht = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue zef = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue grer = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue ver = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue vtr = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue xzae = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue bgtr = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue vbrt = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue vrt = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue vtrr = new LanguageValue("&c&l[CANCEL]");
    @Expose @Getter private LanguageValue bvytr = new LanguageValue("&c&l[CANCEL]");




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
