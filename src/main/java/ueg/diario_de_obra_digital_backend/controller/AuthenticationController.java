package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ueg.diario_de_obra_digital_backend.config.security.TokenService;
import ueg.diario_de_obra_digital_backend.dto.AuthenticationDTO;
import ueg.diario_de_obra_digital_backend.dto.LoginResponseDTO;
import ueg.diario_de_obra_digital_backend.service.AuthorizationService;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final TokenService tokenService;
  private final AuthorizationService authorizationService;

  @PostMapping("login")
  public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
    var token = authorizationService.authenticate(data);

    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
    loginResponseDTO.setToken(token);

    return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
  }
}