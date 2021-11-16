package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.springframework.mock.web.MockMultipartFile;
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
    private String pictureURL;
    
    @BeforeEach
    void init() {
        picture = new Picture("image", "desc", "Path", "file", "www.file.com");
        pictureURL = "https://foodprint-amazon-storage.s3.ap-southeast-1.amazonaws.com/Path/file";
        pictureId = 1L;
        newPicture = new Picture("anotherimage", "desc", "Path", "file", "www.file.com");
    }

    @Test
    void getPictureById_PictureFound_ReturnPicture() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        Picture getPicture = pictureService.get(pictureId);
        assertEquals(picture, getPicture);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPicture_PictureNotFound_ReturnException() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            pictureService.get(pictureId);
        } catch(NotFoundException e) {
            exceptionMsg = e.getMessage();
        }
        
        assertEquals("Picture not found", exceptionMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPictureById_PictureFound_ReturnStringUrl() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));

        String pictureUrl = pictureService.getPictureById(pictureId);
        assertEquals(pictureURL, pictureUrl);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void getPictureById_PictureNotFound_ReturnException() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            pictureService.getPictureById(pictureId);
        } catch(NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Picture not found.", exceptionMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundAndDeleted_Success() {
        ReflectionTestUtils.setField(picture, "pictureId", pictureId);

        doNothing().when(pictureRepo).delete(picture);
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture)).thenReturn(Optional.empty());
        
        String exceptionMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(pictureRepo).delete(picture);
        verify(pictureRepo, times(2)).findById(pictureId);
    }

    @Test
    void deletePicture_PictureNotFound_ReturnException() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch(NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Picture not found", exceptionMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void deletePicture_PictureFoundButNotDeleted_ReturnException() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.of(picture));
        doNothing().when(pictureRepo).delete(any(Picture.class));

        pictureRepo.saveAndFlush(picture);
        String exceptionMsg = "";
        try {
            pictureService.deletePicture(pictureId);
        } catch(DeleteFailedException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Picture could not be deleted", exceptionMsg);
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
        
        assertEquals(picture, updatedPicture);
        verify(pictureRepo).findById(pictureId);
        verify(pictureRepo, times(2)).saveAndFlush(picture);
    }

    @Test
    void updatePicture_PictureNotFound_ReturnException() {
        when(pictureRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        String exceptionMsg = "";
        try {
            pictureService.updatedPicture(pictureId, newPicture);
        } catch(NotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("Picture not found", exceptionMsg);
        verify(pictureRepo).findById(pictureId);
    }

    @Test
    void savePicture_FileEmpty_ReturnException() {
        MockMultipartFile file = new MockMultipartFile("newPicture", "newPicture.png", "image", "".getBytes());
        String exceptionMsg = "";
        try {
            pictureService.savePicture("title", "description", file);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }
        assertEquals("Cannot upload empty file", exceptionMsg);
    }

    @Test
    void savePicture_FileNotImage_ReturnException() {
        MockMultipartFile file = new MockMultipartFile("newPicture", "newPicture.txt", "text/plain", "content".getBytes());
        String exceptionMsg = "";
        try {
            pictureService.savePicture("title", "description", file);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }
        assertEquals("File uploaded is not an image", exceptionMsg);
    }

    @Test
    void savePicture_FileValid_Success() {
        MockMultipartFile file = new MockMultipartFile("newPicture", "newPicture.png", "image/png", "content".getBytes());
        Picture picture = new Picture("title", "description", "path", "fileName", "url");
        when(pictureRepo.saveAndFlush(any(Picture.class))).thenReturn(picture);

        String exceptionMsg = "";
        try {
            pictureService.savePicture("title", "description", file);
        } catch (Exception e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals("", exceptionMsg);
        verify(pictureRepo).saveAndFlush(any(Picture.class));
    }

}
