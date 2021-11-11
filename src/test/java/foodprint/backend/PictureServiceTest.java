package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    FileStore fileStore;

    @InjectMocks
    PictureService pictureService;

    private Picture picture;
    private Long pictureId;
    private Picture newPicture;
    
    @BeforeEach
    void init() {
        picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        pictureId = 1L;
        newPicture = new Picture("anotherimage", "desc", "Path", "file", "www.file.com");
    }

    @Test
    void getPictureById_PictureFound_ReturnPicture() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        Picture getPicture = pictureService.get(pictureId);
        assertNotNull(getPicture);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPicture_PictureNotFound_ReturnError() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String errorMsg = "";
        try {
            pictureService.get(pictureId);
        } catch(NotFoundException e) {
            errorMsg = e.getMessage();
        }
        
        assertEquals("Picture not found", errorMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPictureById_PictureFound_ReturnStringUrl() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        String pictureUrl = pictureService.getPictureById(pictureId);
        assertNotNull(pictureUrl);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPictureById_PictureNotFound_ReturnError() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String errorMsg = "";
        try {
            pictureService.getPictureById(pictureId);
        } catch(NotFoundException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Picture not found.", errorMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundAndDeleted_Return() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        doNothing().when(pictureRepo).delete(picture);
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture)).thenReturn(Optional.empty());
        
        pictureRepo.saveAndFlush(picture);
        String errorMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }

        assertEquals("", errorMsg);
        verify(pictureRepo).delete(picture);
        verify(pictureRepo, times(2)).findById(pictureId);
    }

    @Test
    void deletePicture_PictureNotFound_ReturnError() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String errorMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch(NotFoundException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Picture not found", errorMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundButNotDeleted_ReturnError() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));
        doNothing().when(pictureRepo).delete(any(Picture.class));

        pictureRepo.saveAndFlush(picture);
        String errorMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch(DeleteFailedException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Picture could not be deleted", errorMsg);
        verify(pictureRepo, times(2)).findById(pictureId);
        verify(pictureRepo).delete(picture);
    }

    @Test
    void updatePicture_PictureFoundAndUpdatedSuccessfully_ReturnPicture() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

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
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String errorMsg = "";
        try {
            pictureService.updatedPicture(pictureId, newPicture);
        } catch(NotFoundException e) {
            errorMsg = e.getMessage();
        }

        assertEquals("Picture not found", errorMsg);
        verify(pictureRepo).findById(pictureId);
    }
}
