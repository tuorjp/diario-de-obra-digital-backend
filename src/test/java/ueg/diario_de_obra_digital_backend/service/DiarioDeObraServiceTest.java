package ueg.diario_de_obra_digital_backend.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ueg.diario_de_obra_digital_backend.enums.DiarioStatus;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.*;
import ueg.diario_de_obra_digital_backend.repository.DiarioDeObraRepository;
import ueg.diario_de_obra_digital_backend.repository.EquipamentoRepository;
import ueg.diario_de_obra_digital_backend.repository.ObraRepository;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional // Garante que cada teste rode em uma transação e seja revertido ao final
@ActiveProfiles("test")
class DiarioDeObraServiceTest {

  @Autowired
  private DiarioDeObraService diarioDeObraService;

  @Autowired
  private DiarioDeObraRepository diarioDeObraRepository;

  @Autowired
  private ObraRepository obraRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  EquipamentoRepository equipamentoRepository;

  private Obra obraTeste;
  private User autorTeste;
  private DiarioDeObra diarioAtivo;
  private DiarioDeObra diarioDeletado;

  // O método @BeforeEach roda antes de cada método de teste (@Test)
  @BeforeEach
  void setUp() {
    // Limpa os repositórios para garantir que cada teste comece do zero
    // Segurança extra além do @Transactional
    diarioDeObraRepository.deleteAll();
    obraRepository.deleteAll();
    userRepository.deleteAll();
    equipamentoRepository.deleteAll();

    // Criação dos dados em memória
    autorTeste = new User("autor.teste", "Autor Teste", "senha", UserRole.ENGENHEIRO);

    obraTeste = new Obra();
    obraTeste.setProjeto("Projeto de Teste");
    obraTeste.setContratante("Contratante Teste");
    obraTeste.setContratada("Contratada Teste");
    obraTeste.setStatus(ObraStatus.ATIVA);

    Equipamento equipamentoTeste = new Equipamento();
    equipamentoTeste.setNome("Betoneira de Teste");

    // Persistindo as entidades que não têm dependências complexas primeiro
    userRepository.save(autorTeste);
    obraRepository.save(obraTeste);
    equipamentoRepository.save(equipamentoTeste);

    // Montando o diarioAtivo completo em memória
    diarioAtivo = new DiarioDeObra();
    diarioAtivo.setObra(obraTeste);
    diarioAtivo.setAutor(autorTeste);
    diarioAtivo.setData(LocalDate.now().minusDays(2));
    diarioAtivo.setCondicaoClimatica("Ensolarado");
    diarioAtivo.setStatus(DiarioStatus.AGUARDANDO_AVALIACAO);
    diarioAtivo.setObservacoes("Tudo correu bem.");

    DiarioEquipamento diarioEquipamento = new DiarioEquipamento();
    diarioEquipamento.setDiario(diarioAtivo);
    diarioEquipamento.setEquipamento(equipamentoTeste);
    diarioEquipamento.setQuantidade(1);
    diarioAtivo.getEquipamentos().add(diarioEquipamento);

    // Montando o diarioDeletado completo em memória
    diarioDeletado = new DiarioDeObra();
    diarioDeletado.setObra(obraTeste);
    diarioDeletado.setAutor(autorTeste);
    diarioDeletado.setData(LocalDate.now().minusDays(1));
    diarioDeletado.setCondicaoClimatica("Chuvoso");
    diarioDeletado.setDeletado(true);
    diarioDeletado.setStatus(DiarioStatus.VALIDO);
    diarioDeletado.setObservacoes("Trabalho interrompido pela chuva.");

    // Salvando os diários de uma única vez no final
    diarioDeObraRepository.saveAll(List.of(diarioAtivo, diarioDeletado));
  }

  @Test
  @DisplayName("Deve listar apenas os diários ativos")
  void listAllAtivos_deveRetornarApenasDiariosNaoDeletados() {
    // Ação
    List<DiarioDeObra> resultado = diarioDeObraService.listAllActive();

    // Verificação
    assertNotNull(resultado);
    assertEquals(1, resultado.size(), "A lista deve conter apenas 1 diário ativo");
    assertFalse(resultado.get(0).isDeletado(), "O diário retornado não deve estar marcado como deletado");
    assertEquals(diarioAtivo.getId(), resultado.get(0).getId());
  }

  @Test
  @DisplayName("Deve listar todos os diários, incluindo os deletados")
  void listAllIncluindoDeletados_deveRetornarTodosOsDiarios() {
    // Ação
    List<DiarioDeObra> resultado = diarioDeObraService.listAllWithDeleted();

    // Verificação
    assertNotNull(resultado);
    assertEquals(2, resultado.size(), "A lista deve conter 2 diários no total");
  }

  @Test
  @DisplayName("Deve realizar o delete lógico de um diário")
  void deleteLogico_deveMarcarDiarioComoDeletado() {
    // Ação
    diarioDeObraService.logicDelete(diarioAtivo.getId());

    // Verificação
    // Repositório para buscar diretamente no banco, ignorando o filtro @Where
    Optional<DiarioDeObra> diarioVerificado = diarioDeObraRepository.findByIdEvenIfDeleted(diarioAtivo.getId());

    assertTrue(diarioVerificado.isPresent(), "O diário ainda deve existir no banco de dados");
    assertTrue(diarioVerificado.get().isDeletado(), "O campo 'deletado' do diário deve ser true");

    // Verificamos também se o método de listar ativos não o encontra mais
    List<DiarioDeObra> listaAtivosAposDelete = diarioDeObraService.listAllActive();
    assertEquals(0, listaAtivosAposDelete.size(), "A lista de diários ativos deve estar vazia após o delete");
  }
}