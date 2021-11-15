package foodprint.backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import foodprint.backend.service.FileStore;

@ExtendWith(MockitoExtension.class)
public class FileStoreTest {
    
    @Mock
    AmazonS3 amazonS3;

    @InjectMocks
    FileStore fileStore;
    
    @BeforeEach
    void init() {
    }

    @Test
    void amazonS3_upload_success() {

        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(null);
        var optMetadata = Optional.of(Map.of("", ""));
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());


        assertDoesNotThrow(() -> {
            fileStore.upload("fakePath", "fakeFilename", optMetadata, anyInputStream);
        });

        verify(amazonS3).putObject(any(PutObjectRequest.class));

    }
}
