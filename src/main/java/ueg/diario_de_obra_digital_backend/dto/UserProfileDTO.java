package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.User;

import java.time.LocalDate;

@Data
public class UserProfileDTO {
    private Long id;
    private String name;
    private String login;
    private UserRole role;

    private String phone1;
    private String phone2;
    private String crea;
    private String creaUf;
    private String cpf;
    private LocalDate creationDate;
    private String status;

    private String address;
    private String addressNumber;
    private String zipCode;
    private String complement;
    private String city;
    private String state;

    public UserProfileDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.login = user.getLogin();
        this.role = user.getRole();
        this.phone1 = user.getPhone1();
        this.phone2 = user.getPhone2();
        this.crea = user.getCrea();
        this.creaUf = user.getCreaUf();
        this.cpf = user.getCpf();
        this.creationDate = user.getCreationDate();
        this.status = user.getStatus();
        this.address = user.getAddress();
        this.addressNumber = user.getAddressNumber();
        this.zipCode = user.getZipCode();
        this.complement = user.getComplement();
        this.city = user.getCity();
        this.state = user.getState();
    }
}