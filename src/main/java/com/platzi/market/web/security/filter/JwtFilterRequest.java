package com.platzi.market.web.security.filter;

import com.platzi.market.domain.service.PlatziUserDetailsService;
import com.platzi.market.web.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilterRequest extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PlatziUserDetailsService platziUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader( "Authorization" );
        if ( authHeader != null && authHeader.startsWith("Bearer") ) {

            String jwt = authHeader.substring(7);
            String username = jwtUtil.extractUsername( jwt );

            // Check if actually isn't instanciated an Authorization request*
            if( username != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
                UserDetails userDetails = platziUserDetailsService.loadUserByUsername( username );

                if( jwtUtil.validateToken( jwt, userDetails ) ) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( userDetails, null, userDetails.getAuthorities() );
                    authToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );

                    SecurityContextHolder.getContext().setAuthentication( authToken );
                }
            }
        }

        // Evaluates request and response through filter.
        filterChain.doFilter( request, response );
    }
}
