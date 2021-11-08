package foodprint.backend.dto;

import org.springframework.web.multipart.MultipartFile;

public class UpdatePictureDTO {
    private String title;

    private String description;

    private MultipartFile pictureFile;


    public UpdatePictureDTO() {
    }

    public UpdatePictureDTO(String title, String description, MultipartFile pictureFile) {
        this.title = title;
        this.description = description;
        this.pictureFile = pictureFile;
    }


    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getPictureFile() {
        return this.pictureFile;
    }

    public void setPictureFile(MultipartFile pictureFile) {
        this.pictureFile = pictureFile;
    }
   
   
}
