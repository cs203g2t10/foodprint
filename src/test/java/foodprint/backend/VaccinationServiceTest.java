package foodprint.backend;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;

import foodprint.backend.config.JwtTokenUtil;
import foodprint.backend.exceptions.VaccinationValidationException;
import foodprint.backend.model.User;
import foodprint.backend.service.UserService;
import foodprint.backend.service.VaccinationService;

@ExtendWith(MockitoExtension.class)
public class VaccinationServiceTest {

    @InjectMocks
    VaccinationService vaccinationService;

    @Mock
    UserService userService;

    @Mock
    JwtTokenUtil jwtUtil;

    @Mock
    HttpClient httpClient;

    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    void init() {
    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_valid_success() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";
        HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

        String json = "{'status':'Valid', 'patientName': 'Bobby Mock', 'patientBirthDate': '2000-01-01'}".replace("'", "\"");

        ObjectMapper actualMapper = new ObjectMapper();
        JsonNode node = actualMapper.readTree(json);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(httpResponse);
        when(objectMapper.readTree(any(String.class))).thenReturn(node);
        when(userService.protectedGetUser(any(Long.class))).thenReturn(user);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("<VALID JWT TOKEN>");

        String answer = vaccinationService.validateVaccination(user, oaFileContent);

        verify(userService).protectedGetUser(1L);
        verify(httpResponse).body();
        verify(objectMapper).readTree(json);
        verify(jwtUtil).generateAccessToken(user);

        assertEquals("<VALID JWT TOKEN>", answer);

    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_invalidJson_fail() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";
        HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

        String json = "INVALID JSON HERE".replace("'", "\"");

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(httpResponse);
        when(objectMapper.readTree(any(String.class))).thenThrow(JsonParseException.class);

        var ex = assertThrows(VaccinationValidationException.class, () -> vaccinationService.validateVaccination(user, oaFileContent));
        assertEquals(ex.getMessage(), "Unable to process vaccination validation response");

        verify(httpResponse).body();
        verify(objectMapper).readTree(json);

    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_statusless_fail() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";
        HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

        String json = "{'patientName': 'Bobby Mock', 'patientBirthDate': '2000-01-01'}".replace("'", "\"");

        ObjectMapper actualMapper = new ObjectMapper();
        JsonNode node = actualMapper.readTree(json);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(httpResponse);
        when(objectMapper.readTree(any(String.class))).thenReturn(node);

        var ex = assertThrows(VaccinationValidationException.class, () -> vaccinationService.validateVaccination(user, oaFileContent));
        assertEquals(ex.getMessage(), "Obtaining malformed responses from upstream service");

        verify(httpResponse).body();
        verify(objectMapper).readTree(json);

    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_validationFail_fail() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";
        HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

        String json = "{'status': 'Invalid', 'reason': 'Bad Certificate'}".replace("'", "\"");

        ObjectMapper actualMapper = new ObjectMapper();
        JsonNode node = actualMapper.readTree(json);

        when(httpResponse.body()).thenReturn(json);
        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(httpResponse);
        when(objectMapper.readTree(any(String.class))).thenReturn(node);

        var ex = assertThrows(VaccinationValidationException.class, () -> vaccinationService.validateVaccination(user, oaFileContent));
        assertEquals(ex.getMessage(), "Vaccination certificate invalid: Bad Certificate");

        verify(httpResponse).body();
        verify(objectMapper).readTree(json);

    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_IOException_fail() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenThrow(IOException.class);

        var ex = assertThrows(VaccinationValidationException.class, () -> vaccinationService.validateVaccination(user, oaFileContent));
        assertEquals(ex.getMessage(), "Unable to validate vaccination status.");

    }

    @Test
    @SuppressWarnings("unchecked")
    void validateVaccination_InterruptedException_fail() throws InterruptedException, IOException {

        User user = new User("bobby@gmail.com", "SuperSecurePassw0rd", "123");
        ReflectionTestUtils.setField(user, "id", 1L);
        String oaFileContent = "<MOCK VACCINATION CERT>";

        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenThrow(InterruptedException.class);

        var ex = assertThrows(VaccinationValidationException.class, () -> vaccinationService.validateVaccination(user, oaFileContent));
        assertEquals(ex.getMessage(), "Unable to validate vaccination status due to server error.");

    }

}
