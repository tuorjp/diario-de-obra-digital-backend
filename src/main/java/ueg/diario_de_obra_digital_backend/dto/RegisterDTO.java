package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import lombok.Setter;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

@Getter
@Setter
public class RegisterDTO {
  public String login;
  public String name;
  public String password;
  public UserRole role;
}
