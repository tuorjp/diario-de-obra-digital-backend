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
                        if (userRepository.findUserByLogin("admin@gmail.com").isEmpty()) {
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
                        if (userRepository.findUserByLogin("miguel@gmail.com").isEmpty()) {
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

                        // ---------------------------------------------------------
                        // 3. SEED DAS OBRAS
                        // ---------------------------------------------------------
                        if (obraRepository.count() == 0) {
                                System.out.println("Iniciando Seeding das Obras...");

                                // Busca fiscais e engenheiros já persistidos
                                List<User> fiscais = userRepository.findAllByRole(UserRole.FISCAL);
                                List<User> engenheiros = userRepository.findAllByRole(UserRole.ENGENHEIRO);

                                // Dados fictícios realistas de obras de construção civil
                                String[][] obrasData = {
                                                // { contratante, contratada, projeto }
                                                { "Prefeitura Municipal de Anápolis", "Construtora Horizonte Ltda.",
                                                                "Construção da UBS Bairro Jundiaí" },
                                                { "Governo do Estado de Goiás", "Engenharia Alfa S.A.",
                                                                "Pavimentação da GO-080 – Trecho Serra Dourada" },
                                                { "Secretaria de Educação – GO", "Empreiteira Construtura Verde",
                                                                "Ampliação da Escola Estadual Prof. Rui Barbosa" },
                                                { "SANEAGO", "HidroTec Engenharia Ltda.",
                                                                "Implantação de Rede de Esgoto – Bairro Paraíso" },
                                                { "Ministério da Infraestrutura", "Viapar Construtora S.A.",
                                                                "Duplicação da BR-153 – Trecho Anápolis/Goiânia" },
                                                { "Condomínio Residencial Villa Verde", "Construtora Morada Bela Ltda.",
                                                                "Construção do Bloco C – Torres Residenciais" },
                                                { "Universidade Estadual de Goiás", "Arquiteto & Engenharia Associados",
                                                                "Construção do Novo Bloco Administrativo – UEG" },
                                                { "Prefeitura de Goiânia", "Mega Obras Engenharia S.A.",
                                                                "Revitalização do Parque Areião – Fase II" },
                                                { "Celg Distribuição S.A.", "Elétrica Goiana Serviços Ltda.",
                                                                "Subestação de Energia – Distrito Industrial Norte" },
                                                { "Grupo Hospitalar Primavera", "Saúde & Obra Construções Ltda.",
                                                                "Expansão do Centro Cirúrgico – HGP Anápolis" },
                                                { "Tribunal de Justiça de Goiás", "Construtora Horizonte Ltda.",
                                                                "Reforma do Fórum Cível" },
                                                { "Ministério Público de Goiás", "Engenharia Alfa S.A.",
                                                                "Construção da Nova Sede" }
                                };

                                // Alterna status: 7 ativas, 3 inativas, 2 concluídas
                                ObraStatus[] statuses = {
                                                ObraStatus.ATIVA, ObraStatus.ATIVA, ObraStatus.ATIVA,
                                                ObraStatus.ATIVA, ObraStatus.ATIVA, ObraStatus.INATIVA,
                                                ObraStatus.ATIVA, ObraStatus.ATIVA, ObraStatus.INATIVA,
                                                ObraStatus.INATIVA, ObraStatus.CONCLUIDA, ObraStatus.CONCLUIDA
                                };

                                for (int i = 0; i < obrasData.length; i++) {
                                        Obra obra = new Obra();
                                        obra.setContratante(obrasData[i][0]);
                                        obra.setContratada(obrasData[i][1]);
                                        obra.setProjeto(obrasData[i][2]);
                                        obra.setNumeroContrato(String.format("CT-%04d/%d", i + 1, LocalDate.now().getYear()));
                                        obra.setStatus(statuses[i]);
                                        obra.setDataInicio(LocalDate.now().minusDays(new Random().nextInt(365)));
                                        obra.setDataPrevistaFim(LocalDate.now().plusMonths(6 + i));
                                        obra.setObservacao("Observação inicial gerada automaticamente para o projeto " + obrasData[i][2] + " da contratada " + obrasData[i][1] + ".");

                                        EnderecoObra endereco = new EnderecoObra();
                                        endereco.setEndereco("Rua Fictícia da Obra " + i);
                                        endereco.setComplemento("Setor " + i);
                                        endereco.setNumero(String.valueOf(i * 100));
                                        endereco.setCidade("Anápolis");
                                        endereco.setCep("75000-000");
                                        endereco.setUf("GO");
                                        endereco.setObra(obra);
                                        obra.setEndereco(endereco);

                                        // Atribui fiscal de forma rotativa (se houver fiscais)
                                        if (!fiscais.isEmpty()) {
                                                obra.setFiscal(fiscais.get(i % fiscais.size()));
                                        }

                                        // Atribui 1 ou 2 engenheiros de forma rotativa (se houver engenheiros)
                                        if (!engenheiros.isEmpty()) {
                                                obra.getEngenheiros().add(engenheiros.get(i % engenheiros.size()));
                                                if (engenheiros.size() > 1) {
                                                        obra.getEngenheiros().add(
                                                                        engenheiros.get((i + 1) % engenheiros.size()));
                                                }
                                        }

                                        obraRepository.save(obra);
                                }

                                System.out.println("Seeding de obras concluído. Total: " + obrasData.length
                                                + " obras inseridas.");
                        }

                        // ---------------------------------------------------------
                        // 4. SEED DE MÃO DE OBRA
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
                        // 5. SEED DE EQUIPAMENTOS
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
                        // 6. SEED DE SERVIÇOS
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

                        // ---------------------------------------------------------
                        // 7. SEED DE DIÁRIOS DE OBRA
                        // ---------------------------------------------------------
                        if (diarioDeObraRepository.count() == 0) {
                                System.out.println("Iniciando Seeding de Diários de Obra...");
                                List<Obra> obrasAtivas = obraRepository.findAllByStatus(ObraStatus.ATIVA);
                                List<MaoDeObra> todosMdo = maoDeObraRepository.findAll();
                                List<Servico> todosServicos = servicoRepository.findAll();
                                List<Equipamento> todosEquipamentos = equipamentoRepository.findAll();
                                Random random = new Random();

                                for (Obra obra : obrasAtivas) {
                                        // Cria 2 diários para cada obra ativa (datas diferentes)
                                        for (int d = 0; d < 2; d++) {
                                                DiarioDeObra diario = new DiarioDeObra();
                                                diario.setObra(obra);
                                                diario.setData(LocalDate.now().minusDays(d + 1));
                                                diario.setCondicaoClimatica("SOL");
                                                diario.setObservacoes("Diário gerado automaticamente na carga inicial.");
                                                diario.setStatus(d == 0 ? DiarioStatus.VALIDO : DiarioStatus.AGUARDANDO_AVALIACAO);
                                                
                                                // Autor: o primeiro engenheiro da obra ou o admin
                                                User autor = !obra.getEngenheiros().isEmpty() 
                                                        ? obra.getEngenheiros().iterator().next() 
                                                        : userRepository.findUserByLogin("admin@gmail.com").orElse(null);
                                                diario.setAutor(autor);

                                                // Tenta carregar imagem do resources
                                                try (InputStream is = resourceLoader.getResource("classpath:img.png").getInputStream()) {
                                                        String fotoNome = fileStorageService.storeFile(is, "img.png");
                                                        diario.getFotos().add(fotoNome);
                                                } catch (Exception e) {
                                                        System.err.println("Erro ao carregar img.png dos resources para o seeder: " + e.getMessage());
                                                }

                                                // Itens aleatórios
                                                // Mão de Obra
                                                for (int i = 0; i < 3; i++) {
                                                        DiarioMaoDeObra dm = new DiarioMaoDeObra();
                                                        dm.setDiario(diario);
                                                        dm.setMaoDeObra(todosMdo.get(random.nextInt(todosMdo.size())));
                                                        dm.setQuantidade(random.nextInt(5) + 1);
                                                        diario.getMaoDeObra().add(dm);
                                                }
                                                // Serviços
                                                for (int i = 0; i < 2; i++) {
                                                        DiarioServico ds = new DiarioServico();
                                                        ds.setDiario(diario);
                                                        ds.setServico(todosServicos.get(random.nextInt(todosServicos.size())));
                                                        ds.setQuantidade((double) (random.nextInt(10) + 1));
                                                        diario.getServicosExecutados().add(ds);
                                                }
                                                // Equipamentos
                                                for (int i = 0; i < 2; i++) {
                                                        DiarioEquipamento de = new DiarioEquipamento();
                                                        de.setDiario(diario);
                                                        de.setEquipamento(todosEquipamentos.get(random.nextInt(todosEquipamentos.size())));
                                                        de.setQuantidade(random.nextInt(3) + 1);
                                                        diario.getEquipamentos().add(de);
                                                }

                                                diarioDeObraRepository.save(diario);
                                        }
                                }
                                System.out.println("Seeding de Diários de Obra concluído.");
                        }
                } catch (Exception e) {
                        System.err.println("Erro durante o seeding: " + e.getMessage());
                        e.printStackTrace();
                }
        }
}
