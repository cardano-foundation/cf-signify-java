package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.ExnMessageTypes.MultisigIcpExchange;
import org.cardanofoundation.signify.app.ExnMessageTypes.MultisigIcpGroup;
import org.cardanofoundation.signify.app.ExnMessageTypes.MultisigIssGroup;
import org.cardanofoundation.signify.app.ExnMessageTypes.IpexGrantExchange;
import org.cardanofoundation.signify.app.ExnMessageTypes.TypedExchange;
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

public class ExnMessageTypesTest {

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
        TypedExchange typed = ExnMessageTypes
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
        assertTrue(ExnMessageTypes.asTyped(exchange("/unknown/route", Map.of(), Map.of())).isEmpty());
        assertTrue(ExnMessageTypes.asTyped(exchange(null, Map.of(), Map.of())).isEmpty());
        assertTrue(ExnMessageTypes.asTyped(new ExchangeResource()).isEmpty());
        assertTrue(ExnMessageTypes.asTyped(null).isEmpty());
    }

    @Test
    @DisplayName("as() narrows to the requested type, empty on mismatch")
    void asNarrows() {
        ExchangeResource msg = exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of());

        assertTrue(ExnMessageTypes.as(msg, MultisigIcpExchange.class).isPresent());
        assertTrue(ExnMessageTypes.as(msg, IpexGrantExchange.class).isEmpty());
    }

    @Test
    @DisplayName("asGroup parses group requests with metadata")
    void asGroupParses() {
        MultisigIcpGroup parsed = ExnMessageTypes
            .asGroup(group(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of()), MultisigIcpGroup.class)
            .orElseThrow();

        assertEquals("multisig", parsed.metadata().groupName());
        assertEquals("member1", parsed.metadata().memberName());
        assertEquals("EGroupId", parsed.a().gid());

        assertTrue(ExnMessageTypes
            .asGroup(group(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of()), MultisigIssGroup.class)
            .isEmpty());
    }

    @Test
    @DisplayName("malformed matching-route messages fail loudly with route context")
    void malformedFailsWithContext() {
        ExchangeResource missingGid = exchange(MULTISIG_ICP_ROUTE, Map.of("smids", List.of("EMember1")), Map.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ExnMessageTypes.asTyped(missingGid));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
        assertTrue(exception.getMessage().contains("EMessageSaid"));
        assertTrue(exception.getMessage().contains("gid"));
    }

    @Test
    @DisplayName("parsed views are read-only")
    void parsedViewsAreReadOnly() {
        MultisigIcpExchange icp = ExnMessageTypes
            .as(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", new LinkedHashMap<>(Map.of("t", "icp")))),
                MultisigIcpExchange.class)
            .orElseThrow();

        assertThrows(UnsupportedOperationException.class, () -> icp.e().icp().put("t", "rot"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().additional().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().smids().add("EIntruder"));
    }
}
