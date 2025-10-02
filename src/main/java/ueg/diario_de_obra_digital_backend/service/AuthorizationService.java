package ueg.diario_de_obra_digital_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

// Esse service precisa implementar o UserDetailsService, para usar o método de encontrar o usuário por login (no nosso caso o email)
// A única responsabilidade desse service é a função que está criada dentro dele
// !! NÃO MEXER !!
@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByLogin(username); //no nosso caso vai ser o email
  }
}
