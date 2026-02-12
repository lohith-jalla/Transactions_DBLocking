package com.lohith.Lpay.configs;

import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.repos.UserRepo;
import com.lohith.Lpay.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepo userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No token found in request");
            filterChain.doFilter(req,res);
            return;
        }

        String token = authHeader.substring(7);

        String email=jwtService.extractUsername(token);
        if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null ){
            if(jwtService.validate(token)){
                User user =userRepository.findByEmail(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("JWT Validated for user: " + email);
            }
        }
        filterChain.doFilter(req,res);
    }
}
