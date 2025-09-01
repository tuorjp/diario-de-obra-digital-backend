package ueg.diario_de_obra_digital_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationDTO {
  public String login;
  public String password;
}