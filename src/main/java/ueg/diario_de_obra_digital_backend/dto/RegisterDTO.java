package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

@Data
public class RegisterDTO {
    private String name;
    private String login;
    private String password;
    private UserRole role;

    // Dados Pessoais / Profissionais
    private String phone1;
    private String phone2;
    private String crea;
    private String creaUf;
    private String cpf;

    // Endereço
    private String zipCode;
    private String city;
    private String state;
    private String address;
    private String addressNumber;
    private String complement;
}