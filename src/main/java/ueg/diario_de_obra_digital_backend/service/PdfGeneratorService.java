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
        if (diarios == null || diarios.isEmpty()) {
            return new byte[0];
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font cardHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Obra obra = diarios.get(0).getObra();

            // Cabeçalho - Dados da Obra (Impresso apenas na primeira página)
            Paragraph title = new Paragraph("Relatório de Diários de Obra", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Dados da Obra", subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable obraTable = new PdfPTable(2);
            obraTable.setWidthPercentage(100);
            
            addTableRowWithBorders(obraTable, "Nome da Obra:", obra.getProjeto(), boldFont, normalFont);
            addTableRowWithBorders(obraTable, "Contratante:", obra.getContratante(), boldFont, normalFont);
            addTableRowWithBorders(obraTable, "Contratada:", obra.getContratada(), boldFont, normalFont);
            addTableRowWithBorders(obraTable, "Data Início:", obra.getDataInicio() != null ? obra.getDataInicio().format(dateFormatter) : "N/D", boldFont, normalFont);
            addTableRowWithBorders(obraTable, "Data Prevista Fim:", obra.getDataPrevistaFim() != null ? obra.getDataPrevistaFim().format(dateFormatter) : "N/D", boldFont, normalFont);
            addTableRowWithBorders(obraTable, "Fiscal:", obra.getFiscal() != null ? obra.getFiscal().getName() : "Nenhum", boldFont, normalFont);
            
            String engenheirosStr = obra.getEngenheiros() != null && !obra.getEngenheiros().isEmpty()
                    ? obra.getEngenheiros().stream().map(User::getName).collect(Collectors.joining(", "))
                    : "Nenhum";
            addTableRowWithBorders(obraTable, "Engenheiros:", engenheirosStr, boldFont, normalFont);
            
            addTableRowWithBorders(obraTable, "Gestor (Criador):", obra.getCriador() != null ? obra.getCriador().getName() : "N/D", boldFont, normalFont);
            
            document.add(obraTable);
            document.add(new Paragraph(" "));
            
            BaseColor cardHeaderBgColor = new BaseColor(59, 130, 246); // Blue
            
            // Diários
            for (int i = 0; i < diarios.size(); i++) {
                document.newPage(); // Cada diário inicia em uma nova página

                DiarioDeObra diario = diarios.get(i);
                
                Paragraph diarioTitle = new Paragraph("Diário de Obra #" + diario.getId(), subtitleFont);
                diarioTitle.setSpacingAfter(10);
                document.add(diarioTitle);

                // CARD: Detalhes do Diário
                PdfPTable detailsCard = createCardTable("Detalhes Principais", cardHeaderBgColor, cardHeaderFont);
                PdfPTable detailsContent = new PdfPTable(2);
                detailsContent.setWidthPercentage(100);
                addTableRow(detailsContent, "Data:", diario.getData() != null ? diario.getData().format(dateFormatter) : "N/D", boldFont, normalFont);
                addTableRow(detailsContent, "Condição Climática:", diario.getCondicaoClimatica() != null ? diario.getCondicaoClimatica() : "N/D", boldFont, normalFont);
                addTableRow(detailsContent, "Autor do Diário:", diario.getAutor() != null ? diario.getAutor().getName() : "N/D", boldFont, normalFont);
                addContentToCard(detailsCard, detailsContent);
                document.add(detailsCard);
                document.add(new Paragraph(" "));

                // CARD: Equipamentos
                PdfPTable equipCard = createCardTable("Equipamentos Utilizados", cardHeaderBgColor, cardHeaderFont);
                if (diario.getEquipamentos() != null && !diario.getEquipamentos().isEmpty()) {
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
                    for (DiarioEquipamento eq : diario.getEquipamentos()) {
                        addTableRow(table, eq.getEquipamento().getNome(), "Qtd: " + eq.getQuantidade(), boldFont, normalFont);
                    }
                    addContentToCard(equipCard, table);
                } else {
                    addTextToCard(equipCard, "Nenhum equipamento registrado.", normalFont);
                }
                document.add(equipCard);
                document.add(new Paragraph(" "));

                // CARD: Mão de Obra
                PdfPTable moCard = createCardTable("Mão de Obra", cardHeaderBgColor, cardHeaderFont);
                if (diario.getMaoDeObra() != null && !diario.getMaoDeObra().isEmpty()) {
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
                    for (DiarioMaoDeObra mo : diario.getMaoDeObra()) {
                        addTableRow(table, mo.getMaoDeObra().getNome(), "Qtd: " + mo.getQuantidade(), boldFont, normalFont);
                    }
                    addContentToCard(moCard, table);
                } else {
                    addTextToCard(moCard, "Nenhuma mão de obra registrada.", normalFont);
                }
                document.add(moCard);
                document.add(new Paragraph(" "));

                // CARD: Serviços Executados
                PdfPTable servCard = createCardTable("Serviços Executados", cardHeaderBgColor, cardHeaderFont);
                if (diario.getServicosExecutados() != null && !diario.getServicosExecutados().isEmpty()) {
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
                    for (DiarioServico sv : diario.getServicosExecutados()) {
                        addTableRow(table, sv.getServico().getNome(), "Qtd: " + sv.getQuantidade(), boldFont, normalFont);
                    }
                    addContentToCard(servCard, table);
                } else {
                    addTextToCard(servCard, "Nenhum serviço registrado.", normalFont);
                }
                document.add(servCard);
                document.add(new Paragraph(" "));

                // CARD: Ocorrências
                PdfPTable ocCard = createCardTable("Ocorrências", cardHeaderBgColor, cardHeaderFont);
                if (diario.getOcorrencias() != null && !diario.getOcorrencias().isEmpty()) {
                    PdfPTable table = new PdfPTable(1);
                    table.setWidthPercentage(100);
                    for (Ocorrencia oc : diario.getOcorrencias()) {
                        PdfPCell cell = new PdfPCell();
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.addElement(new Paragraph(oc.getTipo(), boldFont));
                        cell.addElement(new Paragraph(oc.getOcorrencia(), normalFont));
                        cell.setPaddingBottom(5);
                        table.addCell(cell);
                    }
                    addContentToCard(ocCard, table);
                } else {
                    addTextToCard(ocCard, "Nenhuma ocorrência registrada.", normalFont);
                }
                document.add(ocCard);
                document.add(new Paragraph(" "));

                // CARD: Observações
                PdfPTable obsCard = createCardTable("Observações", cardHeaderBgColor, cardHeaderFont);
                String obs = diario.getObservacoes() != null && !diario.getObservacoes().isEmpty() ? diario.getObservacoes() : "Nenhuma observação.";
                addTextToCard(obsCard, obs, normalFont);
                document.add(obsCard);
                document.add(new Paragraph(" "));

                // Fotos
                PdfPTable fotosCard = createCardTable("Fotos", cardHeaderBgColor, cardHeaderFont);
                if (diario.getFotos() != null && !diario.getFotos().isEmpty()) {
                    PdfPTable imageTable = new PdfPTable(2); 
                    imageTable.setWidthPercentage(100);
                    
                    for (String fotoFileName : diario.getFotos()) {
                        try {
                            Resource resource = fileStorageService.loadFileAsResource(fotoFileName);
                            Image img = Image.getInstance(resource.getFile().getAbsolutePath());
                            img.scaleToFit(200, 200);
                            
                            PdfPCell cell = new PdfPCell(img, true);
                            cell.setBorder(Rectangle.NO_BORDER);
                            cell.setPadding(10);
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            imageTable.addCell(cell);
                        } catch (Exception e) {
                            PdfPCell errCell = new PdfPCell(new Paragraph("[Erro ao carregar imagem]"));
                            errCell.setBorder(Rectangle.NO_BORDER);
                            imageTable.addCell(errCell);
                        }
                    }
                    
                    if (diario.getFotos().size() % 2 != 0) {
                        PdfPCell emptyCell = new PdfPCell(new Paragraph(" "));
                        emptyCell.setBorder(Rectangle.NO_BORDER);
                        imageTable.addCell(emptyCell);
                    }
                    addContentToCard(fotosCard, imageTable);
                } else {
                    addTextToCard(fotosCard, "Nenhuma foto anexada.", normalFont);
                }
                document.add(fotosCard);
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return out.toByteArray();
    }

    private void addTableRowWithBorders(PdfPTable table, String header, String value, Font boldFont, Font normalFont) {
        PdfPCell headerCell = new PdfPCell(new Paragraph(header, boldFont));
        headerCell.setPadding(6);
        headerCell.setBackgroundColor(new BaseColor(243, 244, 246)); // light gray
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, normalFont));
        valueCell.setPadding(6);
        
        table.addCell(headerCell);
        table.addCell(valueCell);
    }
    
    private void addTableRow(PdfPTable table, String header, String value, Font boldFont, Font normalFont) {
        PdfPCell headerCell = new PdfPCell(new Paragraph(header, boldFont));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(4);
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, normalFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        
        table.addCell(headerCell);
        table.addCell(valueCell);
    }

    private PdfPTable createCardTable(String title, BaseColor headerBgColor, Font headerFont) {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);
        
        PdfPCell headerCell = new PdfPCell(new Paragraph(title, headerFont));
        headerCell.setBackgroundColor(headerBgColor);
        headerCell.setPadding(8);
        headerCell.setBorderColor(new BaseColor(200, 200, 200));
        
        card.addCell(headerCell);
        return card;
    }
    
    private void addContentToCard(PdfPTable card, PdfPTable content) {
        PdfPCell contentCell = new PdfPCell(content);
        contentCell.setPadding(10);
        contentCell.setBorderColor(new BaseColor(200, 200, 200));
        card.addCell(contentCell);
    }
    
    private void addTextToCard(PdfPTable card, String text, Font font) {
        PdfPCell contentCell = new PdfPCell(new Paragraph(text, font));
        contentCell.setPadding(10);
        contentCell.setBorderColor(new BaseColor(200, 200, 200));
        card.addCell(contentCell);
    }
}
