package good.damn.textviewset.models;

public class Text {
    private String text;
    private float width;

    public Text(String text, float width) {
        this.text = text;
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public String getText() {
        return text;
    }
}
