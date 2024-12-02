package org.cardanofoundation.signify.e2e.utils;

import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;

import java.util.List;

public class  MultisigUtils {

    @Getter
    @Setter
    public static class AcceptMultisigInceptArgs {
        private String groupName;
        private String localMemberName;
        private String msgSaid;

        public AcceptMultisigInceptArgs(String groupName, String localMemberName, String msgSaid) {
            this.groupName = groupName;
            this.localMemberName = localMemberName;
            this.msgSaid = msgSaid;
        }
    }

    @Getter
    @Setter
    public static class StartMultisigInceptArgs {
        private String groupName;
        private String localMemberName;
        private List<String> participants;
        private Object isith; // Can be Integer, String, or List<String>
        private Object nsith; // Can be Integer, String, or List<String>
        private Integer toad;
        private List<String> wits;
        private String delpre;

        public StartMultisigInceptArgs(String groupName, String localMemberName, List<String> participants) {
            this.groupName = groupName;
            this.localMemberName = localMemberName;
            this.participants = participants;
        }
    }

    public static void acceptMultisigIncept(SignifyClient client2, AcceptMultisigInceptArgs groupName,
                                            AcceptMultisigInceptArgs localMemberName, AcceptMultisigInceptArgs msgSaid) {
        // TO-DO
    }
}
