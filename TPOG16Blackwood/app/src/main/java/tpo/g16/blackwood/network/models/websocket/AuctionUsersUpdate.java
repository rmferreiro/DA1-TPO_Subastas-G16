package tpo.g16.blackwood.network.models.websocket;

import java.util.List;

public class AuctionUsersUpdate {
    public int count;
    public List<UserInfo> participants;

    public static class UserInfo {
        public String username;
        public String initials;
    }
}
