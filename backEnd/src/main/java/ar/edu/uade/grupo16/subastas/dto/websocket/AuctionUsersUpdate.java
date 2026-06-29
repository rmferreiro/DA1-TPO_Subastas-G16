package ar.edu.uade.grupo16.subastas.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionUsersUpdate {
    private int count;
    private List<UserInfo> participants; // nombre + iniciales para el círculo de UI

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String username;
        private String initials;  // calculadas en backend: "Juan Pérez" → "JP"
    }
}
