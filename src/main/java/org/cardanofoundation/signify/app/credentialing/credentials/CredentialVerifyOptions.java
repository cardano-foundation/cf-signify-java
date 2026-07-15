package org.cardanofoundation.signify.app.credentialing.credentials;

import org.cardanofoundation.signify.cesr.Serder;

/**
 * Parameters for {@link Credentials#verify(CredentialVerifyOptions)}.
 *
 * @param acdc    the ACDC Serder to verify
 * @param iss     the TEL issuance (iss) Serder anchoring the ACDC
 * @param issAtc  CESR attachment for {@code iss} (its anchoring KEL seal)
 * @param acdcAtc CESR attachment for {@code acdc}; when null the seal source triple
 *                referencing {@code iss} is derived automatically
 */
public record CredentialVerifyOptions(Serder acdc, Serder iss, String issAtc, String acdcAtc) {
}
