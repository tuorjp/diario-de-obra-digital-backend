package ueg.diario_de_obra_digital_backend.service.exception;

public class FileNotFoundInStorageException extends RuntimeException {
  public FileNotFoundInStorageException(String message) {
    super(message);
  }
}
