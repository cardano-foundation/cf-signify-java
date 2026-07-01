package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.ExnMessages.MultisigIcpExchange;
import org.cardanofoundation.signify.app.ExnMessages.IpexGrantExchange;
import org.cardanofoundation.signify.app.ExnMessages.TypedExchange;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.app.ExnMessages.MULTISIG_ICP_ROUTE;
import static org.junit.jupiter.api.Assertions.*;

public class ExnMessagesTest {

    private static ExchangeResource exchange(String route, Map<String, Object> a, Map<String, Object> e) {
        Exn exn = new Exn();
        exn.setR(route);
        exn.setD("EMessageSaid");
        exn.setA(new LinkedHashMap<>(a));
        exn.setE(new LinkedHashMap<>(e));
        ExchangeResource msg = new ExchangeResource();
        msg.setExn(exn);
        msg.setPathed(new LinkedHashMap<>());
        return msg;
    }

    private static ExnMultisig group(String route, Map<String, Object> a, Map<String, Object> e) {
        ExnMultisig msg = new ExnMultisig();
        msg.setExn(exchange(route, a, e).getExn());
        msg.setGroupName("multisig");
        msg.setMemberName("member1");
        return msg;
    }

    private static Map<String, Object> icpAttributes() {
        return Map.of("gid", "EGroupId", "smids", List.of("EMember1"), "extra", "kept");
    }

    @Test
    @DisplayName("asTyped dispatches on the message's own route")
    void asTypedDispatches() {
        TypedExchange typed = ExnMessages
            .asTyped(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp"))))
            .orElseThrow();

        MultisigIcpExchange icp = assertInstanceOf(MultisigIcpExchange.class, typed);
        assertEquals("EGroupId", icp.a().gid());
        assertEquals(List.of("EMember1"), icp.a().smids());
        // known fields are lifted; the remainder stays available
        assertEquals(Map.of("extra", "kept"), icp.a().additional());
        assertEquals("icp", icp.e().icp().get("t"));
    }

    @Test
    @DisplayName("asTyped is empty for unknown or absent routes")
    void asTypedUnknownRoutes() {
        assertTrue(ExnMessages.asTyped(exchange("/unknown/route", Map.of(), Map.of())).isEmpty());
        assertTrue(ExnMessages.asTyped(exchange(null, Map.of(), Map.of())).isEmpty());
        assertTrue(ExnMessages.asTyped(new ExchangeResource()).isEmpty());
        assertTrue(ExnMessages.asTyped((ExchangeResource) null).isEmpty());
    }

    @Test
    @DisplayName("as() narrows to the requested type, empty on mismatch")
    void asNarrows() {
        ExchangeResource msg = exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp")));

        assertTrue(ExnMessages.as(msg, MultisigIcpExchange.class).isPresent());
        assertTrue(ExnMessages.as(msg, IpexGrantExchange.class).isEmpty());
    }

    @Test
    @DisplayName("group request messages parse via the same typed exchanges")
    void groupRequestsParse() {
        MultisigIcpExchange parsed = ExnMessages
            .as(group(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp"))), MultisigIcpExchange.class)
            .orElseThrow();

        assertEquals("EGroupId", parsed.a().gid());

        assertTrue(ExnMessages
            .as(group(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp"))), IpexGrantExchange.class)
            .isEmpty());
        assertTrue(ExnMessages.asTyped((ExnMultisig) null).isEmpty());
    }

    @Test
    @DisplayName("the group envelope is recoverable from a parsed group request")
    void groupEnvelopeRecoverable() {
        MultisigIcpExchange parsed = ExnMessages
            .as(group(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp"))), MultisigIcpExchange.class)
            .orElseThrow();

        ExnMultisig request = parsed.request();
        assertNotNull(request);
        assertEquals("multisig", request.getGroupName());
        assertEquals("member1", request.getMemberName());
        assertEquals("EMessageSaid", parsed.exn().getD());
    }

    @Test
    @DisplayName("a standalone exchange carries no group request")
    void standaloneExchangeHasNoEnvelope() {
        MultisigIcpExchange parsed = ExnMessages
            .as(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", Map.of("t", "icp"))), MultisigIcpExchange.class)
            .orElseThrow();

        assertNull(parsed.request());
    }

    @Test
    @DisplayName("malformed matching-route messages fail loudly with route context")
    void malformedFailsWithContext() {
        ExchangeResource missingGid = exchange(MULTISIG_ICP_ROUTE, Map.of("smids", List.of("EMember1")), Map.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ExnMessages.asTyped(missingGid));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
        assertTrue(exception.getMessage().contains("EMessageSaid"));
        assertTrue(exception.getMessage().contains("gid"));
    }

    @Test
    @DisplayName("a missing required embed fails loudly rather than parsing to a null payload")
    void missingRequiredEmbedFails() {
        ExchangeResource missingIcp = exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ExnMessages.asTyped(missingIcp));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
        assertTrue(exception.getMessage().contains("icp"));
    }

    @Test
    @DisplayName("parsed views are read-only")
    void parsedViewsAreReadOnly() {
        MultisigIcpExchange icp = ExnMessages
            .as(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", new LinkedHashMap<>(Map.of("t", "icp")))),
                MultisigIcpExchange.class)
            .orElseThrow();

        assertThrows(UnsupportedOperationException.class, () -> icp.e().icp().put("t", "rot"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().additional().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().smids().add("EIntruder"));
    }
}
