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
        String path = String.format("%s/%s", BucketName.PICTURE_IMAGE.getBucketName(), UUID.randomUUID());
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
        Picture picture = new Picture(title, description, path, fileName);
        repository.save(picture);
        return repository.findByTitle(picture.getTitle());
    }

    public byte[] downloadPictureImage(Long id) {
        Picture Picture = repository.findById(id).get();
        return fileStore.download(Picture.getImagePath(), Picture.getImageFileName());
    }

    public List<Picture> getAllPictures() {
        List<Picture> Pictures = new ArrayList<>();
        repository.findAll().forEach(Pictures::add);
        return Pictures;
    }
}