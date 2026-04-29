package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
