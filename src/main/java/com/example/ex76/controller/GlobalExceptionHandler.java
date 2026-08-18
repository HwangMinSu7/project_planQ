package com.example.ex76.controller;

import com.example.ex76.exception.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String notFound(NotFoundException e, Model model) {
    setError(model, 404, "페이지를 찾을 수 없습니다.", e.getMessage());
    return "error/common";
  }

  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String noResource(Model model) {
    setError(model, 404, "페이지를 찾을 수 없습니다.", "요청한 주소를 다시 확인해 주세요.");
    return "error/common";
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String badRequest(Model model) {
    setError(model, 400, "잘못된 요청입니다.", "주소에 전달된 값을 다시 확인해 주세요.");
    return "error/common";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String invalidValue(IllegalArgumentException e, Model model) {
    setError(model, 400, "잘못된 요청입니다.", e.getMessage());
    return "error/common";
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String forbidden(Model model) {
    setError(model, 403, "접근할 수 없습니다.", "이 기능을 사용할 권한이 없습니다.");
    return "error/common";
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String serverError(Exception e, Model model) {
    log.error("처리하지 못한 오류가 발생했습니다.", e);
    setError(model, 500, "잠시 문제가 생겼습니다.", "잠시 후 다시 시도해 주세요.");
    return "error/common";
  }

  private void setError(Model model, int status, String title, String message) {
    model.addAttribute("status", status);
    model.addAttribute("title", title);
    model.addAttribute("message", message);
  }
}
