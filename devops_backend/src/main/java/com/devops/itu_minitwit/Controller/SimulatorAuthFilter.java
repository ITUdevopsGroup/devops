package com.devops.itu_minitwit.Controller;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimulatorAuthFilter implements Filter {

  private static final String EXPECTED = "Basic c2ltdWxhdG9yOnN1cGVyX3NhZmUh";

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String path = request.getRequestURI();

    boolean protectedPath = path.startsWith("/msgs") || path.startsWith("/fllws");

    if (protectedPath) {
      String auth = request.getHeader("Authorization");
      if (auth == null || !auth.equals(EXPECTED)) {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"status\":403,\"error_msg\":\"You are not authorized to use this resource!\"}"
        );
        return;
      }
    }

    chain.doFilter(req, res);
  }
}
