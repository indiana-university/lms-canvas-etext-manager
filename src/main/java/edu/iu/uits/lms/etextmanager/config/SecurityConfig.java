package edu.iu.uits.lms.etextmanager.config;

import edu.iu.uits.lms.common.it12logging.LmsFilterSecurityInterceptorObjectPostProcessor;
import edu.iu.uits.lms.common.it12logging.RestSecurityLoggingConfig;
import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import uk.ac.ox.ctl.lti13.Lti13Configurer;

import static edu.iu.uits.lms.lti.LTIConstants.BASE_USER_ROLE;
import static edu.iu.uits.lms.lti.LTIConstants.WELL_KNOWN_ALL;

@Configuration
public class SecurityConfig {


    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 5)
    public static class AppRestSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .cors().and()
                    .requestMatchers().antMatchers("/rest/**", "/api/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/rest/**")
                    .access("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')")
                    .antMatchers("/api/**").permitAll()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());

            http.apply(new RestSecurityLoggingConfig());
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4)
    public static class AppWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

        @Autowired
        private ETextService eTextUserService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers(WELL_KNOWN_ALL, "/error", "/app/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers(WELL_KNOWN_ALL, "/error").permitAll()
                    .antMatchers("/**").hasRole(BASE_USER_ROLE)
                    .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
                    .and()
                    .headers()
                    .contentSecurityPolicy("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com")
                    .and()
                    .referrerPolicy(referrer -> referrer
                            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN));


            //Setup the LTI handshake
            Lti13Configurer lti13Configurer = new Lti13Configurer()
                    .grantedAuthoritiesMapper(new CustomRoleMapper(defaultInstructorRoleRepository, eTextUserService));


            http.apply(lti13Configurer);

            //Fallback for everything else
            http.requestMatchers().antMatchers("/**")
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
                    .and()
                    .headers()
                    .contentSecurityPolicy("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com")
                    .and()
                    .referrerPolicy(referrer -> referrer
                            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN));
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore everything except paths specified
            web.ignoring().antMatchers("/app/jsrivet/**", "/app/webjars/**", "/app/css/**", "/app/js/**", "/favicon.ico");
        }

    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class CatchAllSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/**")
                    .and()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .withObjectPostProcessor(new LmsFilterSecurityInterceptorObjectPostProcessor())
                    .and()
                    .headers()
                    .contentSecurityPolicy("style-src 'self' 'unsafe-inline'; form-action 'self'; frame-ancestors 'self' https://*.instructure.com")
                    .and()
                    .referrerPolicy(referrer -> referrer
                            .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN));
        }
    }
}
