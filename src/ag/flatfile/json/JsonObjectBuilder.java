package ag.flatfile.json;

import java.util.HashMap;
import java.util.Map;

public class JsonObjectBuilder {
    Map<String, Object> values = new HashMap<>();
    private final JsonObject.Type type;

    JsonObjectBuilder(JsonObject.Type type) {
        this.type = type;
    }

    public JsonObjectBuilder add(String name, Object value) {
        validateIsObject();
        values.put(name, value);
        return this;
    }

    public JsonObjectBuilder add(int index, Object value) {
        validateIsArray();
        values.put("" + index, value);
        return this;
    }

    public JsonObjectBuilder add(Object value) {
        return add(values.size(), value);
    }

    public JsonObjectBuilder addArray(String name) {
        validateIsObject();
        values.put(name, new JsonObjectBuilder(JsonObject.Type.ARRAY));
        return this;
    }

    public JsonObjectBuilder addArray(int index) {
        validateIsArray();
        values.put("" + index, new JsonObjectBuilder(JsonObject.Type.ARRAY));
        return this;
    }

    public JsonObjectBuilder addArray() {
        return addArray(values.size());
    }

    public JsonObjectBuilder addObject(String name) {
        validateIsObject();
        values.put(name, new JsonObjectBuilder(JsonObject.Type.OBJECT));
        return this;
    }

    public JsonObjectBuilder addObject(int index) {
        validateIsArray();
        values.put("" + index, new JsonObjectBuilder(JsonObject.Type.OBJECT));
        return this;
    }

    public JsonObjectBuilder addObject() {
        return addObject(values.size());
    }

    public JsonObjectBuilder get(String name) {
        validateIsObject();
        return (JsonObjectBuilder) values.get(name);
    }

    public JsonObjectBuilder get(int index) {
        validateIsArray();
        return get("" + index);
    }

    public boolean isArray() {
        return JsonObject.Type.ARRAY == type;
    }

    public boolean isObject() {
        return JsonObject.Type.OBJECT == type;
    }

    public JsonObject build() {
        values.entrySet().stream()
                .filter(e -> e.getValue() instanceof JsonObjectBuilder)
                .forEach(this::buildAndReplace);
        return new JsonObject(type, values);
    }

    private void buildAndReplace(Map.Entry<String, Object> entry) {
        JsonObjectBuilder toBuild = (JsonObjectBuilder) entry.getValue();
        values.put(entry.getKey(), toBuild.build());
    }

    private void validateIsObject() {
        if (!isObject()) {
            throw new JsonTypeException("Cannot add a mapping to a non-object");
        }
    }

    private void validateIsArray() {
        if (!isArray()) {
            throw new JsonTypeException("Cannot add an element with index to a non-array");
        }
    }

}
