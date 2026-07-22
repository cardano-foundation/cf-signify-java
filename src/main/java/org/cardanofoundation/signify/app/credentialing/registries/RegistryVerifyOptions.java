package org.cardanofoundation.signify.app.credentialing.registries;

import org.cardanofoundation.signify.cesr.Serder;

/**
 * Parameters for {@link Registries#verify(RegistryVerifyOptions)}.
 *
 * @param vcp the registry inception (vcp) Serder to verify
 * @param atc CESR attachment for {@code vcp} (its anchoring KEL seal)
 */
public record RegistryVerifyOptions(Serder vcp, String atc) {
}
