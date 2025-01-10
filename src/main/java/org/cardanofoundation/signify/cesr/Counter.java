package org.cardanofoundation.signify.cesr;

import org.cardanofoundation.signify.cesr.Matter.Sizage;

import lombok.Getter;
import org.cardanofoundation.signify.cesr.args.CounterArgs;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ShortageException;
import org.cardanofoundation.signify.cesr.exceptions.extraction.UnexpectedCodeException;
import org.cardanofoundation.signify.cesr.exceptions.material.EmptyMaterialException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidCodeSizeException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Counter {

    static Map<String, Sizage> sizes = new HashMap<>();
    static {
        sizes.put("-A", new Sizage(2, 2, 4, 0));
        sizes.put("-B", new Sizage(2, 2, 4, 0));
        sizes.put("-C", new Sizage(2, 2, 4, 0));
        sizes.put("-D", new Sizage(2, 2, 4, 0));
        sizes.put("-E", new Sizage(2, 2, 4, 0));
        sizes.put("-F", new Sizage(2, 2, 4, 0));
        sizes.put("-G", new Sizage(2, 2, 4, 0));
        sizes.put("-H", new Sizage(2, 2, 4, 0));
        sizes.put("-I", new Sizage(2, 2, 4, 0));
        sizes.put("-J", new Sizage(2, 2, 4, 0));
        sizes.put("-K", new Sizage(2, 2, 4, 0));
        sizes.put("-L", new Sizage(2, 2, 4, 0));
        sizes.put("-V", new Sizage(2, 2, 4, 0));
        sizes.put("-0V", new Sizage(3, 5, 8, 0));
        sizes.put("--AAA", new Sizage(5, 3, 8, 0));
    }

    static Map<String, Integer> hards = new HashMap<>();
    static {
        hards.put("-A", 2);
        hards.put("-B", 2);
        hards.put("-C", 2);
        hards.put("-D", 2);
        hards.put("-E", 2);
        hards.put("-F", 2);
        hards.put("-G", 2);
        hards.put("-H", 2);
        hards.put("-I", 2);
        hards.put("-J", 2);
        hards.put("-K", 2);
        hards.put("-L", 2);
        hards.put("-M", 2);
        hards.put("-N", 2);
        hards.put("-O", 2);
        hards.put("-P", 2);
        hards.put("-Q", 2);
        hards.put("-R", 2);
        hards.put("-S", 2);
        hards.put("-T", 2);
        hards.put("-U", 2);
        hards.put("-V", 2);
        hards.put("-W", 2);
        hards.put("-X", 2);
        hards.put("-Y", 2);
        hards.put("-Z", 2);
        hards.put("-a", 2);
        hards.put("-b", 2);
        hards.put("-c", 2);
        hards.put("-d", 2);
        hards.put("-e", 2);
        hards.put("-f", 2);
        hards.put("-g", 2);
        hards.put("-h", 2);
        hards.put("-i", 2);
        hards.put("-j", 2);
        hards.put("-k", 2);
        hards.put("-l", 2);
        hards.put("-m", 2);
        hards.put("-n", 2);
        hards.put("-o", 2);
        hards.put("-p", 2);
        hards.put("-q", 2);
        hards.put("-r", 2);
        hards.put("-s", 2);
        hards.put("-t", 2);
        hards.put("-u", 2);
        hards.put("-v", 2);
        hards.put("-w", 2);
        hards.put("-x", 2);
        hards.put("-y", 2);
        hards.put("-z", 2);
        hards.put("-0", 3);
        hards.put("--", 5);
    }

    private String code = "";
    private int count = -1;

    public Counter(CounterArgs args) {
        if (args.getCode() != null) {
            if (!sizes.containsKey(args.getCode())) {
                throw new UnexpectedCodeException("Unsupported code = " + args.getCode());
            }

            Sizage sizage = sizes.get(args.getCode());
            int cs = sizage.hs + sizage.ss;
            if (sizage.fs != cs || cs % 4 != 0) {
                throw new InvalidCodeSizeException("Whole code size not full size or not multiple of 4. cs=" + cs + " fs=" + sizage.fs);
            }

            int count = args.getCount() != null ? args.getCount() :
                (args.getCountB64() != null ? CoreUtil.b64ToInt(args.getCountB64()) : 1);

            if (count < 0 || count > Math.pow(64, sizage.ss) - 1) {
                throw new InvalidValueException("Invalid count=" + count + " for code=" + args.getCode());
            }

            this.code = args.getCode();
            this.count = count;
        } else if (args.getQb64b() != null) {
            String qb64 = new String(args.getQb64b());
            this._exfil(qb64);
        } else if (args.getQb64() != null) {
            this._exfil(args.getQb64());
        } else if (args.getQb2() != null) {
            throw new UnsupportedOperationException("qb2 not supported yet");
        } else {
            throw new EmptyMaterialException(
                "Improper initialization need either (code and count) or qb64b or qb64 or qb2."
            );
        }
    }

    public String getQb64() {
        return this._infil();
    }

    public byte[] getQb64b() {
        return getQb64().getBytes();
    }

    public String countToB64(Integer l) {
        if (l == null) {
            Sizage sizage = sizes.get(this.code);
            l = sizage.ss;
        }
        return CoreUtil.intToB64(this.count, l);
    }

    public String countToB64() {
        return countToB64(null);
    }

    public static String semVerToB64(String version, int major, int minor, int patch) {
        List<Integer> parts = new ArrayList<>(List.of(major, minor, patch));

        if (!version.isEmpty()) {
            String[] ssplits = version.split("\\.");
            List<Integer> splits = new ArrayList<>();

            for (String x : ssplits) {
                splits.add(x.isEmpty() ? 0 : Integer.parseInt(x));
            }

            int off = splits.size();
            int x = 3 - off;
            for (int i = 0; i < x; i++) {
                splits.add(parts.get(i + off));
            }
            parts = splits;
        }

        for (int p : parts) {
            if (p < 0 || p > 63) {
                throw new InvalidValueException(
                    "Out of bounds semantic version. Part=" + p + " is < 0 or > 63."
                );
            }
        }

        return parts.stream()
            .map(p -> CoreUtil.intToB64(p, 1))
            .collect(Collectors.joining());
    }

    public static String semVerToB64() {
        return semVerToB64("", 0, 0, 0);
    }

    public static String semVerToB64(String version) {
        return semVerToB64(version, 0, 0, 0);
    }

    public static String semVerToB64(String version, int major) {
        return semVerToB64(version, major, 0, 0);
    }

    public static String semVerToB64(String version, int major, int minor) {
        return semVerToB64(version, major, minor, 0);
    }

    private void _exfil(String qb64) {
        if (qb64.isEmpty()) {
            throw new EmptyMaterialException("Empty Material");
        }

        String first = qb64.substring(0, 2);
        if (!hards.containsKey(first)) {
            throw new UnexpectedCodeException("Unexpected code start=" + first);
        }

        int hs = hards.get(first);
        if (qb64.length() < hs) {
            throw new ShortageException("Need " + (hs - qb64.length()) + " more characters.");
        }

        String hard = qb64.substring(0, hs);
        if (!sizes.containsKey(hard)) {
            throw new UnexpectedCodeException("Unsupported code=" + hard);
        }

        Sizage sizage = sizes.get(hard);
        int cs = sizage.hs + sizage.ss;

        if (qb64.length() < cs) {
            throw new ShortageException("Need " + (cs - qb64.length()) + " more chars.");
        }

        String scount = qb64.substring(sizage.hs, sizage.hs + sizage.ss);
        int count = CoreUtil.b64ToInt(scount);

        this.code = hard;
        this.count = count;
    }

    private String _infil() {
        Sizage sizage = sizes.get(this.code);
        int cs = sizage.hs + sizage.ss;

        if (sizage.fs != cs || cs % 4 != 0) {
            throw new InvalidCodeSizeException("Whole code size not full size or not multiple of 4. cs=" + cs + " fs=" + sizage.fs);
        }

        if (this.count < 0 || this.count > Math.pow(64, sizage.ss) - 1) {
            throw new InvalidValueException("Invalid count=" + this.count + " for code=" + this.code);
        }

        return this.code + CoreUtil.intToB64(this.count, sizage.ss);
    }

}
