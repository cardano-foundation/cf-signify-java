package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class CredentialState {
    private int[] vn; // Array of two integers
    private String i;
    private String s;
    private String d;
    private String ri;
    private A a;
    private String dt;
    private String et;

    // Constructor for common properties
    public CredentialState(int[] vn, String i, String s, String d, String ri, A a, String dt, String et) {
        this.vn = vn;
        this.i = i;
        this.s = s;
        this.d = d;
        this.ri = ri;
        this.a = a;
        this.dt = dt;
        this.et = et;
    }

    // Nested class for 'a' property
    @Getter
    @Setter
    public static class A {
        private int s;
        private String d;

        // Constructor, getters, and setters
        public A(int s, String d) {
            this.s = s;
            this.d = d;
        }
    }


    // Subclass for 'et' == 'iss' or 'rev'
    @Getter
    @Setter
    public static class IssRevCredentialState extends CredentialState {
        private Map<String, Object> ra;

        public IssRevCredentialState(int[] vn, String i, String s, String d, String ri, A a, String dt, String et, Map<String, Object> ra) {
            super(vn, i, s, d, ri, a, dt, et);
            this.ra = ra;
        }

    }

    // Subclass for 'et' == 'bis' or 'brv'
    @Getter
    @Setter
    public static class BisBrvCredentialState extends CredentialState {
        private Map<String, String> ra;

        public BisBrvCredentialState(int[] vn, String i, String s, String d, String ri, A a, String dt, String et, Map<String, String> ra) {
            super(vn, i, s, d, ri, a, dt, et);
            this.ra = ra;
        }

    }
}
