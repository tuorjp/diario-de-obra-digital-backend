package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;


}
