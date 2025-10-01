package ueg.diario_de_obra_digital_backend.service.exception;

public class InvalidFileName extends RuntimeException {
  public InvalidFileName(String message) {
    super(message);
  }
}