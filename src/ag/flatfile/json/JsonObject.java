package ag.flatfile.json;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class JsonObject {
    private final Map<String, Object> values;
    private final Type type;

    public enum Type {
        OBJECT, ARRAY
    }

    public JsonObject(Type type, Map<String, Object> values) {
        if (null == type) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        this.type = type;
        this.values = values;
    }

    private void validateMember(String name) {
        if (!values.containsKey(name)) {
            throw new NoSuchElementException("Undefined member '" + name + "'");
        }
    }

    public Object getMember(String name) {
        if (Type.OBJECT != type) {
            throw new JsonTypeException("Cannot lookup named variables from an array. Must be an Object type.");
        }
        validateMember(name);
        return values.get(name);
    }

    public JsonObject get(String name) {
        return (JsonObject) getMember(name);
    }

    public Object getMember(int index) {
        if (Type.ARRAY != type) {
            throw new JsonTypeException("Cannot lookup indexed values from an Object. Must be an Array type.");
        }
        String mapIndex = "" + index;
        validateMember(mapIndex);
        return values.get(mapIndex);
    }

    public JsonObject get(int index) {
        return (JsonObject) getMember(index);
    }

    public boolean isArray() {
        return Type.ARRAY == type;
    }

    public boolean isObject() {
        return Type.OBJECT == type;
    }

    public Set<String> keys() {
        return values.keySet();
    }

    public int size() {
        return values.size();
    }

    public String toJson() {
        return toIndentedString(0);
    }

    @Override
    public String toString() {
        return toJson();
    }

    private String toIndentedString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            sb.append(entryToString(entry, indent + 3));
            sb.append(",\n");
        }
        int lastCommaPos = sb.lastIndexOf(",");
        if (lastCommaPos > 0) {
            sb.setLength(lastCommaPos);
        }
        return typeQuote(sb.toString(), indent);
    }

    private String entryToString(Map.Entry<String, Object> entry, int indent) {
        String str = " ".repeat(indent);
        if (Type.OBJECT == type) {
            str += '"' + entry.getKey() + "\": ";
        }
        Object value = entry.getValue();
        if (value instanceof JsonObject) {
            str += ((JsonObject) value).toIndentedString(indent);
        } else if (value instanceof String) {
            str += "\"" + value + "\"";
        } else {
            str += value;
        }
        return str;
    }

    private String typeQuote(String toQuote, int lineIndent) {
        if (Type.OBJECT == type) {
            return "{\n"
                    + toQuote + "\n"
                    + " ".repeat(lineIndent) + "}";
        }
        return "[\n"
                + toQuote + "\n"
                + " ".repeat(lineIndent) + "]";
    }
}
