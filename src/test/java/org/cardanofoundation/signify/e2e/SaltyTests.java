package org.cardanofoundation.signify.e2e;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.State;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Salter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import com.goterl.lazysodium.LazySodiumJava;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;

class SaltyTests {
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SignifyClient client;

    @Test
    void saltyTest() throws Exception {
        String bran = Coring.randomPasscode();
        client = new SignifyClient(
                url,
                bran,
                Salter.Tier.low,
                bootUrl,
                null
        );
        client.boot();
        client.connect();
        State state = client.state().block();
        System.out.println(state.getAgent());

    }
}
