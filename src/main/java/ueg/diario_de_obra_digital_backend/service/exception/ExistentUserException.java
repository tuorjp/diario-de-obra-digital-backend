package ueg.diario_de_obra_digital_backend.service.exception;

import lombok.Getter;
import ueg.diario_de_obra_digital_backend.model.User;

import java.util.List;

@Getter
public class ExistentUserException extends RuntimeException {
    private final User user;
    // Ajustado para LISTA para suportar múltiplos conflitos (ex: Login e CPF)
    private final List<String> conflictFields;

    public ExistentUserException(String message, User user, List<String> conflictFields) {
        super(message);
        this.user = user;
        this.conflictFields = conflictFields;
    }
}