import ag.flatfile.xml.Entity;
import ag.flatfile.xml.XmlParser;
import org.junit.jupiter.api.Test;

public class XmlMapTest {

    @Test
    public void testXmlMap() throws Exception {
        XmlParser parser = new XmlParser();
        Entity root = parser.parse("test_res/test.xml");
    }
}
