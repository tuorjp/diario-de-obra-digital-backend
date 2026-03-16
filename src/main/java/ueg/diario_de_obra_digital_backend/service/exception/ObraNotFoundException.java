package ueg.diario_de_obra_digital_backend.service.exception;

public class ObraNotFoundException extends RuntimeException {
    public ObraNotFoundException(String message) {
        super(message);
    }
}
