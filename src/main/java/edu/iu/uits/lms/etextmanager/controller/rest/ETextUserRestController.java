package edu.iu.uits.lms.etextmanager.controller.rest;

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
