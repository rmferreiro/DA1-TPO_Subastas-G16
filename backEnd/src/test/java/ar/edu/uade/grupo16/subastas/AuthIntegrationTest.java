package ar.edu.uade.grupo16.subastas;

import ar.edu.uade.grupo16.subastas.dto.request.LoginRequest;
import ar.edu.uade.grupo16.subastas.dto.request.RegistroRequest;
import ar.edu.uade.grupo16.subastas.dto.response.AuthResponse;
import ar.edu.uade.grupo16.subastas.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegistroYLoginExitoso() {
        String email = "test.integration@email.com";
        String password = "Password123!";

        // 1. Registrar
        RegistroRequest registro = RegistroRequest.builder()
                .nombre("Usuario Integracion")
                .documento("99999999")
                .direccion("Calle Falsa 123")
                .paisId(1)
                .email(email)
                .password(password)
                .fotoDocFrente("ZHVtbXk=") // dummy base64
                .fotoDocDorso("ZHVtbXk=")  // dummy base64
                .build();

        AuthResponse registroResponse = authService.registrar(registro);
        assertNotNull(registroResponse);
        assertNotNull(registroResponse.getAccessToken());
        assertEquals(email, registroResponse.getEmail());

        // 2. Intentar Login
        LoginRequest login = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        AuthResponse loginResponse = authService.login(login);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        assertEquals(email, loginResponse.getEmail());
    }
}
