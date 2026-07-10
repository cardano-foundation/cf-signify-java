package org.cardanofoundation.signify.cesr.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.config.GeneratedModelConfig;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.CounterArgs;
import org.cardanofoundation.signify.cesr.exception.InvalidSizeException;
import org.cardanofoundation.signify.cesr.exception.InvalidValueException;
import org.cardanofoundation.signify.exception.SignifySerializationException;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Utils {
    public static final int CRYPTO_BOX_SEAL_BYTES = 48;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        GeneratedModelConfig.configure(objectMapper);
    }

    public static byte[] intToBytes(BigInteger value, int size) {
        if (value.signum() < 0) {
            throw new InvalidValueException("Value must be non-negative");
        }

        byte[] result = new byte[size];
        byte[] valueBytes = value.toByteArray();

        // Remove leading zero byte if present (BigInteger's sign byte)
        if (valueBytes.length > 1 && valueBytes[0] == 0) {
            byte[] tmp = new byte[valueBytes.length - 1];
            System.arraycopy(valueBytes, 1, tmp, 0, tmp.length);
            valueBytes = tmp;
        }

        if (valueBytes.length > size) {
            throw new InvalidSizeException(
                String.format("Value too large: needs %d bytes, but size is limited to %d",
                    valueBytes.length, size)
            );
        }
        // Copy to result array, padding with leading zeros if necessary
        int offset = size - valueBytes.length;
        System.arraycopy(valueBytes, 0, result, offset, valueBytes.length);

        return result;
    }

    public static BigInteger bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return BigInteger.ZERO;
        }
        // Ensure positive number by adding a leading zero byte
        byte[] positiveBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, positiveBytes, 1, bytes.length);
        return new BigInteger(positiveBytes);
    }

    public static String jsonStringify(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new SignifySerializationException("Unable to serialize to JSON: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Map.of();
        }

        try {
            return objectMapper.convertValue(obj, new TypeReference<>() {});
        } catch (Exception e) {
            throw new SignifySerializationException("Unable to create map from object: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new SignifySerializationException(
                    "Error while parsing JSON: " + e.getMessage() + " - payload: " + abbreviate(json), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new SignifySerializationException(
                    "Error while parsing JSON: " + e.getMessage() + " - payload: " + abbreviate(json), e);
        }
    }

    private static String abbreviate(String payload) {
        if (payload == null) {
            return "null";
        }
        return payload.length() <= 1000 ? payload : payload.substring(0, 1000) + "...";
    }

    public static List<String> toList(Object obj) {
        return switch (obj) {
            case String s -> List.of(s);
            case Object[] arr -> Arrays.stream(arr)
                .map(String::valueOf)
                .toList();
            case Collection<?> col -> col.stream()
                .map(String::valueOf)
                .toList();
            case null, default -> Collections.emptyList();
        };
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    // KERI datetimes are fixed-width with exactly 6 fraction digits;
    // Instant.toString() drops the fraction entirely on whole seconds.
    private static final DateTimeFormatter KERI_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSS'+00:00'").withZone(ZoneOffset.UTC);

    public static String currentDateTimeString() {
        return toDateTimeString(Instant.now());
    }

    public static String toDateTimeString(Instant instant) {
        return KERI_DATETIME_FORMATTER.format(instant);
    }


    public static byte[] serializeACDCAttachment(Serder anc) {
        Prefixer prefixer = new Prefixer(anc.getPre());
        Seqner seqner = new Seqner(BigInteger.valueOf(anc.getSn()));
        Saider saider = new Saider(anc.getKed().get("d").toString());

        byte[] craw = new byte[0];
        byte[] ctr = new Counter(CounterArgs.builder()
                .code(Codex.CounterCodex.SealSourceTriples.getValue())
                .count(1).build()).getQb64b();
        byte[] prefix = prefixer.getQb64b();
        byte[] seq = seqner.getQb64b();
        byte[] said = saider.getQb64b();

        byte[] newCraw = new byte[craw.length + ctr.length + prefix.length + seq.length + said.length];

        System.arraycopy(craw, 0, newCraw, 0, craw.length);
        System.arraycopy(ctr, 0, newCraw, craw.length, ctr.length);
        System.arraycopy(prefix, 0, newCraw, craw.length + ctr.length, prefix.length);
        System.arraycopy(seq, 0, newCraw, craw.length + ctr.length + prefix.length, seq.length);
        System.arraycopy(said, 0, newCraw, craw.length + ctr.length + prefix.length + seq.length, said.length);

        return newCraw;
    }

    public static byte[] serializeIssExnAttachment(Serder anc) {
        Seqner seqner = new Seqner(BigInteger.valueOf(anc.getSn()));
        Saider ancSaider = new Saider(anc.getKed().get("d").toString());

        byte[] coupleArray = new byte[seqner.getQb64b().length + ancSaider.getQb64b().length];
        System.arraycopy(seqner.getQb64b(), 0, coupleArray, 0, seqner.getQb64b().length);
        System.arraycopy(ancSaider.getQb64b(), 0, coupleArray, seqner.getQb64b().length, ancSaider.getQb64b().length);

        Counter counter = new Counter(CounterArgs.builder()
                .code(Codex.CounterCodex.SealSourceCouples.getValue())
                .count(1).build());
        byte[] counterQb64b = counter.getQb64b();

        byte[] atc = concatByteArrays(counterQb64b, coupleArray);

        if (atc.length % 4 != 0) {
            throw new InvalidSizeException(
                    String.format("Invalid attachments size: %d, non-integral quadlets detected.", atc.length)
            );
        }

        Counter pcnt = new Counter(CounterArgs.builder()
                .code(Codex.CounterCodex.AttachedMaterialQuadlets.getValue())
                .count(atc.length / 4).build());
        byte[] msg = new byte[pcnt.getQb64b().length + atc.length];
        System.arraycopy(pcnt.getQb64b(), 0, msg, 0, pcnt.getQb64b().length);
        System.arraycopy(atc, 0, msg, pcnt.getQb64b().length, atc.length);

        return msg;
    }

    public static byte[] concatByteArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];

        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);

        return result;
    }
}
