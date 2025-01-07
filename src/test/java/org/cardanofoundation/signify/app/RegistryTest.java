package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.aiding.Identifier;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.Registries;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.params.SaltyParams;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.http.HttpResponse;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistryTest {

    @Mock
    private SignifyClient mockedClient;
    @Mock
    private Identifier mockedIdentifiers;
    @Mock
    private Keeping.KeyManager mockedKeyManager;
    @Mock
    private Keeping.Keeper mockedKeeper;
    @InjectMocks
    private Registries registries;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registries = new Registries(mockedClient);
    }

    @Test
    @DisplayName("should create a registry")
    void shouldCreateRegistry() throws Exception {
        States.HabState hab = States.HabState.builder()
                .prefix("hab prefix")
                .state(States.State.builder()
                        .s("0")
                        .d("a digest")
                        .build())
                .build();

        when(mockedClient.getManager()).thenReturn(mockedKeyManager);
        when(mockedKeyManager.get(hab)).thenReturn(mockedKeeper);
        when(mockedKeeper.sign(any(byte[].class))).thenReturn(new Keeping.SignResult(Collections.singletonList("'a signature'")));


        when(mockedIdentifiers.get("a name")).thenReturn(hab);
        when(mockedClient.getIdentifier()).thenReturn(mockedIdentifiers);

        when(mockedKeeper.getAlgo()).thenReturn(Manager.Algos.salty);
        when(mockedKeeper.getParams()).thenReturn(SaltyParams.builder().build());

        HttpResponse<String> mockedResponse = mock(HttpResponse.class);
        when(mockedClient.fetch(eq("/identifiers/a name/registries"), eq("POST"), any(), isNull()))
                .thenReturn(mockedResponse);

        CreateRegistryArgs args = CreateRegistryArgs.builder()
                .name("a name")
                .registryName("a registry name")
                .nonce("")
                .build();
        var actual = registries.create(args);

        assertEquals("{\"v\":\"KERI10JSON0000c5_\",\"t\":\"vcp\",\"d\":\"EMppKX_JxXBuL_xE3A_a6lOcseYwaB7jAvZ0YFdgecXX\",\"i\":\"EMppKX_JxXBuL_xE3A_a6lOcseYwaB7jAvZ0YFdgecXX\",\"ii\":\"hab prefix\",\"s\":\"0\",\"c\":[\"NB\"],\"bt\":\"0\",\"b\":[],\"n\":\"\"}", actual.getRegser().getRaw());
        assertEquals("{\"v\":\"KERI10JSON0000f4_\",\"t\":\"ixn\",\"d\":\"EE5R61289Xnpxc2M-euPtsAkp849tUdNJ7DuyBeSiRtm\",\"i\":\"hab prefix\",\"s\":\"1\",\"p\":\"a digest\",\"a\":[{\"i\":\"EMppKX_JxXBuL_xE3A_a6lOcseYwaB7jAvZ0YFdgecXX\",\"s\":\"0\",\"d\":\"EMppKX_JxXBuL_xE3A_a6lOcseYwaB7jAvZ0YFdgecXX\"}]}", actual.getSerder().getRaw());
    }

    @Test
    @DisplayName("should fail on establishment only for now")
    void shouldFailOnEstablishmentOnly() throws Exception {
        States.HabState hab = States.HabState.builder()
                .prefix("hab prefix")
                .state(States.State.builder()
                        .s("0")
                        .d("a digest")
                        .c(Collections.singletonList("EO"))
                        .build())
                .name("a name")
                .transferable(true)
                .windexes(Collections.emptyList())
                .build();

        when(mockedIdentifiers.get("a name")).thenReturn(hab);
        when(mockedClient.getIdentifier()).thenReturn(mockedIdentifiers);


        assertThrows(Exception.class, () -> {
            CreateRegistryArgs args = CreateRegistryArgs.builder()
                    .name("a name")
                    .registryName("a registry name")
                    .nonce("")
                    .build();

            registries.create(args);
        }, "Establishment only not implemented");
    }
}