package fr.eris.eristrade.utils;

public class ProgressBar {
    private final String progressBarText;
    public ProgressBar(String progressBarText) {
        this.progressBarText = progressBarText;
    }

    public String getDone(double current, double max) {
        return progressBarText.substring(0, (int)Math.floor((current / max) * (double) progressBarText.length()));
    }

    public String getNotDoneYet(double current, double max) {
        return progressBarText.substring((int)Math.ceil((current / max) * (double) progressBarText.length()));
    }
}
