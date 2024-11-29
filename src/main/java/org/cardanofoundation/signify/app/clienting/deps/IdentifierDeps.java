package org.cardanofoundation.signify.app.clienting.deps;

import org.cardanofoundation.signify.cesr.Keeping;

public interface IdentifierDeps extends BaseDeps {
    int getPidx();
    void setPidx(int pidx);
    Keeping.KeyManager getManager();
}
