package ueg.diario_de_obra_digital_backend.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDataBase() {
        return args -> {
            // Verifica se o admin já existe para não duplicar ou dar erro na inicialização
            if (userRepository.findByLogin("admin@gmail.com") == null) {
                System.out.println("Iniciando Seeding...");
                User user = new User();

                // Credenciais de acesso
                user.setLogin("admin@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(UserRole.ADMIN); // Mantido ADMIN para acesso total

                // Dados Pessoais
                user.setName("Juliana Evelyn Clarice Pires");
                user.setCpf("046.659.785-44");
                user.setEnabled(true);

                // Dados Profissionais
                user.setCrea("5168415");
                user.setCreaUf("GO");

                // Contatos
                user.setPhone1("(62) 99988-7755");
                user.setPhone2("(62) 99988-7755");

                // Endereço
                user.setAddress("Avenida Tancredo Neves");
                user.setAddressNumber("s/n");
                user.setZipCode("85866-000");
                user.setComplement("Bairro Vila A");
                user.setCity("Foz do Iguaçu");
                user.setState("PR");

                userRepository.save(user);
                System.out.println("Seeding Concluído...");
            }
        };
    }
}