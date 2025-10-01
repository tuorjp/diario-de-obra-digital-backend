package ueg.diario_de_obra_digital_backend.dto;

import lombok.Data;

@Data
public class UploadFileResponseDTO {
  private String fileName;
  private String fileDownloadUri;
  private String fileType;
  private long size;
}
