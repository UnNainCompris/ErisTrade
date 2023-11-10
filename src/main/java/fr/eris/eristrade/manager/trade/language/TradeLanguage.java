package fr.eris.eristrade.manager.trade.language;

import com.google.gson.annotations.Expose;
import fr.eris.erisutils.manager.language.data.Language;
import fr.eris.erisutils.manager.language.data.LanguageValue;
import lombok.Getter;

public class TradeLanguage extends Language {

    @Expose @Getter private LanguageValue errorOccur = new LanguageValue("&c[x] &7An error occur please check console for more information !");
    @Expose @Getter private LanguageValue attempting = new LanguageValue("&1[?] &7Attempting to %action% !");
    @Expose @Getter private LanguageValue successfully = new LanguageValue("&a[o] &7Successfully %action% !");
    @Expose @Getter private LanguageValue enterNewValue = new LanguageValue("&3[!] &7Enter new value for %reason% !");
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
