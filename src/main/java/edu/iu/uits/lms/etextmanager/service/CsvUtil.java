package edu.iu.uits.lms.etextmanager.service;

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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvUtil {

   /**
    *
    * @param inputStream
    * @return
    * @throws IOException
    */
   public static <T> List<T> parseCsv(InputStream inputStream, Class<T> clazz) throws IOException {
      final CsvMapper mapper = new CsvMapper();
      CsvSchema headerSchema = CsvSchema.emptySchema().withHeader();

      try (MappingIterator<T> it = mapper.readerFor(clazz)
              .with(headerSchema)
              .readValues(inputStream)) {

         return it.readAll();
      }

   }

   /**
    * Write data to a new csv file
    * @param filePath
    * @param items
    * @param clazz
    * @param <T>
    * @throws IOException
    */
   public static <T> void write(String filePath, List<T> items, Class<T> clazz) throws IOException {
      write(filePath, items, clazz, false);
   }

   /**
    * Write data to a csv file, possibly appending to an existing file.
    * @param filePath
    * @param items
    * @param clazz
    * @param append If true, will append data to existing file and not include the headers (since they are likely already there)
    * @param <T>
    * @throws IOException
    */
   public static <T> void write(String filePath, List<T> items, Class<T> clazz, boolean append) throws IOException {
      // create mapper and schema
      CsvMapper mapper = new CsvMapper()
            .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
            .enable(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS);

      // Need to use a header and nulls will be replaced with a quoted empty string
      CsvSchema schema = mapper.schemaFor(clazz)
            .withUseHeader(!append)
            .withNullValue("\"\"");

      // output writer
      ObjectWriter myObjectWriter = mapper.writer(schema);
      File tempFile = new File(filePath);
      FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile, append);
      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
      OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);
      myObjectWriter.writeValue(writerOutputStream, items);
   }
}
