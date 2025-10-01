package ueg.diario_de_obra_digital_backend.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ueg.diario_de_obra_digital_backend.service.exception.FileNotFoundInStorageException;
import ueg.diario_de_obra_digital_backend.service.exception.FileStorageCreationException;
import ueg.diario_de_obra_digital_backend.service.exception.InvalidFileName;

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
}