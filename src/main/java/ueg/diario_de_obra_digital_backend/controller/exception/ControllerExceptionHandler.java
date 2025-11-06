package ueg.diario_de_obra_digital_backend.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ueg.diario_de_obra_digital_backend.service.exception.FileNotFoundInStorageException;
import ueg.diario_de_obra_digital_backend.service.exception.FileStorageCreationException;
import ueg.diario_de_obra_digital_backend.service.exception.InvalidFileName;
import ueg.diario_de_obra_digital_backend.service.exception.UserNotFoundException;

import javax.naming.AuthenticationException;

@RestControllerAdvice
public class ControllerExceptionHandler {
  @ExceptionHandler(FileStorageCreationException.class)
  public ResponseEntity<String> handleFileStorageCreationException(FileStorageCreationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(FileNotFoundInStorageException.class)
  public ResponseEntity<String> handleFileNotFoundInStorageException(FileNotFoundInStorageException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(InvalidFileName.class)
  public ResponseEntity<String> handleInvalidFileName(InvalidFileName e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED) //401
  public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
    System.err.println("!!!!!!!!!! FALHA NA AUTENTICAÇÃO !!!!!!!!!!!");
    System.err.println("CAUSA: " + e.getClass().getSimpleName());
    System.err.println("MENSAGEM: " + e.getMessage());
    System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos.");
  }
}