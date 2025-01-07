package org.cardanofoundation.signify.app.aiding;

import org.cardanofoundation.signify.app.coring.deps.BaseDeps;
import org.cardanofoundation.signify.cesr.Keeping;

public interface IdentifierDeps extends BaseDeps {
    int getPidx();
    void setPidx(int pidx);
    Keeping.KeyManager getManager();
}
