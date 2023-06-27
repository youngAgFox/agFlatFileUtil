package ag.flatfile.xml;

import java.util.ArrayList;
import java.util.Map;

public class Entity {
    private final String tag;
    private final Map<String, String> attributes;
    private String text;
    private ArrayList<Entity> children;

    public Entity(String tag, Map<String, String> attributes) {
        this.tag = tag;
        this.attributes = attributes;
    }

    public ArrayList<Entity> getChildren() {
        return children;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getTag() {
        return tag;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
