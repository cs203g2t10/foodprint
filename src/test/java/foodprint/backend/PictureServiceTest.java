package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.model.Picture;
import foodprint.backend.model.PictureRepo;
import foodprint.backend.service.FileStore;
import foodprint.backend.service.PictureService;

@ExtendWith(MockitoExtension.class)
public class PictureServiceTest {
    
    @Mock
    PictureRepo pictureRepo;

    @InjectMocks
    FileStore fileStore;

    @InjectMocks
    PictureService pictureService;

    @Test
    void getPictureById_PictureFound_ReturnPicture() {
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 2L;
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        Picture getPicture = pictureService.get(pictureId);
        assertNotNull(getPicture);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPicture_PictureNotFound_ReturnError() {
        Long pictureId = 1L;

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            pictureService.get(pictureId);
        } catch(NotFoundException e) {
            assertEquals("Picture not found", e.getMessage());
        }
        
        verify(pictureRepo).findById(pictureId);
    }

    //Integration testing
    // @Test
    // void savePicture_FileNotEmptyAndAnImage_FileUploaded_ReturnPicture() {
    //     String title = "title";
    //     String description = "desc";
    //     try {
    //         InputStream inputstream = new FileInputStream("c:\\data\\input-text.txt");
    //         MockMultipartFile file = new MockMultipartFile("name", "originalFilename", IMAGE_PNG.getMimeType(), inputstream);
    //         Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
       
    //         when(pictureRepo.saveAndFlush(any(Picture.class))).thenReturn(picture);
    //         fileStore.upload(path, fileName, Optional.of(metadata), file.getInputStream());

    //         Picture savedPicture = pictureService.savePicture(title, description, file);
    //     } catch(IOException e) {
    //         throw new IllegalStateException("Failed to upload file", e);
    //     }
    // }

    @Test
    void getAllPictures_PicturesFound_ReturnPictureList() {
        List<Picture> pictureList = new ArrayList<>();
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        pictureList.add(picture);

        when(pictureRepo.findAll()).thenReturn(pictureList);

        List<Picture> getAllPictures = pictureService.getAllPictures();
        assertNotNull(getAllPictures);
        verify(pictureRepo).findAll();
    }

    @Test
    void getPictureById_PictureFound_ReturnStringUrl() {
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 1L;
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        String pictureUrl = pictureService.getPictureById(pictureId);
        assertNotNull(pictureUrl);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPictureById_PictureNotFound_ReturnError() {
        Long pictureId = 1L;

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            pictureService.getPictureById(pictureId);
        } catch(NotFoundException e) {
            assertEquals("Picture not found.", e.getMessage());
        }

        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundAndDeleted_Return() {
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 1L;
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        doNothing().when(pictureRepo).delete(picture);
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture)).thenReturn(Optional.empty());
        
        pictureRepo.saveAndFlush(picture);
        pictureService.deletePicture(pictureId);
        verify(pictureRepo).delete(picture);
        verify(pictureRepo, times(2)).findById(pictureId);
    }

    @Test
    void deletePicture_PictureNotFound_ReturnError() {
        Long pictureId = 1L;

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            pictureService.deletePicture(pictureId);
        } catch(NotFoundException e) {
            assertEquals("Picture not found", e.getMessage());
        }

        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundButNotDeleted_ReturnError() {
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 1L;

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));
        doNothing().when(pictureRepo).delete(any(Picture.class));

        pictureRepo.saveAndFlush(picture);
        try {
            pictureService.deletePicture(pictureId);
        } catch(DeleteFailedException e) {
            assertEquals("Picture could not be deleted", e.getMessage());
        }

        verify(pictureRepo, times(2)).findById(pictureId);
        verify(pictureRepo).delete(picture);
    }

    @Test
    void updatePicture_PictureFoundAndUpdatedSuccessfully_ReturnPicture() {
        Picture picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        Long pictureId = 1L;
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);
        Picture newPicture = new Picture("anotherimage", "desc", "Path", "file", "www.file.com");

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));
        when(pictureRepo.saveAndFlush(any(Picture.class))).thenReturn(picture);

        pictureRepo.saveAndFlush(picture);
        Picture updatedPicture = pictureService.updatedPicture(pictureId, newPicture);
        assertNotNull(updatedPicture);
        verify(pictureRepo).findById(pictureId);
        verify(pictureRepo, times(2)).saveAndFlush(picture);
    }

    @Test
    void updatePicture_PictureNotFound_ReturnError() {
        Long pictureId = 1L;
        Picture newPicture = new Picture("anotherimage", "desc", "Path", "file", "www.file.com");

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        try {
            Picture updatedPicture = pictureService.updatedPicture(pictureId, newPicture);
        } catch(NotFoundException e) {
            assertEquals("Picture not found", e.getMessage());
        }

        verify(pictureRepo).findById(pictureId);
    }
}
