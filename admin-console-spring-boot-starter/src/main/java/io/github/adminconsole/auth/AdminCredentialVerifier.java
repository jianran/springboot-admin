package io.github.adminconsole.auth;

/** Replace this bean to authenticate against the host application's identity store. */
@FunctionalInterface
public interface AdminCredentialVerifier {
    boolean verify(String username, String password);
}
