package ar.edu.uade.grupo16.subastas.enums;

public enum CategoriaUsuario {
    COMUN("comun", 1),
    ESPECIAL("especial", 2),
    PLATA("plata", 3),
    ORO("oro", 4),
    PLATINO("platino", 5);

    private final String valor;
    private final int nivel;

    CategoriaUsuario(String valor, int nivel) {
        this.valor = valor;
        this.nivel = nivel;
    }

    public String getValor() {
        return valor;
    }

    public int getNivel() {
        return nivel;
    }

    /**
     * Un usuario puede acceder a subastas de categoría menor o igual a la suya.
     */
    public boolean puedeAcceder(CategoriaUsuario categoriaSubasta) {
        return this.nivel >= categoriaSubasta.nivel;
    }

    public static CategoriaUsuario fromValor(String valor) {
        for (CategoriaUsuario c : values()) {
            if (c.valor.equalsIgnoreCase(valor)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Categoría no válida: " + valor);
    }
}
