package tpo.g16.blackwood.network.model;

public class RegistroRequest {
    private String nombre;
    private String documento;
    private String direccion;
    private Integer paisId;
    private String email;
    private String password;
    private String fotoDocFrente;
    private String fotoDocDorso;

    public RegistroRequest(String nombre, String documento, String direccion, Integer paisId, String email, String password, String fotoDocFrente, String fotoDocDorso) {
        this.nombre = nombre;
        this.documento = documento;
        this.direccion = direccion;
        this.paisId = paisId;
        this.email = email;
        this.password = password;
        this.fotoDocFrente = fotoDocFrente;
        this.fotoDocDorso = fotoDocDorso;
    }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public String getDireccion() { return direccion; }
    public Integer getPaisId() { return paisId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFotoDocFrente() { return fotoDocFrente; }
    public String getFotoDocDorso() { return fotoDocDorso; }
}
