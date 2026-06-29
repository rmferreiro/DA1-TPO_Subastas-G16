package tpo.g16.blackwood.network;

import android.util.Log;
import com.google.gson.Gson;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import tpo.g16.blackwood.network.models.websocket.*;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class AuctionStompClient {

    private static final String TAG = "AuctionStompClient";
    private StompClient stompClient;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private static final String BASE_URL = tpo.g16.blackwood.network.ApiConfig.BASE_URL
            .replace("http://", "ws://")
            .replace("https://", "wss://") + "ws-auction";

    public interface AuctionEventListener {
        void onConnected();
        void onStateUpdate(AuctionStateUpdate update);
        void onUsersUpdate(AuctionUsersUpdate update);
        void onLotChange(LotChangeUpdate update);
        void onAuctionFinished(AuctionFinishedUpdate update);
        void onBidError(BidError error);
        void onDisconnected();
        void onError(String message);
    }

    private interface MessageHandler {
        void accept(String msg);
    }

    public void connect(String auctionId, String authToken, AuctionEventListener listener) {
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + authToken));

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BASE_URL);

        disposables.add(
            stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    switch (event.getType()) {
                        case OPENED:
                            subscribeToTopics(auctionId, listener);
                            listener.onConnected();
                            break;
                        case CLOSED:
                            listener.onDisconnected();
                            break;
                        case ERROR:
                            listener.onError(event.getException() != null
                                ? event.getException().getMessage() : "Error de conexión");
                            break;
                    }
                })
        );

        stompClient.connect(headers);
    }

    private void subscribeToTopics(String auctionId, AuctionEventListener listener) {
        Gson gson = new Gson();

        // IMPORTANTE: suscribir a .users PRIMERO para estar listo cuando
        // el backend broadcastee la lista al recibir la suscripción a .state
        sub("/topic/auction." + auctionId + ".users", msg ->
            listener.onUsersUpdate(gson.fromJson(msg, AuctionUsersUpdate.class)));

        // Estado del lote (precio, líder, tiempo) — este dispara el broadcast de presencia en el backend
        sub("/topic/auction." + auctionId + ".state", msg ->
            listener.onStateUpdate(gson.fromJson(msg, AuctionStateUpdate.class)));

        // Cambio de lote
        sub("/topic/auction." + auctionId + ".lot_change", msg ->
            listener.onLotChange(gson.fromJson(msg, LotChangeUpdate.class)));

        // Subasta finalizada
        sub("/topic/auction." + auctionId + ".finished", msg ->
            listener.onAuctionFinished(gson.fromJson(msg, AuctionFinishedUpdate.class)));

        // Errores privados de puja
        sub("/user/queue/bid_error", msg ->
            listener.onBidError(gson.fromJson(msg, BidError.class)));
    }

    private void sub(String topic, MessageHandler handler) {
        disposables.add(
            stompClient.topic(topic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    msg -> handler.accept(msg.getPayload()),
                    err -> Log.e(TAG, "Error en topic " + topic, err)
                )
        );
    }

    public void sendBid(String auctionId, double amount, Long medioPagoId) {
        if (stompClient == null || !stompClient.isConnected()) return;
        BidRequest bid = new BidRequest();
        bid.amount = amount;
        bid.medioPagoId = medioPagoId;
        disposables.add(
            stompClient.send(
                "/app/auction." + auctionId + ".bid",
                new Gson().toJson(bid)
            ).subscribeOn(Schedulers.io()).subscribe(
                () -> {},
                err -> Log.e(TAG, "Error enviando puja", err)
            )
        );
    }

    public boolean isConnected() {
        return stompClient != null && stompClient.isConnected();
    }

    public void disconnect() {
        disposables.clear();
        if (stompClient != null) stompClient.disconnect();
    }
}
