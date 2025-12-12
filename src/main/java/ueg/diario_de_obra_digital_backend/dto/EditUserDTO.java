package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

import java.time.LocalDate;

@Data
public class EditUserDTO {
    private Long id;
    private String login;
    private String name;
    private String password; // Opcional: tratar null no service se não for alterar
    private UserRole role;

    private String phone1;
    private String phone2;
    private String crea;
    private String creaUf;
    private String cpf;
    private LocalDate admissionDate;
    private boolean enabled;

    private String address;
    private String addressNumber;
    private String zipCode;
    private String complement;
    private String city;
    private String state;
}