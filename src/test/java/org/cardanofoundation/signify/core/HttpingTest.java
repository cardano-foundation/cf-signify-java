package org.cardanofoundation.signify.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.cardanofoundation.signify.cesr.Cigar;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class HttpingTest {

    @Test
    @DisplayName("create valid Signature-Input header with signature")
    void testSiginput() throws SodiumException {
        final String salt = "0123456789abcdef";
        final Salter salter = new Salter(RawArgs.builder().raw(salt.getBytes()).build());
        final Signer signer = salter.signer();

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("content-length", "256");
        headers.put("connection", "close");
        headers.put("signify-resource", "EWJkQCFvKuyxZi582yJPb0wcwuW3VXmFNuvbQuBpgmIs");
        headers.put("signify-timestamp", "2022-09-24T00:05:48.196795+00:00");

        final Httping.SiginputArgs args = new Httping.SiginputArgs();
        args.setName("sig0");
        args.setMethod("POST");
        args.setPath("/signify");
        args.setHeaders(headers);
        args.setFields(Arrays.asList("signify-resource", "@method", "@path", "signify-timestamp"));
        args.setAlg("ed25519");
        args.setKeyid(signer.getVerfer().getQb64());

        final Httping.SiginputResult result;
        try(MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(Utils::currentTimeSeconds).thenReturn(1663968348L);
            result = Httping.siginput(signer, args);
        }

        Map<String, String> header = result.headers();
        Cigar cigar = (Cigar) result.sig();

        assertEquals(1, header.size());
        assertTrue(header.containsKey("signature-input"));
        final String sigipt = header.get("signature-input");

        assertEquals(
                "sig0=(\"signify-resource\" \"@method\" \"@path\" \"signify-timestamp\");created=1663968348;keyid=\"DN54yRad_BTqgZYUSi_NthRBQrxSnqQdJXWI5UHcGOQt\";alg=\"ed25519\"",
                sigipt
        );

        assertEquals("0BAf5J7VAFCvT8w5HivXSRpzI13iaMMJjDfV1LKmoimufvn7hvJ6Ws9VeD91KSVVnM04F9gQSKutujK3tf9xeG0I", cigar.getQb64());
    }

    @Test
    @DisplayName("desiginput")
    void testDesiginput() {
        final String siginput = "sig0=(\"signify-resource\" \"@method\" \"@path\" \"signify-timestamp\");created=1609459200;keyid=\"EIaGMMWJFPmtXznY1IIiKDIrg-vIyge6mBl2QV8dDjI3\";alg=\"ed25519\"";
        List<Httping.Inputage> inputages = Httping.desiginput(siginput);
        assertEquals(1, inputages.size());

        List<String> fields = new ArrayList<>() {{
            add("signify-resource");
            add("@method");
            add("@path");
            add("signify-timestamp");
        }};
        Httping.Inputage inputage = inputages.get(0);
        assertEquals(fields, inputage.getFields());


        assertEquals("sig0", inputage.getName());
        assertEquals(1609459200L, inputage.getCreated());
        assertEquals("EIaGMMWJFPmtXznY1IIiKDIrg-vIyge6mBl2QV8dDjI3", inputage.getKeyid());
        assertEquals("ed25519", inputage.getAlg());

        assertNull(inputage.getExpires());
        assertNull(inputage.getNonce());
        assertNull(inputage.getContext());
    }
}
