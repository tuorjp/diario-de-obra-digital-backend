package ueg.diario_de_obra_digital_backend.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ueg.diario_de_obra_digital_backend.model.*;
import ueg.diario_de_obra_digital_backend.repository.*;
import ueg.diario_de_obra_digital_backend.service.FileStorageService;
import org.springframework.core.io.ResourceLoader;
import ueg.diario_de_obra_digital_backend.enums.DiarioStatus;
import org.springframework.transaction.support.TransactionTemplate;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.enums.UserRole;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {
        private final UserRepository userRepository;
        private final ObraRepository obraRepository;
        private final MaoDeObraRepository maoDeObraRepository;
        private final EquipamentoRepository equipamentoRepository;
        private final ServicoRepository servicoRepository;
        private final DiarioDeObraRepository diarioDeObraRepository;
        private final PasswordEncoder passwordEncoder;
        private final FileStorageService fileStorageService;
        private final ResourceLoader resourceLoader;
        private final TransactionTemplate transactionTemplate;

        @Bean
        public CommandLineRunner seedDataBase() {
                return args -> {
                        transactionTemplate.execute(status -> {
                                runSeeder();
                                return null;
                        });
                };
        }

        public void runSeeder() {
                try {
                        // ---------------------------------------------------------
                        // 1. SEED DO ADMIN
                        // ---------------------------------------------------------
                        if (userRepository.findUserByLogin("admin@gmail.com").isEmpty() && userRepository.findUserByCpf("000.000.000-00").isEmpty()) {
                                System.out.println("Iniciando Seeding do ADMIN...");
                                User user = new User();

                                user.setLogin("admin@gmail.com");
                                user.setPassword(passwordEncoder.encode("123456"));
                                user.setRole(UserRole.ADMIN);
                                user.setName("Administrador");
                                user.setCpf("000.000.000-00");
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
                        // 2. SEED DOS DEMAIS USUÁRIOS (GESTOR, FISCAL, ENGENHEIRO)
                        // ---------------------------------------------------------

                        // GESTOR
                        if (userRepository.findUserByLogin("gestor@gmail.com").isEmpty() && userRepository.findUserByCpf("123.456.789-00").isEmpty()) {
                            System.out.println("Iniciando Seeding do GESTOR...");
                            User user = new User();
                            user.setLogin("gestor@gmail.com");
                            user.setPassword(passwordEncoder.encode("123456"));
                            user.setRole(UserRole.GESTOR);
                            user.setName("gestor");
                            user.setCpf("123.456.789-00");
                            user.setEnabled(true);
                            user.setCreationDate(LocalDate.now());
                            user.setPhone1("(62) 98888-1111");
                            user.setAddress("Rua Principal");
                            user.setAddressNumber("10");
                            user.setZipCode("75000-000");
                            user.setCity("Anápolis");
                            user.setState("GO");

                            userRepository.save(user);
                            System.out.println("Gestor inserido com sucesso.");
                        }

                        // FISCAL
                        if (userRepository.findUserByLogin("fiscal@gmail.com").isEmpty() && userRepository.findUserByCpf("234.567.890-11").isEmpty()) {
                            System.out.println("Iniciando Seeding do FISCAL...");
                            User user = new User();
                            user.setLogin("fiscal@gmail.com");
                            user.setPassword(passwordEncoder.encode("123456"));
                            user.setRole(UserRole.FISCAL);
                            user.setName("fiscal");
                            user.setCpf("234.567.890-11");
                            user.setEnabled(true);
                            user.setCreationDate(LocalDate.now());
                            user.setPhone1("(62) 97777-2222");
                            user.setAddress("Rua das Flores");
                            user.setAddressNumber("100");
                            user.setZipCode("75000-000");
                            user.setCity("Anápolis");
                            user.setState("GO");

                            userRepository.save(user);
                            System.out.println("Fiscal inserido com sucesso.");
                        }

                        // ENGENHEIROS
                        if (userRepository.findUserByLogin("engenheiro1@gmail.com").isEmpty() && userRepository.findUserByCpf("345.678.901-22").isEmpty()) {
                            System.out.println("Iniciando Seeding dos Engenheiros...");

                            // Primeiro Engenheiro
                            User eng1 = new User();
                            eng1.setLogin("engenheiro1@gmail.com");
                            eng1.setPassword(passwordEncoder.encode("123456"));
                            eng1.setRole(UserRole.ENGENHEIRO);
                            eng1.setName("engenheiro1");
                            eng1.setCpf("345.678.901-22");
                            eng1.setEnabled(true);
                            eng1.setCrea("9876543");
                            eng1.setCreaUf("GO");
                            eng1.setCreationDate(LocalDate.now());
                            eng1.setPhone1("(62) 96666-3333");
                            eng1.setAddress("Alameda das Palmeiras");
                            eng1.setAddressNumber("500");
                            eng1.setZipCode("75000-000");
                            eng1.setCity("Anápolis");
                            eng1.setState("GO");
                            userRepository.save(eng1);
                        }
                        
                        if (userRepository.findUserByLogin("engenheiro2@gmail.com").isEmpty() && userRepository.findUserByCpf("348.678.901-22").isEmpty()) {
                            // Segundo Engenheiro
                            User eng2 = new User();
                            eng2.setLogin("engenheiro2@gmail.com");
                            eng2.setPassword(passwordEncoder.encode("123456"));
                            eng2.setRole(UserRole.ENGENHEIRO);
                            eng2.setName("engenheiro2");
                            eng2.setCpf("348.678.901-22");
                            eng2.setEnabled(true);
                            eng2.setCrea("9856544");
                            eng2.setCreaUf("SP");
                            eng2.setCreationDate(LocalDate.now());
                            eng2.setPhone1("(62) 96666-4444");
                            eng2.setAddress("Alameda 5");
                            eng2.setAddressNumber("501");
                            eng2.setZipCode("75000-020");
                            eng2.setCity("Goiânia");
                            eng2.setState("GO");
                            userRepository.save(eng2);

                            System.out.println("Engenheiros processados com sucesso.");
                        }

                        // ---------------------------------------------------------
                        // 3. SEED DE MÃO DE OBRA
                        // ---------------------------------------------------------
                        if (maoDeObraRepository.count() == 0) {
                                System.out.println("Iniciando Seeding de Mão de Obra...");
                                String[] mdoNomes = {
                                        "MEIO OF PEDREIRO", "ARMADOR", "CARPINTEIRO", "PEDREIRO", "SERVENTE",
                                        "SERRALHEIRO", "PINTOR", "ELETRICISTA", "MESTRE DE OBRA", "OP DE BETONEIRA",
                                        "SERVICOS GERAIS", "PORTEIRO", "ENC DE OBRA", "ENC DE ALMOXARIFADO",
                                        "TEC SEG DO TRABALHO", "ASS ADMINISTRATIVO", "ENC DE SERRALHEIRIA",
                                        "ENC DE ARMACAO", "DPTO PESSOAL", "AUXILIAR DE ENGENHARIA",
                                        "ENGENHEIRO EXECUÇÃO", "ENGENHEIRO PLANEJAMENTO", "MOTORISTA",
                                        "SUPERVISOR DE OBRA"
                                };
                                for (String nome : mdoNomes) {
                                        MaoDeObra mdo = new MaoDeObra();
                                        mdo.setNome(nome);
                                        maoDeObraRepository.save(mdo);
                                }
                                System.out.println("Mão de Obra inserida com sucesso.");
                        }

                        // ---------------------------------------------------------
                        // 4. SEED DE EQUIPAMENTOS
                        // ---------------------------------------------------------
                        if (equipamentoRepository.count() == 0) {
                                System.out.println("Iniciando Seeding de Equipamentos...");
                                String[] equipNomes = {
                                        "CAMINHÃO CAÇAMBA", "MARTELETE", "MINI CARREGADEIRA", "MOTONIVELADORA",
                                        "PÁ CARREGADEIRA", "ROLO COMPACTADOR", "TRATOR C/ GRADE", "TRATOR DE ESTEIRA",
                                        "CAMINHÃO MUNCK", "RETROESCAVADEIRA", "GUINDASTE", "CARRETA",
                                        "PLATAFORMA ARTICULADA", "PLATAFORMA TESOURA", "VIBRO ACABADORA",
                                        "ACABADORA DUPLA", "REGUA VIBRATÓRIA", "CAMINHÃO BETONEIRA"
                                };
                                for (String nome : equipNomes) {
                                        Equipamento e = new Equipamento();
                                        e.setNome(nome);
                                        equipamentoRepository.save(e);
                                }
                                System.out.println("Equipamentos inseridos com sucesso.");
                        }

                        // ---------------------------------------------------------
                        // 5. SEED DE SERVIÇOS
                        // ---------------------------------------------------------
                        if (servicoRepository.count() == 0) {
                                System.out.println("Iniciando Seeding de Serviços...");
                                String[][] servicosData = {
                                        {"SOLDADOR", "UN"},
                                        {"MONTADOR", "UN"},
                                        {"ESCAVAÇÃO", "m³"},
                                        {"ALVENARIA", "m²"},
                                        {"PINTURA", "m²"},
                                        {"PISO", "m²"}
                                };
                                for (String[] data : servicosData) {
                                        Servico s = new Servico();
                                        s.setNome(data[0]);
                                        s.setUnidadeMedida(data[1]);
                                        servicoRepository.save(s);
                                }
                                System.out.println("Serviços inseridos com sucesso.");
                        }
                } catch (Exception e) {
                        System.err.println("Erro durante o seeding: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}
