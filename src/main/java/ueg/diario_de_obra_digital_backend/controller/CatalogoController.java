package ueg.diario_de_obra_digital_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ueg.diario_de_obra_digital_backend.model.Equipamento;
import ueg.diario_de_obra_digital_backend.model.MaoDeObra;
import ueg.diario_de_obra_digital_backend.model.Servico;
import ueg.diario_de_obra_digital_backend.repository.EquipamentoRepository;
import ueg.diario_de_obra_digital_backend.repository.MaoDeObraRepository;
import ueg.diario_de_obra_digital_backend.repository.ServicoRepository;

import java.util.List;

@RestController
@RequestMapping("/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final EquipamentoRepository equipamentoRepository;
    private final MaoDeObraRepository maoDeObraRepository;
    private final ServicoRepository servicoRepository;

    @GetMapping("/equipamentos")
    public ResponseEntity<List<Equipamento>> listEquipamentos() {
        return ResponseEntity.ok(equipamentoRepository.findAll());
    }

    @GetMapping("/maodeobras")
    public ResponseEntity<List<MaoDeObra>> listMaoDeObras() {
        return ResponseEntity.ok(maoDeObraRepository.findAll());
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<Servico>> listServicos() {
        return ResponseEntity.ok(servicoRepository.findAll());
    }
}
