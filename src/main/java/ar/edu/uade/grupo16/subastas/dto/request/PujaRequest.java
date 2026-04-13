package ar.edu.uade.grupo16.subastas.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PujaRequest {

    @NotNull(message = "El ID del item es obligatorio")
    private Integer itemId;

    @NotNull(message = "El importe es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe debe ser mayor a 0")
    private BigDecimal importe;

    @NotNull(message = "El ID del medio de pago es obligatorio")
    private Long medioPagoId;
}
