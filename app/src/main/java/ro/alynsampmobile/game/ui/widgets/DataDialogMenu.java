package ro.alynsampmobile.game.ui.widgets;

public class DataDialogMenu {
    private final int id;
    private final int imgDrawableButton;
    private final String nameButton;

    public DataDialogMenu(int id, int imgDrawableButton, String nameButton) {
        this.id = id;
        this.imgDrawableButton = imgDrawableButton;
        this.nameButton = nameButton;
    }

    public int getId() {
        return id;
    }

    public int getImgDrawableButton() {
        return imgDrawableButton;
    }

    public String getNameButton() {
        return nameButton;
    }
}