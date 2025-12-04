package org.cardanofoundation.signify.app.controlller;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.clienting.State;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.args.RotateArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.exceptions.validation.ValidationException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;

import java.math.BigInteger;
import java.security.DigestException;
import java.util.*;
import java.util.stream.Collectors;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.Tier;

/**
 * Controller is responsible for managing signing keys for the client and agent.  The client
 * signing key represents the Account for the client on the agent
 */
@Getter
@Setter
public class Controller {
    private String bran;
    public String stem;
    public Tier tier;
    public int ridx;
    public Salter salter;
    public Signer signer;
    private Signer nsigner;
    public Serder serder;
    private List<String> keys;
    public List<String> ndigs;

    public Controller(String bran, Tier tier) throws DigestException, LibsodiumException {
        this(bran, tier, 0, null);
    }

    public Controller(String bran, Tier tier, Integer ridx) throws DigestException, LibsodiumException {
        this(bran, tier, ridx, null);
    }

    public Controller(String bran, Tier tier, Integer ridx, Object state) throws DigestException, LibsodiumException {
        this.bran = MatterCodex.Salt_128.getValue() + "A" + bran.substring(0, 21); // qb64 salt for seed
        this.stem = "signify:controller";
        this.tier = tier;
        this.ridx = ridx;

        this.salter = new Salter(this.bran, this.tier);

        Manager.SaltyCreator creator = new Manager.SaltyCreator(
            this.salter.getQb64(),
            this.tier,
            this.stem
        );

        this.signer = new LinkedList<>(creator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx,
                0,
                false
            ).getSigners()).pop();

        this.nsigner = new LinkedList<>(creator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx + 1,
                0,
                false
            ).getSigners()).pop();

        this.keys = List.of(this.signer.getVerfer().getQb64());

        RawArgs rawArgs = RawArgs.builder()
            .code(MatterCodex.Blake3_256.getValue())
            .build();
        this.ndigs = List.of(
            new Diger(rawArgs, this.nsigner.getVerfer().getQb64b())
                .getQb64()
        );

        Map<String, Object> stateMap = Utils.toMap(state);
        Map<String, Object> ee = Utils.toMap(stateMap.get("ee"));
        if (stateMap.isEmpty() || ee.get("s").equals("0")) {
            InceptArgs args = InceptArgs.builder()
                .keys(this.keys)
                .isith("1")
                .nsith("1")
                .ndigs(this.ndigs)
                .code(MatterCodex.Blake3_256.getValue())
                .toad(0)
                .wits(new ArrayList<>())
                .build();
            this.serder = Eventing.incept(args);
        } else {
            this.serder = new Serder(ee);
        }
    }

    public Object approveDelegation(Agent agent) throws DigestException, LibsodiumException {
        Seqner seqner = new Seqner(agent.getSn());

        Map<String, String> anchor = new LinkedHashMap<>();
        anchor.put("i", agent.getPre());
        anchor.put("s", seqner.getSnh());
        anchor.put("d", agent.getSaid());

        String currentSn = (String) serder.getKed().get("s");
        BigInteger nextSn = new CesrNumber(new RawArgs(), null, currentSn).getNum().add(BigInteger.ONE);

        serder = Eventing.interact(
            InteractArgs.builder()
                .pre(serder.getPre())
                .dig((String) serder.getKed().get("d"))
                .sn(nextSn)
                .data(Collections.singletonList(anchor))
                .version(new CoreUtil.Version())
                .kind(CoreUtil.Serials.JSON)
                .build()
        );

        Siger sig = (Siger) signer.sign(serder.getRaw().getBytes(), 0);
        return new String[] { sig.getQb64() };
    }

    public String getPre() {
        return this.serder.getPre();
    }

    public EventResult getEvent() throws Exception {
        Siger siger = (Siger) this.signer.sign(
            this.serder.getRaw().getBytes(),
            0);
        return new EventResult(this.serder, siger);
    }

    public Verfer[] getVerfers() {
        return new Verfer[]{this.signer.getVerfer()};
    }

    public Serder derive(Object state) throws DigestException {
        if (state != null && ((KeyStateRecord) state).getEe().getS().equals("0")) {
            return Eventing.incept(InceptArgs.builder()
                .keys(this.keys)
                .isith("1")
                .nsith("1")
                .ndigs(this.ndigs)
                .code(MatterCodex.Blake3_256.getValue())
                .toad(0)
                .wits(List.of())
                .build()
            );
        } else {
            return new Serder(Utils.toMap(Utils.toMap(((State) state).getController()).get("ee")));
        }
    }

    public Map<String, Object> rotate(String bran, List<Map<String, Object>> aids) throws DigestException, LibsodiumException {
        String nbran = MatterCodex.Salt_128.getValue() + "A" + bran.substring(0, 21); // qb64 salt for seed
        Salter nsalter = new Salter(nbran, this.tier);
        Signer nsigner = this.salter.signer(null, false);

        Manager.SaltyCreator creator = new Manager.SaltyCreator(
            this.salter.getQb64(),
            this.tier,
            this.stem
        );
        Signer signer = new LinkedList<>(creator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx + 1,
                0,
                false)
            .getSigners()).pop();

        Manager.SaltyCreator ncreator = new Manager.SaltyCreator(nsalter.getQb64(), this.tier, this.stem);
        this.signer = new LinkedList<>(ncreator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx,
                0,
                false)
            .getSigners()).pop();
        this.nsigner = new LinkedList<>(ncreator
            .create(
                null,
                1,
                MatterCodex.Ed25519_Seed.getValue(),
                true,
                0,
                this.ridx + 1,
                0,
                false)
            .getSigners()).pop();

        this.keys = Arrays.asList(this.signer.getVerfer().getQb64(), signer.getVerfer().getQb64());
        this.ndigs = Collections.singletonList(new Diger(RawArgs.builder().build(), this.nsigner.getVerfer().getQb64b()).getQb64());

        Serder rot = Eventing.rotate(RotateArgs.builder()
                .pre(this.getPre())
                .keys(this.keys)
                .dig((String) this.serder.getKed().get("d"))
                .isith(Arrays.asList("1", "0"))
                .nsith("1")
                .ndigs(this.ndigs)
            .build());

        List<String> sigs = Arrays.asList(
            ((Siger) signer.sign(rot.getRaw().getBytes(), 1, false, 0)).getQb64(),
            ((Siger) this.signer.sign(rot.getRaw().getBytes(), 0)).getQb64()
        );

        Encrypter encrypter = new Encrypter(RawArgs.builder().build(), nsigner.getVerfer().getQb64().getBytes());
        Decrypter decrypter = new Decrypter(RawArgs.builder().build(), nsigner.getQb64b());
        String sxlt = encrypter.encrypt(bran.getBytes()).getQb64();

        Map<String, Object> keys = new LinkedHashMap<>();
        for (Map<String, Object> aid : aids) {
            String pre = (String) aid.get("prefix");

            if (aid.containsKey("salty")) {
                Map<String, Object> salty = Utils.toMap(aid.get("salty"));
                Cipher cipher = new Cipher(salty.get("sxlt").toString());
                String dnxt = ((Salter) decrypter.decrypt(null, cipher)).getQb64();

                // Now we have the AID salt, use it to verify against the current public keys
                Manager.SaltyCreator acreator = new Manager.SaltyCreator(
                    dnxt,
                    (Tier) salty.get("tier"),
                    salty.get("stem").toString()
                );

                List<Signer> signers = acreator.create(
                    Utils.toList(salty.get("icodes")),
                    null,
                    MatterCodex.Ed25519_Seed.getValue(),
                    (Boolean) salty.get("transferable"),
                    (Integer) salty.get("pidx"),
                    0,
                    (Integer) salty.get("kidx"),
                    false
                ).getSigners();

                List<String> _signers = signers.stream()
                    .map(s -> s.getVerfer().getQb64())
                    .collect(Collectors.toList());

                List<String> pubs = Utils.toList(Utils.toMap(aid.get("state")).get("k"));

                if (!String.join(",", pubs).equals(String.join(",", _signers))) {
                    throw new InvalidValueException("Invalid Salty AID");
                }

                String asxlt = encrypter.encrypt(dnxt.getBytes()).getQb64();
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("sxlt", asxlt);
                keys.put(pre, keyData);

            } else if (aid.containsKey("randy")) {
                Map<String, Object> randy = Utils.toMap(aid.get("randy"));
                List<String> prxs = Utils.toList(randy.get("prxs"));
                List<String> nxts = Utils.toList(randy.get("nxts"));

                List<String> nprxs = new ArrayList<>();
                List<Signer> signers = new ArrayList<>();

                for (String prx : prxs) {
                    Cipher cipher = new Cipher(prx);
                    Signer dsigner = (Signer) decrypter.decrypt(null, cipher, true);
                    signers.add(dsigner);
                    nprxs.add(encrypter.encrypt(dsigner.getQb64().getBytes()).getQb64());
                }

                List<String> pubs = Utils.toList(Utils.toMap(aid.get("state")).get("k"));
                List<String> _signers = signers.stream()
                    .map(s -> s.getVerfer().getQb64())
                    .collect(Collectors.toList());

                if (!String.join(",", pubs).equals(String.join(",", _signers))) {
                    throw new ValidationException("Unable to rotate, validation of encrypted public keys " + pubs + " failed");
                }

                List<String> nnxts = new ArrayList<>();
                for (String nxt : nxts) {
                    nnxts.add(this.recrypt(nxt, decrypter, encrypter));
                }

                Map<String, Object> keyData = new LinkedHashMap<>();
                keyData.put("prxs", nprxs);
                keyData.put("nxts", nnxts);
                keys.put(pre, keyData);

            } else {
                throw new InvalidValueException("Invalid aid type");
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rot", rot.getKed());
        data.put("sigs", sigs);
        data.put("sxlt", sxlt);
        data.put("keys", keys);

        return data;
    }

    public String recrypt(String enc, Decrypter decrypter, Encrypter encrypter) throws LibsodiumException {
        Cipher cipher = new Cipher(enc);
        String dnxt = ((Salter) decrypter.decrypt(null, cipher)).getQb64();
        return encrypter.encrypt(dnxt.getBytes()).getQb64();
    }

    public record EventResult(Serder evt, Siger sign) {}

}
