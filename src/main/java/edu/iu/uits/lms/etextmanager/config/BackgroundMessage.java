package edu.iu.uits.lms.etextmanager.config;

import edu.iu.uits.lms.etextmanager.model.ETextCsv;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class BackgroundMessage implements Serializable {
   @NonNull
   private String username;
   private Set<FileGroup> fileGroup;


   @Data
   @AllArgsConstructor
   public static class FileGroup implements Serializable {
      private String fileName;
      private List<ETextCsv> fileContent;
   }
}
