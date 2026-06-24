package tpo.g16.blackwood.network.models;

import java.math.BigDecimal;

public class ItemResultadoResponse {
    private Integer itemId;
    private Integer subastaId;
    private String productoDesc;
    private String subastaDesc;
    private String ganadorNombre;
    private boolean soyGanador;
    private BigDecimal importe;
    private Integer pujoId;

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getSubastaId() { return subastaId; }
    public void setSubastaId(Integer subastaId) { this.subastaId = subastaId; }

    public String getProductoDesc() { return productoDesc; }
    public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }

    public String getSubastaDesc() { return subastaDesc; }
    public void setSubastaDesc(String subastaDesc) { this.subastaDesc = subastaDesc; }

    public String getGanadorNombre() { return ganadorNombre; }
    public void setGanadorNombre(String ganadorNombre) { this.ganadorNombre = ganadorNombre; }

    public boolean isSoyGanador() { return soyGanador; }
    public void setSoyGanador(boolean soyGanador) { this.soyGanador = soyGanador; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public Integer getPujoId() { return pujoId; }
    public void setPujoId(Integer pujoId) { this.pujoId = pujoId; }
}
