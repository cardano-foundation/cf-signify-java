package org.cardanofoundation.signify.end;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cardanofoundation.signify.cesr.Cigar;
import org.cardanofoundation.signify.cesr.Siger;

import lombok.Getter;
import lombok.Setter;

import static org.cardanofoundation.signify.core.Httping.HEADER_SIG;

@Getter
@Setter
public class Signage {
    private Object markers;
    private Boolean indexed;
    private String signer;
    private String ordinal;
    private String digest;
    private String kind;

    public Signage(Object markers) {
        this.markers = markers;
    }

    public Signage(Object markers, boolean indexed) {
        this.markers = markers;
        this.indexed = indexed;
    }

    public Signage(Object markers, boolean indexed, String signer, String ordinal, String digest, String kind) {
        this.markers = markers;
        this.indexed = indexed;
        this.signer = signer;
        this.ordinal = ordinal;
        this.digest = digest;
        this.kind = kind;
    }

    public static final Set<Object> FALSY = Set.of(false, 0, "?0", "no", "false", "False", "off");
    public static final Set<Object> TRUTHY = Set.of(true, 1, "?1", "yes", "true", "True", "on");

    public static Map<String, String> signature(List<Signage> signages) {
        List<String> values = new ArrayList<>();

        for(Signage signage : signages) {
            List<Object> markers;
            List<String> tags;

            if(signage.getMarkers() instanceof Map) {
                markers = new ArrayList<>(((Map<String, Object>) signage.getMarkers()).values());
                tags = new ArrayList<>(((Map<String, Object>) signage.getMarkers()).keySet());
            } else {
                markers = (List<Object>) signage.getMarkers();
                tags = new ArrayList<>();
            }

            List<String> items = new ArrayList<>();
            boolean indexed = signage.getIndexed() != null ? signage.getIndexed() : markers.get(0) instanceof Siger;

            if(indexed) {
                items.add("indexed=\"?1\"");
            } else {
                items.add("indexed=\"?0\"");
            }

            if(signage.getSigner() != null) {
                items.add("signer=\"" + signage.getSigner() + "\"");
            }
            if(signage.getOrdinal() != null) {
                items.add("ordinal=\"" + signage.getOrdinal() + "\"");
            }
            if(signage.getDigest() != null) {
                items.add("digest=\"" + signage.getDigest() + "\"");
            }
            if(signage.getKind() != null) {
                items.add("kind=\"" + signage.getKind() + "\"");
            }

            for (int idx = 0; idx < markers.size(); idx++) {
                Object marker = markers.get(idx);
                String tag = null;
                String val;

                if (tags.size() > idx) {
                    tag = tags.get(idx);
                }

                if (marker instanceof Siger) {
                    if (!indexed) {
                        throw new IllegalArgumentException(
                            "Indexed signature marker " + marker + " when indexed False."
                        );
                    }

                    tag = (tag != null) ? tag : String.valueOf(((Siger) marker).getIndex());
                    val = ((Siger) marker).getQb64();
                } else if (marker instanceof Cigar) {
                    if (indexed) {
                        throw new IllegalArgumentException(
                            "Unindexed signature marker " + marker + " when indexed True."
                        );
                    }
                    if (((Cigar) marker).getVerfer() == null) {
                        throw new IllegalArgumentException(
                            "Indexed signature marker is missing verfer"
                        );
                    }

                    tag = (tag != null) ? tag : ((Cigar) marker).getVerfer().getQb64();
                    val = ((Cigar) marker).getQb64();
                } else {
                    tag = (tag != null) ? tag : String.valueOf(idx);
                    val = marker.toString();
                }

                items.add(tag + "=\"" + val + "\"");
            }

            values.add(String.join(";", items));
        }

        return Map.of(HEADER_SIG, String.join(",", values));
    }

    public static List<Signage> designature(String value) {
        String[] values = value.replace(" ", "").split(",");

        List<Signage> signages = new ArrayList<>();

        for (String val : values) {
            Map<String, String> dict = new HashMap<>();
            String[] pairs = val.split(";");

            for (String v : pairs) {
                String[] splits = v.split("=", 2);
                dict.put(splits[0], splits[1].replace("\"", ""));
            }

            if (!dict.containsKey("indexed")) {
                throw new IllegalArgumentException(
                    "Missing indexed field in Signature header signage."
                );
            }
            Object item = dict.get("indexed");
            boolean indexed = !FALSY.contains(item);
            dict.remove("indexed");

            String signer = dict.remove("signer");
            String ordinal = dict.remove("ordinal");
            String digest = dict.remove("digest");
            String kind = dict.getOrDefault("kind", "CESR");
            dict.remove("kind");

            if ("CESR".equals(kind)) {
                Map<String, Object> markers = new HashMap<>();

                for (Map.Entry<String, String> entry : dict.entrySet()) {
                    if (indexed) {
                        markers.put(entry.getKey(), new Siger(entry.getValue()));
                    } else {
                        markers.put(entry.getKey(), new Cigar(entry.getValue()));
                    }
                }

                signages.add(new Signage(markers, indexed, signer, ordinal, digest, kind));
            } else {
                signages.add(new Signage(dict, indexed, signer, ordinal, digest, kind));
            }
        }

        return signages;
    }
}
