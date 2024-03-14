package edu.iu.uits.lms.etextmanager.controller.rest;

import edu.iu.uits.lms.etextmanager.model.ETextUser;
import edu.iu.uits.lms.etextmanager.repository.ETextUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/etext_user2")
@Tag(name = "ETextUserRestController", description = "Operations involving the ETextUser table")
@Slf4j
@Deprecated
public class ETextUserRestController {

   @Autowired
   private ETextUserRepository eTextUserRepository;

   @GetMapping("/{id}")
   @Operation(summary = "Get ETextUser by id")
   public ETextUser getFromId(@PathVariable Long id) {
      return eTextUserRepository.findById(id).orElse(null);
   }

   @GetMapping("/username/{username}")
   @Operation(summary = "Get ETextUser by username")
   public ETextUser getByUsername(@PathVariable String username) {
      return eTextUserRepository.findByUsername(username);
   }

   @GetMapping("/all")
   @Operation(summary = "Get all ETextUsers")
   public List<ETextUser> getAll() {
      return (List<ETextUser>) eTextUserRepository.findAll();
   }

   @PutMapping("/{id}")
   @Operation(summary = "Update an existing ETextUser by id")
   public ETextUser update(@PathVariable Long id, @RequestBody ETextUser user) {
      ETextUser updatingUser = eTextUserRepository.findById(id).orElse(null);

      if (user.getUsername() != null) {
         updatingUser.setUsername(user.getUsername());
      }
      if (user.getEmail() != null) {
         updatingUser.setEmail(user.getEmail());
      }
      if (user.getDisplayName() != null) {
         updatingUser.setDisplayName(user.getDisplayName());
      }
      return eTextUserRepository.save(updatingUser);
   }

   @PostMapping("/")
   @Operation(summary = "Create a new ETextUser")
   public ETextUser create(@RequestBody ETextUser user) {
      ETextUser newUser = new ETextUser();
      newUser.setUsername(user.getUsername());
      newUser.setDisplayName(user.getDisplayName());
      newUser.setEmail(user.getEmail());
      return eTextUserRepository.save(newUser);
   }

   @DeleteMapping("/{id}")
   @Operation(summary = "Delete a ETextUser by id")
   public String delete(@PathVariable Long id) {
      eTextUserRepository.deleteById(id);
      return "Delete success.";
   }
}
