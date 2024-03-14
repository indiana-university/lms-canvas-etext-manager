package edu.iu.uits.lms.etextmanager.services;

import edu.iu.uits.lms.etextmanager.model.ETextCsv;
import edu.iu.uits.lms.etextmanager.service.CsvUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CsvParsingTest {

    @Test
    void testParseGoodCsv() throws IOException {
        InputStream fileStream = getFile("normal.csv");
        List<ETextCsv> data = CsvUtil.parseCsv(fileStream, ETextCsv.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(3, data.size());
        //"tool1","fancy name","fa1234","PB Title","https://example.com/foobar"
        Assertions.assertEquals("tool1", data.get(0).getTool());
        Assertions.assertEquals("fancy name", data.get(0).getNewName());
        Assertions.assertEquals("fa1234", data.get(0).getSisCourseId());
        Assertions.assertEquals("PB Title", data.get(0).getPressbookTitle());
        Assertions.assertEquals("https://example.com/foobar", data.get(0).getPressbookLink());
    }

    @Test
    void testParseDifferentOrderedCsv() throws IOException {
        InputStream fileStream = getFile("different_order.csv");
        List<ETextCsv> data = CsvUtil.parseCsv(fileStream, ETextCsv.class);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(3, data.size());
        //"fa1234","PB Title","https://example.com/foobar","tool1","fancy name"
        Assertions.assertEquals("tool1", data.get(0).getTool());
        Assertions.assertEquals("fancy name", data.get(0).getNewName());
        Assertions.assertEquals("fa1234", data.get(0).getSisCourseId());
        Assertions.assertEquals("PB Title", data.get(0).getPressbookTitle());
        Assertions.assertEquals("https://example.com/foobar", data.get(0).getPressbookLink());
    }

    @Test
    void testParseLowercasedCsv() throws IOException {
        //Columns aren't as expected, so will throw an exception
        try (InputStream fileStream = getFile("lower_case.csv")) {
            IOException t = Assertions.assertThrows(IOException.class, () ->
                    CsvUtil.parseCsv(fileStream, ETextCsv.class));
        }
    }

    private InputStream getFile(String fileName) {
        InputStream fileStream = this.getClass().getResourceAsStream("/uploads/" + fileName);
        return fileStream;
    }
}
