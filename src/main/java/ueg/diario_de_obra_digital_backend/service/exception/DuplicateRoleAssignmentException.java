package ueg.diario_de_obra_digital_backend.service.exception;

public class DuplicateRoleAssignmentException extends RuntimeException {
    public DuplicateRoleAssignmentException(String message) {
        super(message);
    }
}
