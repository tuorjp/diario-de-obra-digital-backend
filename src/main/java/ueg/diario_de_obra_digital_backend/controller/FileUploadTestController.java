package ueg.diario_de_obra_digital_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ueg.diario_de_obra_digital_backend.dto.UploadFileResponseDTO;
import ueg.diario_de_obra_digital_backend.service.FileStorageService;

@RestController
@RequestMapping("/api/files/test")
public class FileUploadTestController {
  @Autowired
  private FileStorageService fileStorageService;

  @PostMapping("/upload")
  public UploadFileResponseDTO uploadFile(@RequestParam("file") MultipartFile file) {
    String fileName = fileStorageService.storeFile(file);

    String fileDownloadUri = ServletUriComponentsBuilder
        .fromCurrentContextPath()
        .path("api/files/test/download")
        .path(fileName)
        .toUriString(); // constrói a uri de download

    UploadFileResponseDTO responseDTO = new UploadFileResponseDTO();
    responseDTO.setFileName(fileName);
    responseDTO.setFileDownloadUri(fileDownloadUri);
    responseDTO.setFileType(file.getContentType());
    responseDTO.setSize(file.getSize());

    return responseDTO;
  }

  @GetMapping("/download/{fileName:.+}") // regex que garante a leitura da extensão do arquivo, não só o nome
  public ResponseEntity<Resource> downloadFile(
      @PathVariable String fileName,
      HttpServletRequest request
  ) {
    Resource resource = this.fileStorageService.loadFileAsResource(fileName);
    String contentType = request.getServletContext().getMimeType(resource.getFilename());

    if(contentType == null) {
      contentType = "application/octet-stream";
    }

    return ResponseEntity
        .ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"") // attachment para download
        .body(resource);
  }
}