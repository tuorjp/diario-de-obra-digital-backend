package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ueg.diario_de_obra_digital_backend.config.security.TokenService;
import ueg.diario_de_obra_digital_backend.dto.AuthenticationDTO;
import ueg.diario_de_obra_digital_backend.dto.LoginResponseDTO;
import ueg.diario_de_obra_digital_backend.dto.RegisterDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("login")
  public ResponseEntity login(@RequestBody AuthenticationDTO data) {
    var usernamePassword = new UsernamePasswordAuthenticationToken(data.login, data.password);

    var auth = this.authenticationManager.authenticate(usernamePassword);

    var token = tokenService.generateToken((User) auth.getPrincipal());

    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
    loginResponseDTO.setToken(token);

    return ResponseEntity.ok(loginResponseDTO);
  }

  @PostMapping("register")
  public ResponseEntity register(@RequestBody RegisterDTO data) {
    if(this.userRepository.findByLogin(data.login) != null) {
      return ResponseEntity.badRequest().build();
    }

    String encryptedPassword = passwordEncoder.encode(data.password);
    User newUser = new User(data.getLogin(), encryptedPassword, data.getRole());

    userRepository.save(newUser);
    return ResponseEntity.ok().build();
  }
}
