package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Pather is a subclass of Bexter that provides SAD Path language specific functionality
    for variable length strings that only contain Base64 URL safe characters.  Pather allows
    the specification of SAD Paths as a list of field components which will be converted to the
    Base64 URL safe character representation.

    Additionally, Pather provides .rawify for extracting and serializing the content targeted by
    .path for a SAD, represented as an instance of Serder.  Pather enforces Base64 URL character
    safety by leveraging the fact that SADs must have static field ordering.  Any field label can
    be replaced by its field ordinal to allow for path specification and traversal for any field
    labels that contain non-Base64 URL safe characters.


    Examples: strings:
        path = []
        text = "-"
        qb64 = '6AABAAA-'

        path = ["A"]
        text = "-A"
        qb64 = '5AABAA-A'

        path = ["A", "B"]
        text = "-A-B"
        qb64 = '4AAB-A-B'

        path = ["A", 1, "B", 3]
        text = "-A-1-B-3"
        qb64 = '4AAC-A-1-B-3'

 */
public class Pather extends Bexter {

    public Pather(RawArgs args, String bext, String[] path) {
        super(args, getBext(args, bext, path));
    }

    public Pather(RawArgs args) {
        this(args, null, null);
    }

    private static String getBext(RawArgs args, String bext, String[] path) {
        if (args.getCode() == null) {
            args.setCode(Codex.MatterCodex.StrB64_L0.getValue());
        }
        if (args.getRaw() == null) {
            if (path == null) {
                throw new EmptyMaterialException("Missing path string.");
            }
            bext = bextify(path);
        }

        return bext;
    }

    private static String bextify(String[] path) {
        List<String> vath = new ArrayList<>();

        for (Object p : path) {
            String sp;
            if (p instanceof Number) {
                sp = p.toString();
            } else {
                sp = (String) p;
            }

            Pattern Reb64 = Pattern.compile("^[A-Za-z0-9_-]*$");
            Matcher matcher = Reb64.matcher(sp);
            if (!matcher.matches()) {
                throw new InvalidValueException("Non Base64 path component = " + p);
            }

            vath.add(sp);
        }

        return "-" + String.join("-", vath);
    }

    public String[] getPath() {
        if (!this.getBext().startsWith("-")) {
            throw new InvalidValueException("Invalid SAD ptr");
        }

        String path = this.getBext();
        while (!path.isEmpty() && path.charAt(0) == '-') {
            path = path.substring(1);
        }

        String[] apath = path.split("-");
        if (!apath[0].isEmpty()) {
            return apath;
        } else {
            return new String[0];
        }
    }
}
