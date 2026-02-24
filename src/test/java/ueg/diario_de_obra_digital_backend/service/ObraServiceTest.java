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

    @BeforeEach
    void setUp() {
        obraRepository.deleteAll();
        userRepository.deleteAll();

        fiscal = new User("fiscal@test.com", "Fiscal Teste", "senha", UserRole.FISCAL);
        engenheiro = new User("engenheiro@test.com", "Engenheiro Teste", "senha", UserRole.ENGENHEIRO);
        engenheiroExtra = new User("engenheiro2@test.com", "Engenheiro Extra", "senha", UserRole.ENGENHEIRO);

        userRepository.save(fiscal);
        userRepository.save(engenheiro);
        userRepository.save(engenheiroExtra);
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar obra com dados válidos e status ATIVA")
    void create_comDadosValidos_deveSalvarObraAtiva() {
        CreateObraDTO dto = new CreateObraDTO();
        dto.setContratante("Prefeitura Municipal");
        dto.setContratada("Construtora ABC");
        dto.setProjeto("Escola Nova");

        ObraResponseDTO response = obraService.create(dto);

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

        ObraResponseDTO response = obraService.create(dto);

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

        assertThrows(IllegalArgumentException.class, () -> obraService.create(dto));
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

        assertThrows(DuplicateRoleAssignmentException.class, () -> obraService.create(dto));
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
        obraRepository.save(obra);

        UpdateObraDTO dto = new UpdateObraDTO();
        dto.setContratante("Novo Contratante");
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(engenheiro.getId()));

        ObraResponseDTO response = obraService.update(obra.getId(), dto);

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
        obraRepository.save(obra);

        UpdateObraDTO dto = new UpdateObraDTO();
        dto.setFiscalId(fiscal.getId());
        dto.setEngenheiroIds(Set.of(fiscal.getId())); // conflito

        assertThrows(DuplicateRoleAssignmentException.class, () -> obraService.update(obra.getId(), dto));
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
        assertThrows(ObraNotFoundException.class, () -> obraService.findById(999L));
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

        Obra o2 = new Obra();
        o2.setContratante("Estado");
        o2.setContratada("Empreiteira");
        o2.setProjeto("Hospital");
        o2.setStatus(ObraStatus.INATIVA);

        obraRepository.save(o1);
        obraRepository.save(o2);

        Page<ObraResponseDTO> result = obraService.search("Escola", null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());

        Page<ObraResponseDTO> inativas = obraService.search(null, ObraStatus.INATIVA, PageRequest.of(0, 10));
        assertEquals(1, inativas.getTotalElements());
    }
}
