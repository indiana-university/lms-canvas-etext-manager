package edu.iu.uits.lms.etextmanager.service;

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
