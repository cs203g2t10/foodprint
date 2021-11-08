package foodprint.backend.service;

import static org.apache.http.entity.ContentType.IMAGE_BMP;
import static org.apache.http.entity.ContentType.IMAGE_GIF;
import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;

@Service
public class PictureService  {
    private final FileStore fileStore;
    private final PictureRepo repository;

    @Autowired
    public PictureService(FileStore fileStore, PictureRepo repository) {
        this.fileStore = fileStore;
        this.repository = repository;
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public Picture get(Long id) {
        Optional<Picture> picture = repository.findById(id);
        return picture.orElseThrow(() -> new NotFoundException("Picture not found"));
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture savePicture(String title, String description, MultipartFile file) {

        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file");
        }

        //Check if the file is an image
        if (!Arrays.asList(IMAGE_PNG.getMimeType(),
                IMAGE_BMP.getMimeType(),
                IMAGE_GIF.getMimeType(),
                IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("FIle uploaded is not an image");
        }

        // Get file metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        
        // Upload image to Amazon S3
        UUID uuid = UUID.randomUUID();
        String path = String.format("foodprint-amazon-storage/%s", uuid);
        String fileName = String.format("%s", file.getOriginalFilename());
        try {
            fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
        path = String.format("%s", uuid);
        String url = String.format("%s%s/%s", "https://foodprint-amazon-storage.s3.ap-southeast-1.amazonaws.com/", path, fileName.replace(" ", "+"));
        
        // Save picture in database
        Picture picture = new Picture(title, description, path, fileName, url);
        repository.saveAndFlush(picture);
        return picture;
    }

    public byte[] downloadPictureImage(Long id) {
        Picture picture = repository.findById(id).orElseThrow(() -> new NotFoundException("Image with this ID was not found"));
        return fileStore.download(picture.getImagePath(), picture.getImageFileName());
    }

    @PreAuthorize("hasAnyAuthority('FP_USER')")
    public String getPictureById(Long id) {
        Optional<Picture> picture = repository.findById(id);
        if (picture.isEmpty()) {
            throw new NotFoundException("Picture not found.");
        }
        
        return String.format("%s%s/%s", "https://foodprint-amazon-storage.s3.ap-southeast-1.amazonaws.com/", picture.get().getImagePath(), picture.get().getImageFileName().replace(" ", "+"));
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public void deletePicture(Long id) {
        Picture picture = get(id);
        repository.delete(picture);
        try {
            this.get(id);
            throw new DeleteFailedException("Picture could not be deleted");
        } catch (NotFoundException e) {
            return;
        }
    }

    @PreAuthorize("hasAnyAuthority('FP_ADMIN', 'FP_MANAGER')")
    public Picture updatedPicture(Long pictureId, Picture newPicture) {
        Picture oldPicture = get(pictureId);
        oldPicture.setDescription(newPicture.getDescription());
        oldPicture.setTitle(newPicture.getTitle());
        oldPicture = repository.saveAndFlush(oldPicture);
        return oldPicture;
    }
}