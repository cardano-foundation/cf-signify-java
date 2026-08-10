package id.veridian.signify.cesr.deps;

import id.veridian.signify.cesr.Keeping;

public interface IdentifierDeps extends BaseDeps {
    int getPidx();
    Keeping.KeyManager getManager();
}
