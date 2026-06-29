package tpo.g16.blackwood.network.models;

public class ClienteResponse {
    private Integer id;
    private String nombre;
    private String documento;
    private String direccion;
    private String pais;
    private String email;
    private String categoria;
    private String estado;
    private boolean tieneMedioPagoVerificado;
    private String ultimoMedioPago;
    private boolean esAdmin;

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
    public String getDireccion() { return direccion; }
    public String getPais() { return pais; }
    public String getEmail() { return email; }
    public String getCategoria() { return categoria; }
    public String getEstado() { return estado; }
    public boolean isTieneMedioPagoVerificado() { return tieneMedioPagoVerificado; }
    public String getUltimoMedioPago() { return ultimoMedioPago; }
    public boolean isEsAdmin() { return esAdmin; }
}
