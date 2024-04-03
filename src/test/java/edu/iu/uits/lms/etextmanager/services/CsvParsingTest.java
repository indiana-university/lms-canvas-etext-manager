package edu.iu.uits.lms.etextmanager.services;

/*-
 * #%L
 * etext-manager
 * %%
 * Copyright (C) 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
