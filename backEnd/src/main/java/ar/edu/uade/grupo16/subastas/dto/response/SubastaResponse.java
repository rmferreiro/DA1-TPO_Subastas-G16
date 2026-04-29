package ar.edu.uade.grupo16.subastas.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubastaResponse {
    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String categoria;
    private String ubicacion;
    private String moneda;
    private String descripcion;
    private String subastadorNombre;
    private Integer capacidadAsistentes;
    private Integer asistentesActuales;
    private boolean tieneDeposito;
    private boolean seguridadPropia;
}
