package ar.edu.uade.grupo16.subastas.enums;

public enum EstadoSubasta {
    PENDIENTE("PENDIENTE"),
    ACTIVA("ACTIVA"),
    FINALIZADA("FINALIZADA");

    private final String valor;

    EstadoSubasta(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static EstadoSubasta fromValor(String valor) {
        for (EstadoSubasta e : values()) {
            if (e.valor.equalsIgnoreCase(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de subasta no válido: " + valor);
    }
}
