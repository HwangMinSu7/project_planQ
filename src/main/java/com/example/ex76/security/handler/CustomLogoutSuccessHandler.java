package com.example.ex76.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) throws IOException, ServletException {
    // request: 요청, response: 응답, authentication: 인증
    if (authentication != null || authentication.getPrincipal() != null) {
      request.getSession().invalidate();
    }
    response.setStatus(HttpServletResponse.SC_OK);
    response.sendRedirect(request.getContextPath());
  }
}
