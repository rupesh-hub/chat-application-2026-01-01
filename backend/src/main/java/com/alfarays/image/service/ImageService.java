package com.alfarays.image.service;

import com.alfarays.image.entity.Image;
import com.alfarays.image.mapper.ImageMapper;
import com.alfarays.image.model.ImageResponse;
import com.alfarays.image.repository.ImageRepository;
import com.alfarays.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private static final long MAX_FILE_SIZE = 10_485_760; // 10MB
    private static final String ALLOWED_TYPES = "image/jpeg,image/png,image/gif,image/webp";
    private static final String PUBLIC_UPLOAD_PATH = "/uploads/";
    private final ImageRepository imageRepository;

    @Value("${file.upload.directory}")
    private String uploadDir;

    private Path getUploadsDirectory() throws IOException {
        Path backendRoot = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        Path uploadsPath = backendRoot
                .resolve(uploadDir)
                .normalize();

        if(Files.notExists(uploadsPath)) {
            log.info("Creating uploads directory at: {}", uploadsPath);
            Files.createDirectories(uploadsPath);
        }

        if(!Files.isWritable(uploadsPath)) {
            throw new IOException("Upload directory is not writable: " + uploadsPath);
        }

        return uploadsPath;
    }

    public Image upload(MultipartFile file, User user) throws IOException {
        validateFile(file);

        Path filePath = saveFileToDisk(file, user);
        String fileDownloadUri = generateFileUrl(filePath);

        Image image = buildImageEntity(file, user, fileDownloadUri, filePath.getFileName().toString());
        log.info("Image uploaded successfully for user: {}", user.getEmail());

        return imageRepository.save(image);
    }

    public List<ImageResponse> get(Long userId) {
        return imageRepository.findByUserId(userId)
                .stream()
                .map(ImageMapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean delete(Image existingImage) throws IOException {
        Path filePath = Paths.get(existingImage.getPath());
        Files.deleteIfExists(filePath);

        imageRepository.delete(existingImage);
        log.info("Image deleted successfully: {}", existingImage.getFilename());
        return true;
    }

    public Image update(MultipartFile newFile, Image existingImage) throws IOException {
        if(newFile == null || newFile.isEmpty()) {
            return existingImage;
        }

        Path oldFilePath = Paths.get(existingImage.getPath());
        Files.deleteIfExists(oldFilePath);

        Path newFilePath = saveFileToDisk(newFile, existingImage.getUser());
        String newDownloadUri = generateFileUrl(newFilePath);

        existingImage.setPath(newDownloadUri);
        existingImage.setFilename(newFilePath.getFileName().toString());
        existingImage.setType(newFile.getContentType());
        existingImage.setSize(newFile.getSize());

        return imageRepository.save(existingImage);
    }

    private Path saveFileToDisk(MultipartFile file, User user) throws IOException {
        Path uploadsPath = getUploadsDirectory();

        String dateFolder = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        Path dateDir = uploadsPath.resolve(dateFolder);
        Files.createDirectories(dateDir);

        String filename = buildFilename(file, user);
        Path targetPath = dateDir.resolve(filename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved at {}", targetPath);
        return targetPath;
    }

    private String buildFilename(MultipartFile file, User user) {
        String original = file.getOriginalFilename();
        String cleanName = original.replaceAll("[^a-zA-Z0-9._-]", "_");

        return user.getId()
                + "_" + UUID.randomUUID().toString().substring(0, 8)
                + "_" + cleanName;
    }

    private String generateFileUrl(Path filePath) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(PUBLIC_UPLOAD_PATH)
                .path(filePath.getParent().getFileName().toString() + "/")
                .path(filePath.getFileName().toString())
                .toUriString();
    }

    private void validateFile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if(file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB");
        }
        if(!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private Image buildImageEntity(MultipartFile file, User user, String uri, String filename) {
        Image image = new Image();
        image.setUser(user);
        image.setPath(uri);
        image.setFilename(filename);
        image.setType(file.getContentType());
        image.setSize(file.getSize());
        return image;
    }
}
