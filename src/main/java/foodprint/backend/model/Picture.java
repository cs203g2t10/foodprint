package foodprint.backend.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.springframework.transaction.annotation.EnableTransactionManagement;

@Entity
@Table
@EnableTransactionManagement
public class Picture implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pictureId" )
    private Long pictureId;

    @Column(name = "title" )
    @NotEmpty(message = "Picture title should not be empty")
    private String title;

    @Column(name = "description" )
    @NotEmpty(message = "Picture description should not be empty")
    private String description;

    @Column(name = "imagePath" )
    private String imagePath;

    @Column(name = "imageFileName" )
    private String imageFileName;

    @Column(name = "url")
    private String url;

    public Picture(String title, String description, String imagePath, String imageFileName, String url) {
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.imageFileName = imageFileName;
        this.url = url;
    }

    public Picture(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Picture() {
    }

    public Long getId() {
        return this.pictureId;
    }

    public void setId(Long id) {
        this.pictureId = id;
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

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageFileName() {
        return this.imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public Picture id(Long id) {
        setId(id);
        return this;
    }

    public Picture title(String title) {
        setTitle(title);
        return this;
    }

    public Picture description(String description) {
        setDescription(description);
        return this;
    }

    public Picture imagePath(String imagePath) {
        setImagePath(imagePath);
        return this;
    }

    public Picture imageFileName(String imageFileName) {
        setImageFileName(imageFileName);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Picture)) {
            return false;
        }
        Picture picture = (Picture) o;
        return Objects.equals(pictureId, picture.pictureId) && Objects.equals(title, picture.title) && Objects.equals(description, picture.description) && Objects.equals(imagePath, picture.imagePath) && Objects.equals(imageFileName, picture.imageFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pictureId, title, description, imagePath, imageFileName);
    }


}

   
