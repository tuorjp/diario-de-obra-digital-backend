package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.config.security.TokenService;
import ueg.diario_de_obra_digital_backend.dto.AuthenticationDTO;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

// Esse service precisa implementar o UserDetailsService, para usar o método de encontrar o usuário por login (no nosso caso o email)
@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByLogin(username); //no nosso caso vai ser o email
  }

  public String authenticate(AuthenticationDTO data) {
    var usernamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());

    var auth = this.authenticationManager.authenticate(usernamePassword);

    var token = tokenService.generateToken((User) auth.getPrincipal());

    return token;
  }
}
