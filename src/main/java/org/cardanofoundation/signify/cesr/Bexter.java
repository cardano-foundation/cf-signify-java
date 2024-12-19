package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.decodeBase64Url;
import static org.cardanofoundation.signify.cesr.util.CoreUtil.encodeBase64Url;

/*
    Bexter is subclass of Matter, cryptographic material, for variable length
    strings that only contain Base64 URL safe characters, i.e. Base64 text (bext).
    When created using the 'bext' parameter, the encoded matter in qb64 format
    in the text domain is more compact than would be the case if the string were
    passed in as raw bytes. The text is used as is to form the value part of the
    qb64 version not including the leader.

    Due to ambiguity that arises from pre-padding bext whose length is a multiple of
    three with one or more 'A' chars. Any bext that starts with an 'A' and whose length
    is either a multiple of 3 or 4 may not round trip. Bext with a leading 'A'
    whose length is a multiple of four may have the leading 'A' stripped when
    round tripping.

        Bexter(bext='ABBB').bext == 'BBB'
        Bexter(bext='BBB').bext == 'BBB'
        Bexter(bext='ABBB').qb64 == '4AABABBB' == Bexter(bext='BBB').qb64

    To avoid this problem, only use for applications of base 64 strings that
    never start with 'A'

    Examples: base64 text strings:

    bext = ""
    qb64 = '4AAA'

    bext = "-"
    qb64 = '6AABAAA-'

    bext = "-A"
    qb64 = '5AABAA-A'

    bext = "-A-"
    qb64 = '4AABA-A-'

    bext = "-A-B"
    qb64 = '4AAB-A-B'


    Example uses:
        CESR encoded paths for nested SADs and SAIDs
        CESR encoded fractionally weighted threshold expressions


    Attributes:

    Inherited Properties:  (See Matter)
        .pad  is int number of pad chars given raw

        .code is  str derivation code to indicate cypher suite
        .raw is bytes crypto material only without code
        .index is int count of attached crypto material by context (receipts)
        .qb64 is str in Base64 fully qualified with derivation code + crypto mat
        .qb64b is bytes in Base64 fully qualified with derivation code + crypto mat
        .qb2  is bytes in binary with derivation code + crypto material
        .transferable is Boolean, True when transferable derivation code False otherwise

    Properties:
        .text is the Base64 text value, .qb64 with text code and leader removed.

    Hidden:
        ._pad is method to compute  .pad property
        ._code is str value for .code property
        ._raw is bytes value for .raw property
        ._index is int value for .index property
        ._infil is method to compute fully qualified Base64 from .raw and .code
        ._exfil is method to extract .code and .raw from fully qualified Base64

    Methods:

 */
public class Bexter extends Matter {

    public Bexter(RawArgs args, String bext) {
        super(getRawArgs(args, bext));

        if (!Codex.BexCodex.has(this.getCode())) {
            throw new InvalidCodeException("Invalid code = " + this.getCode() + "for Bexter.");
        }
    }

    public Bexter(RawArgs args) {
        this(args, null);
    }

    private static RawArgs getRawArgs(RawArgs args, String bext) {
        if (args.getCode() == null) {
            args.setCode(MatterCodex.StrB64_L0.getValue());
        }
        if (args.getRaw() == null) {
            if (bext == null) {
                throw new EmptyMaterialException("Missing bext string.");
            }

            Pattern Reb64 = Pattern.compile("^[A-Za-z0-9_-]*$");  // Base64URL character set pattern
            Matcher matcher = Reb64.matcher(bext);
            if (!matcher.matches()) {
                throw new InvalidValueException("Invalid Base64");
            }

            args.setRaw(rawify(bext));
        }

        return args;
    }

    private static byte[] rawify(String bext) {
        final int ts = bext.length() % 4;    // bext size mod 4
        final int ws = (4 - ts) % 4;         // pre conv wad size in chars
        final int ls = (3 - ts) % 3;         // post conv lead size in bytes

        char[] wadChars = new char[ws];
        Arrays.fill(wadChars, 'A');
        String wad = new String(wadChars);

        final String base = wad + bext;      // pre pad with wad of zeros in Base64 == 'A'
        final byte[] raw = decodeBase64Url(base); // [ls:]  // convert and remove leader

        return Arrays.copyOfRange(raw, ls, raw.length); // raw binary equivalent of text
    }

    public String getBext() {
        Sizage sizage = Matter.sizes.get(this.getCode());
        byte[] wad = new byte[sizage.ls];
        Arrays.fill(wad, (byte) 0);

        byte[] combined = new byte[wad.length + this.getRaw().length];
        System.arraycopy(wad, 0, combined, 0, wad.length);
        System.arraycopy(this.getRaw(), 0, combined, wad.length, this.getRaw().length);

        String bext = encodeBase64Url(combined);

        int ws = 0;
        if (sizage.ls == 0 && !bext.isEmpty()) {
            if (bext.charAt(0) == 'A') {
                ws = 1;
            }
        } else {
            ws = (sizage.ls + 1) % 4;
        }

        return bext.isEmpty() ? bext : bext.substring(ws);
    }
}
