package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Grouping {
    @Getter
    public static class Groups {
        public final SignifyClient client;

        /**
         * Groups
         * @param client {SignifyClient}
         */
        public Groups(SignifyClient client) {
            this.client = client;
        }

        /**
         * Get group request messages
         * @param said SAID of exn message to load
         * @return Optional containing the replay messages if found, or empty if not found
         * @throws LibsodiumException if a Sodium error occurs
         * @throws IOException if an I/O error occurs
         * @throws InterruptedException if the operation is interrupted
         */
        public Optional<Object> getRequest(String said) throws LibsodiumException, IOException, InterruptedException {
            String path = "/multisig/request/" + said;
            String method = "GET";
            HttpResponse<String> response = this.client.fetch(path, method, null);
            
            if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            
            return Optional.of(Utils.fromJson(response.body(), Object.class));
        }

        /**
         * Send multisig exn request messages to other group members
         * @param name human-readable name of group AID
         * @param exn  exn message to send to other members
         * @param sigs signature of the participant over the exn
         * @param atc  additional attachments from embedded events in exn
         * @return The list of replay messages
         * @throws Exception if the fetch operation fails
         */
        public Object sendRequest(
            String name,
            Map<String, Object> exn,
            List<String> sigs,
            String atc
        ) throws LibsodiumException, IOException, InterruptedException {
            String path = "/identifiers/" + name + "/multisig/request";
            String method = "POST";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("exn", exn);
            data.put("sigs", sigs);
            data.put("atc", atc);

            HttpResponse<String> response = this.client.fetch(path, method, data);
            return Utils.fromJson(response.body(), Object.class);
        }

        /**
         * Join multisig group using rotation event.
         * This can be used by participants being asked to contribute keys to a rotation event to join an existing group.
         * @param name  human-readable name of group AID
         * @param rot   rotation event
         * @param sigs  signatures
         * @param gid   prefix
         * @param smids array of participants
         * @param rmids array of participants
         * @return The list of replay messages
         * @throws Exception if the fetch operation fails
         */
        public Object join(
            String name,
            Object rot,
            List<String> sigs,
            String gid,
            List<String> smids,
            List<String> rmids
        ) throws LibsodiumException, IOException, InterruptedException {
            String path = "/identifiers/" + name + "/multisig/join";
            String method = "POST";
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("tpc", "multisig");
            data.put("rot", Utils.toMap(rot).get("ked"));
            data.put("sigs", sigs);
            data.put("gid", gid);
            data.put("smids", smids);
            data.put("rmids", rmids);

            HttpResponse<String> response = this.client.fetch(path, method, data);
            return Utils.fromJson(response.body(), Object.class);
        }
    }
}
