package org.cardanofoundation.signify.cesr.deps;

import org.cardanofoundation.signify.cesr.Keeping;

public interface IdentifierDeps extends BaseDeps {
    int getPidx();
    Keeping.KeyManager getManager();
}
