package edu.iu.uits.lms.etextmanager.config;

import edu.iu.uits.lms.etextmanager.model.ETextUser;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class CustomRoleMapper extends LmsDefaultGrantedAuthoritiesMapper {

   private ETextService eTextService;

   public CustomRoleMapper(DefaultInstructorRoleRepository defaultInstructorRoleRepository, ETextService eTextService) {
      super(defaultInstructorRoleRepository);
      this.eTextService = eTextService;
   }

   @Override
   public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
      List<GrantedAuthority> remappedAuthorities = new ArrayList<>();
      remappedAuthorities.addAll(authorities);
      for (GrantedAuthority authority : authorities) {
         OidcUserAuthority userAuth = (OidcUserAuthority) authority;
         OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(userAuth.getAttributes());
         log.debug("LTI Claims: {}", userAuth.getAttributes());

         String userId = oidcTokenUtils.getUserLoginId();

         String rolesString = "NotAuthorized";

         ETextUser user = eTextService.findByUsername(userId);

         if (user != null) {
            rolesString = LTIConstants.CANVAS_INSTRUCTOR_ROLE;
         }

         String[] userRoles = rolesString.split(",");

         String newAuthString = returnEquivalentAuthority(userRoles, getDefaultInstructorRoles());

         OidcUserAuthority newUserAuth = new OidcUserAuthority(newAuthString, userAuth.getIdToken(), userAuth.getUserInfo());
         remappedAuthorities.add(newUserAuth);
      }

      return remappedAuthorities;
   }
}
