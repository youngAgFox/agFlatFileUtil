package ag.flatfile.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class XmlBuilder {

    private String tag;
    private final StringBuilder text = new StringBuilder();
    private final HashMap<String, String> attributes = new HashMap<>();
    private final List<XmlBuilder> children = new ArrayList<>();
    private XmlBuilder parent;

    public XmlBuilder(String tag) {
        this(tag, null);
    }

    private XmlBuilder(String tag, XmlBuilder parent) {
        this.tag = tag;
        this.parent = parent;
    }

    public XmlBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public XmlBuilder appendText(String text) {
        this.text.append(text);
        return this;
    }

    public XmlBuilder setAttribute(String attribute, String value) {
        attributes.put(attribute, value);
        return this;
    }

    public XmlBuilder addChild(String tag) {
        XmlBuilder child = new XmlBuilder(tag, this);
        children.add(child);
        return this;
    }

    public XmlBuilder getChild(int index) {
        return children.get(index);
    }

    public XmlBuilder getLastChild() {
        return getChild(children.size() - 1);
    }

    public XmlBuilder getParent() {
        return parent;
    }

    public XmlBuilder setParent(XmlBuilder stepParent) {
        if (null == stepParent) {
            throw new NullPointerException("setParent() stepParent cannot be null");
        }
        if (null != parent) {
            parent.children.remove(this);
        }
        stepParent.children.add(this);
        return this;
    }

    public XmlBuilder getRoot() {
        XmlBuilder root = this;
        while (null != root.parent) {
            root = root.parent;
        }
        return root;
    }

    public Entity build() {
        Entity e = new Entity(tag, attributes);
        List<Entity> childEntities = children.stream().map(XmlBuilder::build).collect(Collectors.toList());
        e.getChildren().addAll(childEntities);
        e.setText(text.toString());
        return e;
    }

}
