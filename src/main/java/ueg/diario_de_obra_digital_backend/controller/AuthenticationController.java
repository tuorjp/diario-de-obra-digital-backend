package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ueg.diario_de_obra_digital_backend.config.security.TokenService;
import ueg.diario_de_obra_digital_backend.dto.AuthenticationDTO;
import ueg.diario_de_obra_digital_backend.dto.LoginResponseDTO;
import ueg.diario_de_obra_digital_backend.model.User;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;

  @PostMapping("login")
  public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
    // Cria um token de autenticação com as credenciais recebidas
    var usernamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());

    // O AuthenticationManager usa o seu AuthorizationService por baixo dos panos para
    // buscar o usuário e verificar a senha.
    var auth = this.authenticationManager.authenticate(usernamePassword);

    // Se a autenticação foi bem-sucedida, gera o token JWT
    var token = tokenService.generateToken((User) auth.getPrincipal());

    // Retorna o token em um DTO
    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
    loginResponseDTO.setToken(token);

    return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
  }
}