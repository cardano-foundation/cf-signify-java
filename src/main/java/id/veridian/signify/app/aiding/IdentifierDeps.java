package id.veridian.signify.app.aiding;

import id.veridian.signify.app.coring.deps.BaseDeps;
import id.veridian.signify.cesr.Keeping;

public interface IdentifierDeps extends BaseDeps {
    int getPidx();
    void setPidx(int pidx);
    Keeping.KeyManager getManager();
}
