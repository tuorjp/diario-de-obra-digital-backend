package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

@Data
public class EditUserDTO {
  public String login;
  public String name;
  public String password;
  public UserRole role;
  public Long id;
}
