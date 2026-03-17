package ueg.diario_de_obra_digital_backend.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ueg.diario_de_obra_digital_backend.dto.CreateObraDTO;
import ueg.diario_de_obra_digital_backend.dto.ObraResponseDTO;
import ueg.diario_de_obra_digital_backend.dto.UpdateObraDTO;
import ueg.diario_de_obra_digital_backend.enums.ObraStatus;
import ueg.diario_de_obra_digital_backend.enums.UserRole;
import ueg.diario_de_obra_digital_backend.model.Obra;
import ueg.diario_de_obra_digital_backend.model.User;
import ueg.diario_de_obra_digital_backend.repository.ObraRepository;
import ueg.diario_de_obra_digital_backend.repository.UserRepository;
import ueg.diario_de_obra_digital_backend.service.exception.DuplicateRoleAssignmentException;
import ueg.diario_de_obra_digital_backend.service.exception.ObraNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ObraServiceTest {

    @Autowired
    private ObraService obraService;

    @Autowired
    private ObraRepository obraRepository;

    @Autowired
    private UserRepository userRepository;

    private User fiscal;
    private User engenheiro;
    private User engenheiroExtra;
    private User gestor;
    private User admin;

    @BeforeEach
    void setUp() {
        obraRepository.deleteAll();
        userRepository.deleteAll();

        fiscal = new User("fiscal@test.com", "Fiscal Teste", "senha", UserRole.FISCAL);
        engenheiro = new User("engenheiro@test.com", "Engenheiro Teste", "senha", UserRole.ENGENHEIRO);
        engenheiroExtra = new User("engenheiro2@test.com", "Engenheiro Extra", "senha", UserRole.ENGENHEIRO);
        gestor = new User("gestor@test.com", "Gestor Teste", "senha", UserRole.GESTOR);
        admin = new User("admin@test.com", "Admin Teste", "senha", UserRole.ADMIN);

        userRepository.save(fiscal);
        userRepository.save(engenheiro);
        userRepository.save(engenheiroExtra);
        userRepository.save(gestor);
        userRepository.save(admin);
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar obra com dados válidos e status ATIVA")
    void create_comDadosValidos_deveSalvarObraAtiva() {
        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura Municipal");
        dto.setContratada("Construtora ABC");
        dto.setProjeto("Escola Nova");

        ObraResponseDTO response = obraService.create(dto, admin);

        assertNotNull(response.getId());
        assertEquals("Prefeitura Municipal", response.getContratante());
        assertEquals(ObraStatus.ATIVA, response.getStatus());
    }

    @Test
    @DisplayName("Deve criar obra com fiscal e engenheiro atribuídos")
    void create_comFiscalEEngenheiro_deveAtribuirCorretamente() {
        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura");
        dto.setContratada("Construtora");
        dto.setProjeto("Ponte");
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(engenheiro.getId()));

        ObraResponseDTO response = obraService.create(dto, admin);

        assertNotNull(response.getFiscal());
        assertEquals(fiscal.getId(), response.getFiscal().getId());
        assertEquals(1, response.getEngenheiros().size());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao criar sem campos obrigatórios")
    void create_semCamposObrigatorios_deveLancarExcecao() {
        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura");
        // contratada e projeto ausentes

        assertThrows(IllegalArgumentException.class, () -> obraService.create(dto, admin));
    }

    @Test
    @DisplayName("RN 07 — Deve lançar exceção ao atribuir mesmo usuário como Fiscal e Engenheiro")
    void create_comMesmoUsuarioFiscalEEngenheiro_deveLancarExcecaoRN07() {
        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura");
        dto.setContratada("Construtora");
        dto.setProjeto("Prédio");
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(fiscal.getId())); // mesmo usuário!

        assertThrows(DuplicateRoleAssignmentException.class, () -> obraService.create(dto, admin));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao atribuir fiscal inativo")
    void create_comFiscalInativo_deveLancarExcecao() {
        fiscal.setEnabled(false);
        userRepository.save(fiscal);

        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura");
        dto.setContratada("Construtora");
        dto.setProjeto("Prédio");
        dto.setFiscalId(fiscal.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> obraService.create(dto, admin));
        assertEquals("Não é possível atribuir um usuário inativo como fiscal", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao atribuir engenheiro inativo")
    void create_comEngenheiroInativo_deveLancarExcecao() {
        engenheiro.setEnabled(false);
        userRepository.save(engenheiro);

        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura");
        dto.setContratada("Construtora");
        dto.setProjeto("Prédio");
        dto.setEngenheiroIds(Set.of(engenheiro.getId()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> obraService.create(dto, admin));
        assertEquals("Não é possível atribuir um usuário inativo como engenheiro", exception.getMessage());
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve editar obra e atualizar os campos corretamente")
    void update_comDadosValidos_deveAtualizarObra() {
        // Setup: cria obra
        Obra obra = new Obra();
        obra.setContratante("Antigo");
        obra.setContratada("Construtora");
        obra.setProjeto("Projeto A");
        obra.setStatus(ObraStatus.ATIVA);
        obra.setCriador(gestor);
        obraRepository.save(obra);

        UpdateObraDTO dto = new UpdateObraDTO();
        dto.setContratante("Novo Contratante");
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(engenheiro.getId()));

        ObraResponseDTO response = obraService.update(obra.getId(), dto, gestor);

        assertEquals("Novo Contratante", response.getContratante());
        assertNotNull(response.getFiscal());
        assertEquals(1, response.getEngenheiros().size());
    }

    @Test
    @DisplayName("RN 07 — Deve lançar exceção ao editar com mesmo usuário como Fiscal e Engenheiro")
    void update_comMesmoUsuarioFiscalEEngenheiro_deveLancarExcecaoRN07() {
        Obra obra = new Obra();
        obra.setContratante("X");
        obra.setContratada("Y");
        obra.setProjeto("Z");
        obra.setStatus(ObraStatus.ATIVA);
        obra.setCriador(gestor);
        obraRepository.save(obra);

        UpdateObraDTO dto = new UpdateObraDTO();
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(fiscal.getId())); // conflito

        assertThrows(DuplicateRoleAssignmentException.class, () -> obraService.update(obra.getId(), dto, gestor));
    }

    @Test
    @DisplayName("ACL — Deve bloquear edição de obra para gestor que não é o criador")
    void update_seNaoForCriadorEForGestor_deveLancarForbidden() {
        Obra obra = new Obra();
        obra.setContratante("X");
        obra.setContratada("Y");
        obra.setProjeto("Z");
        obra.setStatus(ObraStatus.ATIVA);
        obra.setCriador(admin); // outra pessoa
        obraRepository.save(obra);

        UpdateObraDTO dto = new UpdateObraDTO();
        dto.setContratante("Novo Contratante");

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> obraService.update(obra.getId(), dto, gestor));
    }

    // ─── DEACTIVATE ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve realizar exclusão lógica alterando status para INATIVA")
    void deactivate_obraAtiva_deveAlterarStatusParaInativa() {
        Obra obra = new Obra();
        obra.setContratante("X");
        obra.setContratada("Y");
        obra.setProjeto("Z");
        obra.setStatus(ObraStatus.ATIVA);
        obraRepository.save(obra);

        obraService.deactivate(obra.getId());

        Obra obraAtualizada = obraRepository.findById(obra.getId()).orElseThrow();
        assertEquals(ObraStatus.INATIVA, obraAtualizada.getStatus());
    }

    // ─── FIND ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve lançar ObraNotFoundException para ID inexistente")
    void findById_idInexistente_deveLancarExcecao() {
        assertThrows(ObraNotFoundException.class, () -> obraService.findById(999L, admin));
    }

    @Test
    @DisplayName("Deve retornar obras paginadas conforme filtros")
    void search_comFiltros_deveRetornarPaginaCorreta() {
        // Cria 2 obras
        Obra o1 = new Obra();
        o1.setContratante("Prefeitura A");
        o1.setContratada("Construtora");
        o1.setProjeto("Escola");
        o1.setStatus(ObraStatus.ATIVA);
        o1.setCriador(gestor);

        Obra o2 = new Obra();
        o2.setContratante("Estado");
        o2.setContratada("Empreiteira");
        o2.setProjeto("Hospital");
        o2.setStatus(ObraStatus.INATIVA);
        o2.setCriador(admin);

        obraRepository.save(o1);
        obraRepository.save(o2);

        Page<ObraResponseDTO> result = obraService.search("Escola", "projeto", null, PageRequest.of(0, 10), admin);
        assertEquals(1, result.getTotalElements());

        Page<ObraResponseDTO> inativas = obraService.search(null, null, ObraStatus.INATIVA, PageRequest.of(0, 10), admin);
        assertEquals(1, inativas.getTotalElements());
    }

    @Test
    @DisplayName("ACL — Gestor lista apenas as próprias obras")
    void search_comGestor_deveFiltrarObrasPorGestor() {
        Obra o1 = new Obra();
        o1.setContratante("P1");
        o1.setContratada("C1");
        o1.setProjeto("Prj1");
        o1.setStatus(ObraStatus.ATIVA);
        o1.setCriador(gestor);

        Obra o2 = new Obra();
        o2.setContratante("P2");
        o2.setContratada("C2");
        o2.setProjeto("Prj2");
        o2.setStatus(ObraStatus.ATIVA);
        o2.setCriador(admin);

        obraRepository.save(o1);
        obraRepository.save(o2);

        Page<ObraResponseDTO> result = obraService.search(null, null, null, PageRequest.of(0, 10), gestor);
        assertEquals(1, result.getTotalElements());
        assertEquals("P1", result.getContent().get(0).getContratante());
    }

    @Test
    @DisplayName("ACL — Fiscal lista apenas as obras nas quais está envolvido")
    void search_comFiscal_deveFiltrarObrasPorFiscalOuEngenheiro() {
        Obra o1 = new Obra();
        o1.setContratante("P1");
        o1.setContratada("C1");
        o1.setProjeto("Prj1");
        o1.setStatus(ObraStatus.ATIVA);
        o1.setFiscal(fiscal);
        o1.setCriador(gestor);

        Obra o2 = new Obra();
        o2.setContratante("P2");
        o2.setContratada("C2");
        o2.setProjeto("Prj2");
        o2.setStatus(ObraStatus.ATIVA);
        o2.setCriador(gestor);

        obraRepository.save(o1);
        obraRepository.save(o2);

        Page<ObraResponseDTO> result = obraService.search(null, null, null, PageRequest.of(0, 10), fiscal);
        assertEquals(1, result.getTotalElements());
        assertEquals("P1", result.getContent().get(0).getContratante());
    }
}
