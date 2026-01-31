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
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDataBase() {
        return args -> {
            // ---------------------------------------------------------
            // 1. SEED DO ADMIN
            // ---------------------------------------------------------
            if (userRepository.findByLogin("admin@gmail.com") == null) {
                System.out.println("Iniciando Seeding do ADMIN...");
                User user = new User();

                user.setLogin("admin@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(UserRole.ADMIN);
                user.setName("Juliana Evelyn Clarice Pires");
                user.setCpf("046.659.785-44");
                user.setEnabled(true);
                user.setCrea("5168415");
                user.setCreaUf("GO");
                user.setPhone1("(62) 99988-7755");
                user.setPhone2("(62) 99988-7755");
                user.setAddress("Avenida Tancredo Neves");
                user.setAddressNumber("s/n");
                user.setZipCode("85866-000");
                user.setComplement("Bairro Vila A");
                user.setCity("Foz do Iguaçu");
                user.setState("PR");

                // Define data fixa para o admin
                user.setCreationDate(LocalDate.now());

                userRepository.save(user);
                System.out.println("Admin inserido com sucesso.");
            }

            // ---------------------------------------------------------
            // 2. SEED DOS 30 NOVOS USUÁRIOS (ALEATÓRIOS)
            // ---------------------------------------------------------
            if (userRepository.findByLogin("miguel@gmail.com") == null) {
                System.out.println("Gerando 30 usuários aleatórios...");

                // Lista de primeiros nomes (garante login único: nome@gmail.com)
                String[] firstNames = {
                        "Miguel", "Arthur", "Gael", "Théo", "Heitor",
                        "Ravi", "Davi", "Bernardo", "Noah", "Gabriel",
                        "Samuel", "Pedro", "Anthony", "Isaac", "Benicio",
                        "Benjamin", "Matheus", "Lucas", "Joaquim", "Nicolas",
                        "Lucca", "Henrique", "Manuela", "Maia", "Helena",
                        "Alice", "Laura", "Carmen", "Valentina", "Heloisa"
                };

                // Lista de sobrenomes para sortear
                String[] lastNames = {
                        "Silva", "Santos", "Oliveira", "Souza", "Rodrigues",
                        "Ferreira", "Alves", "Pereira", "Lima", "Gomes",
                        "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida",
                        "Lopes", "Soares", "Fernandes", "Vieira", "Barbosa"
                };

                // Roles permitidas (Sem USER, Sem ADMIN)
                UserRole[] roles = {
                        UserRole.GESTOR,
                        UserRole.ENGENHEIRO,
                        UserRole.FISCAL
                };

                Random random = new Random();

                for (int i = 0; i < firstNames.length; i++) {
                    String firstName = firstNames[i];
                    // Sorteia um sobrenome aleatório
                    String lastName = lastNames[random.nextInt(lastNames.length)];

                    // Distribuição equitativa das Roles (0, 1, 2, 0, 1, 2...)
                    UserRole role = roles[i % roles.length];

                    User newUser = new User();

                    // Dados Básicos
                    newUser.setName(firstName + " " + lastName);
                    newUser.setLogin(firstName.toLowerCase() + "@gmail.com");
                    newUser.setPassword(passwordEncoder.encode("123456"));
                    newUser.setRole(role);

                    // Aleatoriedade de Status: A cada 5 usuários, 1 é inativo (20% inativos)
                    // Ou use random.nextBoolean() para ser totalmente aleatório
                    boolean isActive = (i % 5) != 0;
                    newUser.setEnabled(isActive);

                    // Datas Aleatórias (Entre hoje e 2 anos atrás)
                    int daysToSubtract = random.nextInt(730); // 0 a 730 dias
                    newUser.setCreationDate(LocalDate.now().minusDays(daysToSubtract));

                    // Dados Obrigatórios Fictícios
                    String cpfIndex = String.format("%02d", i);
                    newUser.setCpf("111.222.3" + cpfIndex + "-00");
                    newUser.setPhone1("(62) 9" + (1000 + i) + "-" + (2000 + i));

                    // Endereço fictício obrigatório na entidade
                    newUser.setAddress("Rua Aleatória " + i);
                    newUser.setAddressNumber(String.valueOf(i * 10));
                    newUser.setZipCode("75000-000");
                    newUser.setCity("Anápolis");
                    newUser.setState("GO");

                    // CREA apenas para cargos técnicos
                    if (role == UserRole.ENGENHEIRO || role == UserRole.FISCAL) {
                        newUser.setCrea("CREA-" + (5000 + i));
                        newUser.setCreaUf("GO");
                    }

                    userRepository.save(newUser);
                }
                System.out.println("Seeding de usuários concluído.");
            }
        };
    }
}