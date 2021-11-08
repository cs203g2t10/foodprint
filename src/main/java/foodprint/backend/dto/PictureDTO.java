package foodprint.backend.dto;

import org.springframework.web.multipart.MultipartFile;

public class PictureDTO {

    private String title;

    private String description;

    private String url;

    public PictureDTO(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public PictureDTO() {
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


    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    


    
}
