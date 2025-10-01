package ueg.diario_de_obra_digital_backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Classe utilitária para gerar hashes de senha BCrypt.
 * Não faz parte da aplicação Spring Boot, é apenas para ser executada
 * manualmente quando precisarmos de um novo hash.
 */
public class PasswordEncoderUtil {

  public static void main(String[] args) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String rawPassword = "password";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    System.out.println("==================================================");
    System.out.println("Senha original: " + rawPassword);
    System.out.println("Hash BCrypt gerado: " + encodedPassword);
    System.out.println("==================================================");
  }
}
