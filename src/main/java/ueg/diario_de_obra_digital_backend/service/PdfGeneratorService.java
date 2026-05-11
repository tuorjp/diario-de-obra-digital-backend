package ueg.diario_de_obra_digital_backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ueg.diario_de_obra_digital_backend.model.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final FileStorageService fileStorageService;

    public byte[] generateDiariosPdf(List<DiarioDeObra> diarios) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < diarios.size(); i++) {
                if (i > 0) {
                    document.newPage(); // Cada diário em uma nova página
                }

                DiarioDeObra diario = diarios.get(i);
                Obra obra = diario.getObra();

                // Cabeçalho - Dados da Obra
                Paragraph title = new Paragraph("Relatório de Diário de Obra", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                document.add(new Paragraph("Dados da Obra", subtitleFont));
                document.add(new Paragraph(" "));

                PdfPTable obraTable = new PdfPTable(2);
                obraTable.setWidthPercentage(100);
                
                addTableRow(obraTable, "Nome da Obra:", obra.getProjeto(), boldFont, normalFont);
                addTableRow(obraTable, "Contratante:", obra.getContratante(), boldFont, normalFont);
                addTableRow(obraTable, "Contratada:", obra.getContratada(), boldFont, normalFont);
                addTableRow(obraTable, "Data Início:", obra.getDataInicio() != null ? obra.getDataInicio().format(dateFormatter) : "N/D", boldFont, normalFont);
                addTableRow(obraTable, "Data Prevista Fim:", obra.getDataPrevistaFim() != null ? obra.getDataPrevistaFim().format(dateFormatter) : "N/D", boldFont, normalFont);
                addTableRow(obraTable, "Fiscal:", obra.getFiscal() != null ? obra.getFiscal().getName() : "Nenhum", boldFont, normalFont);
                
                String engenheirosStr = obra.getEngenheiros() != null && !obra.getEngenheiros().isEmpty()
                        ? obra.getEngenheiros().stream().map(User::getName).collect(Collectors.joining(", "))
                        : "Nenhum";
                addTableRow(obraTable, "Engenheiros:", engenheirosStr, boldFont, normalFont);
                
                addTableRow(obraTable, "Gestor (Criador):", obra.getCriador() != null ? obra.getCriador().getName() : "N/D", boldFont, normalFont);
                
                document.add(obraTable);
                document.add(new Paragraph(" "));
                document.add(new Paragraph("--------------------------------------------------"));
                document.add(new Paragraph(" "));

                // Dados do Diário
                document.add(new Paragraph("Detalhes do Diário", subtitleFont));
                document.add(new Paragraph(" "));

                PdfPTable diarioTable = new PdfPTable(2);
                diarioTable.setWidthPercentage(100);
                
                addTableRow(diarioTable, "Data do Diário:", diario.getData() != null ? diario.getData().format(dateFormatter) : "N/D", boldFont, normalFont);
                addTableRow(diarioTable, "Condição Climática:", diario.getCondicaoClimatica() != null ? diario.getCondicaoClimatica() : "N/D", boldFont, normalFont);
                addTableRow(diarioTable, "Autor do Diário:", diario.getAutor() != null ? diario.getAutor().getName() : "N/D", boldFont, normalFont);
                document.add(diarioTable);
                document.add(new Paragraph(" "));

                // Equipamentos
                document.add(new Paragraph("Equipamentos Utilizados:", boldFont));
                if (diario.getEquipamentos() != null && !diario.getEquipamentos().isEmpty()) {
                    for (DiarioEquipamento eq : diario.getEquipamentos()) {
                        document.add(new Paragraph("- " + eq.getEquipamento().getNome() + " (Qtd: " + eq.getQuantidade() + ")", normalFont));
                    }
                } else {
                    document.add(new Paragraph("Nenhum equipamento registrado.", normalFont));
                }
                document.add(new Paragraph(" "));

                // Mão de Obra
                document.add(new Paragraph("Mão de Obra:", boldFont));
                if (diario.getMaoDeObra() != null && !diario.getMaoDeObra().isEmpty()) {
                    for (DiarioMaoDeObra mo : diario.getMaoDeObra()) {
                        document.add(new Paragraph("- " + mo.getMaoDeObra().getNome() + " (Qtd: " + mo.getQuantidade() + ")", normalFont));
                    }
                } else {
                    document.add(new Paragraph("Nenhuma mão de obra registrada.", normalFont));
                }
                document.add(new Paragraph(" "));

                // Serviços Executados
                document.add(new Paragraph("Serviços Executados:", boldFont));
                if (diario.getServicosExecutados() != null && !diario.getServicosExecutados().isEmpty()) {
                    for (DiarioServico sv : diario.getServicosExecutados()) {
                        document.add(new Paragraph("- " + sv.getServico().getNome() + " (Qtd: " + sv.getQuantidade() + ")", normalFont));
                    }
                } else {
                    document.add(new Paragraph("Nenhum serviço registrado.", normalFont));
                }
                document.add(new Paragraph(" "));

                // Ocorrências
                document.add(new Paragraph("Ocorrências:", boldFont));
                if (diario.getOcorrencias() != null && !diario.getOcorrencias().isEmpty()) {
                    for (Ocorrencia oc : diario.getOcorrencias()) {
                        document.add(new Paragraph("- " + oc.getTipo() + ": " + oc.getOcorrencia(), normalFont));
                    }
                } else {
                    document.add(new Paragraph("Nenhuma ocorrência registrada.", normalFont));
                }
                document.add(new Paragraph(" "));

                // Observações
                document.add(new Paragraph("Observações:", boldFont));
                String obs = diario.getObservacoes() != null && !diario.getObservacoes().isEmpty() ? diario.getObservacoes() : "Nenhuma observação.";
                document.add(new Paragraph(obs, normalFont));
                document.add(new Paragraph(" "));

                // Fotos
                document.add(new Paragraph("Fotos:", boldFont));
                document.add(new Paragraph(" "));
                if (diario.getFotos() != null && !diario.getFotos().isEmpty()) {
                    PdfPTable imageTable = new PdfPTable(2); // 2 fotos por linha
                    imageTable.setWidthPercentage(100);
                    
                    for (String fotoFileName : diario.getFotos()) {
                        try {
                            Resource resource = fileStorageService.loadFileAsResource(fotoFileName);
                            Image img = Image.getInstance(resource.getFile().getAbsolutePath());
                            // Escalar para caber na célula
                            img.scaleToFit(200, 200);
                            
                            PdfPCell cell = new PdfPCell(img, true);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setPadding(10);
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            imageTable.addCell(cell);
                        } catch (Exception e) {
                            // Se a foto não for encontrada ou houver erro, adiciona texto placeholder
                            PdfPCell errCell = new PdfPCell(new Paragraph("[Erro ao carregar imagem]"));
                            errCell.setBorder(Rectangle.NO_BORDER);
                            imageTable.addCell(errCell);
                        }
                    }
                    
                    // Se o número de fotos for ímpar, adiciona uma célula vazia para completar a linha
                    if (diario.getFotos().size() % 2 != 0) {
                        PdfPCell emptyCell = new PdfPCell(new Paragraph(" "));
                        emptyCell.setBorder(Rectangle.NO_BORDER);
                        imageTable.addCell(emptyCell);
                    }
                    
                    document.add(imageTable);
                } else {
                    document.add(new Paragraph("Nenhuma foto anexada.", normalFont));
                }
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String header, String value, Font boldFont, Font normalFont) {
        PdfPCell headerCell = new PdfPCell(new Paragraph(header, boldFont));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, normalFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        
        table.addCell(headerCell);
        table.addCell(valueCell);
    }
}
