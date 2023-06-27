import ag.flatfile.csv.CsvMap;
import ag.logger.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class CSVMapTest {

    @Test
    public void testMap() throws IOException, ParseException {
        Logger.getDefaultLogger().setLevel(Logger.Level.DEBUG);
        CsvMap map = new CsvMap("test_res/test.csv");
        assertNotNull(map);
        assertEquals(4, map.columns());
        assertEquals(3, map.rows());
        assertEquals("a", map.get(0,0));
        assertEquals("a", map.get(0,"a"));
        assertEquals("testing a", map.get(1,0));
        assertEquals("testing a", map.get(1,"a"));
        assertEquals("t\"b\"2", map.get(2, "b"));
        assertThrows(NoSuchElementException.class, () -> map.get(2, "f"));
    }
}