package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.ExnMessages.MultisigIcpExchange;
import org.cardanofoundation.signify.app.ExnMessages.IpexGrantExchange;
import org.cardanofoundation.signify.app.ExnMessages.TypedExchange;
import org.cardanofoundation.signify.exception.MalformedExnException;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.app.ExnMessages.IPEX_GRANT_ROUTE;
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
        assertEquals(List.of(), icp.a().rmids());
        // known fields are lifted; the remainder stays available
        assertEquals(Map.of("extra", "kept"), icp.a().additional());
        assertEquals("icp", icp.e().icp().value().getT());
        assertEquals("icp", icp.e().icp().sad().get("t"));
    }

    @Test
    @DisplayName("embeds pair a typed view with the exact wire sad")
    void embedsPairTypedViewWithExactSad() {
        LinkedHashMap<String, Object> icpSad = new LinkedHashMap<>();
        icpSad.put("v", "KERI10JSON0000fd_");
        icpSad.put("t", "icp");
        icpSad.put("d", "EIcpSaid");
        icpSad.put("unknown", "kept");

        MultisigIcpExchange parsed = ExnMessages
            .as(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", icpSad)), MultisigIcpExchange.class)
            .orElseThrow();

        assertEquals("icp", parsed.e().icp().value().getT());
        assertEquals("EIcpSaid", parsed.e().icp().value().getD());
        // the sad keeps wire order and unknown fields, so re-signing stays byte-exact
        assertEquals(List.of("v", "t", "d", "unknown"), List.copyOf(parsed.e().icp().sad().keySet()));
    }

    @Test
    @DisplayName("an embed that fails typed parsing is reported as malformed")
    void malformedEmbedFails() {
        ExchangeResource badIcp = exchange(MULTISIG_ICP_ROUTE, icpAttributes(),
            Map.of("icp", Map.of("k", Map.of("not", "a list"))));

        MalformedExnException exception = assertThrows(MalformedExnException.class,
            () -> ExnMessages.asTyped(badIcp));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
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
        // widening to the sealed union always matches
        assertInstanceOf(MultisigIcpExchange.class, ExnMessages.as(msg, TypedExchange.class).orElseThrow());
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

        MalformedExnException exception = assertThrows(MalformedExnException.class,
            () -> ExnMessages.asTyped(missingGid));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
        assertTrue(exception.getMessage().contains("EMessageSaid"));
        assertTrue(exception.getMessage().contains("gid"));
        assertEquals(MULTISIG_ICP_ROUTE, exception.getRoute());
        assertEquals("EMessageSaid", exception.getSaid());
    }

    @Test
    @DisplayName("as() is empty, not an error, for a malformed message of a different route")
    void asDoesNotParseOtherRoutes() {
        ExchangeResource malformedIcp = exchange(MULTISIG_ICP_ROUTE, Map.of(), Map.of());

        assertTrue(ExnMessages.as(malformedIcp, IpexGrantExchange.class).isEmpty());
        assertThrows(MalformedExnException.class,
            () -> ExnMessages.as(malformedIcp, MultisigIcpExchange.class));
    }

    @Test
    @DisplayName("member lists reject non-string elements and distinguish empty from missing")
    void memberListsAreStrict() {
        MalformedExnException nonString = assertThrows(MalformedExnException.class, () -> ExnMessages.asTyped(
            exchange(MULTISIG_ICP_ROUTE, Map.of("gid", "EGroupId", "smids", List.of(Map.of("not", "an aid"))), Map.of())));
        assertTrue(nonString.getMessage().contains("non-string"));
        assertTrue(nonString.getMessage().contains("smids"));

        MalformedExnException notAList = assertThrows(MalformedExnException.class, () -> ExnMessages.asTyped(
            exchange(MULTISIG_ICP_ROUTE, Map.of("gid", "EGroupId", "smids", "EMember1"), Map.of())));
        assertTrue(notAList.getMessage().contains("not a list"));

        MalformedExnException empty = assertThrows(MalformedExnException.class, () -> ExnMessages.asTyped(
            exchange(MULTISIG_ICP_ROUTE, Map.of("gid", "EGroupId", "smids", List.of()), Map.of())));
        assertTrue(empty.getMessage().contains("empty"));
        assertTrue(empty.getMessage().contains("smids"));

        MalformedExnException rmids = assertThrows(MalformedExnException.class, () -> ExnMessages.asTyped(
            exchange(MULTISIG_ICP_ROUTE,
                Map.of("gid", "EGroupId", "smids", List.of("EMember1"), "rmids", List.of(42)), Map.of())));
        assertTrue(rmids.getMessage().contains("non-string"));
        assertTrue(rmids.getMessage().contains("rmids"));
    }

    @Test
    @DisplayName("a missing required embed fails loudly rather than parsing to a null payload")
    void missingRequiredEmbedFails() {
        ExchangeResource missingIcp = exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of());

        MalformedExnException exception = assertThrows(MalformedExnException.class,
            () -> ExnMessages.asTyped(missingIcp));
        assertTrue(exception.getMessage().contains(MULTISIG_ICP_ROUTE));
        assertTrue(exception.getMessage().contains("icp"));
    }

    @Test
    @DisplayName("unknown extra embeds do not break parsing and stay reachable via the raw exn (forwards compat)")
    void grantEmbedsIgnoreUnknownExtras() {
        Map<String, Object> embeds = Map.of(
            "acdc", Map.of("d", "EAcdcSaid"),
            "iss", Map.of("t", "iss"),
            "anc", Map.of("t", "ixn"),
            "future", Map.of("t", "new"));

        IpexGrantExchange grant = ExnMessages
            .as(exchange(IPEX_GRANT_ROUTE, Map.of("i", "ERecipient"), embeds), IpexGrantExchange.class)
            .orElseThrow();

        assertEquals("EAcdcSaid", grant.e().acdc().value().getD());
        assertEquals("iss", grant.e().iss().value().getT());
        // an embed this version doesn't model stays reachable through the raw exn body
        assertEquals(Map.of("t", "new"), grant.exn().getE().get("future"));
    }

    @Test
    @DisplayName("parsed views are read-only")
    void parsedViewsAreReadOnly() {
        MultisigIcpExchange icp = ExnMessages
            .as(exchange(MULTISIG_ICP_ROUTE, icpAttributes(), Map.of("icp", new LinkedHashMap<>(Map.of("t", "icp")))),
                MultisigIcpExchange.class)
            .orElseThrow();

        assertThrows(UnsupportedOperationException.class, () -> icp.e().icp().sad().put("t", "rot"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().additional().put("x", "y"));
        assertThrows(UnsupportedOperationException.class, () -> icp.a().smids().add("EIntruder"));
    }
}
