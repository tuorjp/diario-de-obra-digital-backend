package ueg.diario_de_obra_digital_backend.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public CommandLineRunner seedDataBase() {
    return args -> {
      System.out.println("SEEDING...");
      User user = new User();

      user.setLogin("admin@gmail.com");
      user.setPassword(passwordEncoder.encode("123456"));
      user.setRole(UserRole.ADMIN);
      user.setName("Admin");

      userRepository.save(user);
      System.out.println("SOWED...");
    };
  }
}
