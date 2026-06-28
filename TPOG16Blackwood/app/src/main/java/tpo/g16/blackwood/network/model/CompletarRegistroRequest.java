package tpo.g16.blackwood.network.model;

import java.util.List;

public class CompletarRegistroRequest {
    private String email;
    private String password;
    private List<MedioPagoRequest> mediosPago;

    public CompletarRegistroRequest(String email, String password, List<MedioPagoRequest> mediosPago) {
        this.email = email;
        this.password = password;
        this.mediosPago = mediosPago;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<MedioPagoRequest> getMediosPago() { return mediosPago; }
}
