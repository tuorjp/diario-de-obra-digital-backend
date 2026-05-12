package ueg.diario_de_obra_digital_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ueg.diario_de_obra_digital_backend.config.FileStorageProperties;
import ueg.diario_de_obra_digital_backend.service.exception.FileNotFoundInStorageException;
import ueg.diario_de_obra_digital_backend.service.exception.FileStorageCreationException;
import ueg.diario_de_obra_digital_backend.service.exception.InvalidFileName;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
  private final Path fileStorageLocation;

  //construtor que inicializa o fileStorageLocation, com base no diretório especificado na classe de configuração
  @Autowired
  public FileStorageService(FileStorageProperties fileStorageProperties) {
    //seta o caminho dos arquivos com base no application.properties
    this.fileStorageLocation = Paths
        .get(fileStorageProperties.getUploadDir())
        .toAbsolutePath()
        .normalize();

    try {
      //cria diretório com base no caminho setado
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String storeFile(MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      return storeFile(inputStream, file.getOriginalFilename());
    } catch (IOException e) {
      throw new FileStorageCreationException("Não foi possível salvar o arquivo");
    }
  }

  public String storeFileWithHash(MultipartFile file) {
    try {
      String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
      if (ext != null) ext = ext.toLowerCase();

      byte[] finalBytes = file.getBytes();

      if ("webp".equals(ext)) {
        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(finalBytes)) {
          java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(bais);
          if (image != null) {
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
              javax.imageio.ImageIO.write(image, "png", baos);
              finalBytes = baos.toByteArray();
              ext = "png";
            }
          }
        } catch (Exception e) {
          System.err.println("Aviso: Falha ao converter WebP para PNG, mantendo original. " + e.getMessage());
        }
      }

      String hash = calculateHash(finalBytes);
      String fileName = hash + (ext != null && !ext.isEmpty() ? "." + ext : "");

      Path targetLocation = this.fileStorageLocation.resolve(fileName);
      if (!Files.exists(targetLocation)) {
        Files.write(targetLocation, finalBytes);
      }
      return fileName;
    } catch (Exception e) {
      throw new FileStorageCreationException("Não foi possível salvar o arquivo");
    }
  }

  public String calculateHash(byte[] fileBytes) throws Exception {
    java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(fileBytes);
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  // Novo método para facilitar seeding a partir de InputStream (ex: resources)
  public String storeFile(InputStream inputStream, String originalFileName) {
    String cleanedFileName = StringUtils.cleanPath(Objects.requireNonNull(originalFileName));

    if(cleanedFileName.contains("..")) {
      throw new InvalidFileName("Nome do arquivo inválido, renomeie e tente novamente.");
    }

    // Gera nome único com UUID para evitar colisões entre arquivos com mesmo nome
    String uniqueFileName = UUID.randomUUID() + "_" + cleanedFileName;

    // efetivamente salva o arquivo no diretório
    try {
      // constrói o caminho de destino, usando o método resolve
      Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
      Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return uniqueFileName;
    } catch (IOException e) {
      throw new FileStorageCreationException("Não foi possível salvar o arquivo");
    }
  }

  // método que carrega um arquivo do storage como um Resource
  public Resource loadFileAsResource(String fileName) {
    try {
      // tenta encontrar o arquivo dentro do storage
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri()); //UrlResource pode lançar uma MalformedURLException

      if(resource.exists()) {
        return resource;
      } else {
        throw new FileNotFoundInStorageException("Arquivo não encontrado no storage: " + fileName);
      }
    } catch (MalformedURLException e) {
      throw new FileNotFoundInStorageException("Arquivo não encontrado no storage: " + fileName);
    }
  }

  // método que deleta um arquivo do storage pelo nome (usado ao remover fotos do diário)
  public void deleteFile(String fileName) {
    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      // Apenas loga o erro sem interromper a operação principal
      System.err.println("Não foi possível deletar o arquivo: " + fileName + " — " + e.getMessage());
    }
  }
}
