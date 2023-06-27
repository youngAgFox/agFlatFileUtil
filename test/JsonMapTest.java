import ag.flatfile.json.JsonParser;
import ag.flatfile.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class JsonMapTest {

    @Test
    public void testJson() {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject json = jsonParser.parse("test_res/test.json");
            System.out.println(json);
            System.out.println(json.get(0));
            System.out.println(json.get(1));
            System.out.println(json.get(1).getMember("City"));
            System.out.println(json.get(1).getMember("precision"));
            System.out.println(json.get(1).getMember("State"));
            System.out.println(json.get(1).getMember("Longitude"));
            System.out.println(json.get(0).get("People"));
            System.out.println(json.get(0).get("People").size());
            System.out.println(json.get(0).get("People").getMember(0));
            System.out.println(json.get(0).get("People").getMember(1));
            System.out.println(json.get(0).get("People").getMember(2));
            System.out.println(json.get(0).get("People").getMember(3));
        } catch (IOException e) {
            System.err.println("Failed to read json");
        }

    }
}
