package foodprint.backend.service;

import static org.apache.http.entity.ContentType.IMAGE_BMP;
import static org.apache.http.entity.ContentType.IMAGE_GIF;
import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.config.BucketName;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;


//TODO Authorities
@Service
public class PictureService  {
    private final FileStore fileStore;
    private final PictureRepo repository;

    @Autowired
    public PictureService(FileStore fileStore, PictureRepo repository) {
        this.fileStore = fileStore;
        this.repository = repository;
    }

    public Picture get(Long id) {
        Optional<Picture> picture = repository.findById(id);
        return picture.orElseThrow(() -> new NotFoundException("Picture not found"));
    }

    public Picture savePicture(String title, String description, MultipartFile file) {
        //check if the file is empty
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
        //get file metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        //Save Image in S3 and then save Picture in the database
        UUID uuid = UUID.randomUUID();
        String path = String.format("%s/%s", BucketName.PICTURE_IMAGE.getBucketName(), uuid);
        String fileName = String.format("%s", file.getOriginalFilename());
        try {
            fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
        // Picture Picture = Picture.builder()
        //         .description(description)
        //         .title(title)
        //         .imagePath(path)
        //         .imageFileName(fileName)
        //         .build();
        path = String.format("%s", uuid);
        Picture picture = new Picture(title, description, path, fileName);
        repository.saveAndFlush(picture);
        return picture;
    }

    public byte[] downloadPictureImage(Long id) {
        Picture picture = repository.findById(id).get();
        return fileStore.download(picture.getImagePath(), picture.getImageFileName());
    }

    public List<Picture> getAllPictures() {
        List<Picture> pictures = new ArrayList<>();
        repository.findAll().forEach(pictures::add);
        return 
        pictures;
    }

    public String getPictureById(Long id) {
        Optional<Picture> picture = repository.findById(id);
        if (picture.isEmpty()) {
            throw new NotFoundException("Picture not found.");
        }
        
        String url = String.format("%s%s/%s", "https://foodprint-amazon-storage.s3.ap-southeast-1.amazonaws.com/", picture.get().getImagePath(), picture.get().getImageFileName().replace(" ", "+"));
        return url;
    }

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
}