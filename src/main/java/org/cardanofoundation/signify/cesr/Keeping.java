package org.cardanofoundation.signify.cesr;

import java.util.List;

public class Keeping {
    public interface ExternalModule {}

    public static class KeyManager {
        public KeyManager(Salter salter, List<ExternalModule> externalModules) {}
    }

    //TODO implement ExternalModule and KeyManager
}
