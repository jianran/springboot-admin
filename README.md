# Embedded Spring Boot Admin Console

A Java library that embeds a small, privileged administration console in an existing Spring Boot 3 application. It uses Sa-Token for sessions and is activated only by `@EnableAdminConsole`.

## Install locally

```bash
mvn install
```

The build produces:

- `admin-console-spring-boot-starter/target/admin-console-spring-boot-starter-0.1.0.jar`
- `demo/target/admin-console-demo-0.1.0.jar`

Then add the library to the host application:

```xml
<dependency>
  <groupId>io.github.jianran</groupId>
  <artifactId>admin-console-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

Enable it explicitly on a Spring configuration class:

```java
@SpringBootApplication
@EnableAdminConsole
public class MyApplication { }
```

Enable the allowlisted Arthas integration directly from the annotation:

```java
@SpringBootApplication
@EnableAdminConsole(enableArthas = true)
public class MyApplication { }
```

The endpoint, timeout, and command allowlist still come from `admin.console.arthas.*` configuration.

Restrict the console to selected Spring profiles:

```java
@EnableAdminConsole(enableArthas = true, profiles = {"dev", "staging"})
```

Profile expressions are supported, so `profiles = "!prod"` enables the console everywhere except production. Leaving `profiles` empty preserves the original behavior and enables the console in every profile.

Configure the privileged capabilities:

```yaml
admin:
  console:
    username: admin
    password-hash: '$2a$10$replaceWithABcryptHash'
    session-timeout: 30m
    max-login-attempts: 5
    login-lockout: 15m
    arthas:
      enabled: true
      endpoint: http://127.0.0.1:8563/api
      allowed-commands: [version, dashboard, thread, jvm, sysprop, sysenv, logger, sc, sm]
    beans:
      writable: [featureFlags.checkoutEnabled]
    jobs:
      triggerable: [billingJobs.reconcile]
```

Only fields annotated with `@FeatureFlag` are shown in the console:

```java
@Component("featureFlags")
class FeatureFlags {
    @FeatureFlag(description = "Enables the redesigned checkout flow")
    private boolean checkoutEnabled = true;
}
```

The field remains read-only unless its exact `beanName.fieldName` is also present in `admin.console.beans.writable`.

Open `http://localhost:8080/admin-console/index.html`. Arthas must already be attached with its HTTP API listening on the configured address.

## Run the demo

```bash
java -jar demo/target/admin-console-demo-0.1.0.jar
```

Open `http://localhost:8080/admin-console/index.html` and log in with the demo-only credentials `admin` / `admin123`.

## Browser automation

The Playwright integration test starts the demo on port `18080`, launches headless Chromium, logs in, searches for the demo bean, and verifies the scheduled job listing.

```bash
mvn -pl demo -Dexec.classpathScope=test \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium" \
  org.codehaus.mojo:exec-maven-plugin:3.5.1:java
mvn -pl demo verify -Pe2e
```

## Publish to Maven Central

The `central-release` profile attaches source and Javadoc JARs, signs every artifact, excludes the demo, and publishes through the Central Portal. Before releasing, verify the `io.github.jianran` namespace in Central, create a Central user token under server id `central` in your local Maven `settings.xml`, and configure a local GPG secret key. Never commit those credentials.

Set a non-SNAPSHOT version and publish:

```bash
mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false
mvn clean deploy -Pcentral-release
```

## Use application authentication

The built-in verifier expects a BCrypt hash. To connect another identity service, expose one bean:

```java
@Bean
AdminCredentialVerifier adminCredentialVerifier(MyIdentityService identity) {
    return (username, password) -> identity.isAdmin(username, password);
}
```

## Security model

- Adding the dependency alone creates no endpoints; `@EnableAdminConsole` is required.
- Bean writes and job triggers have empty allowlists by default.
- Arthas is off by default and commands are checked against a root-command allowlist.
- Keep the host application on a private management network and bind Arthas to loopback.
- Do not expose this console directly to the public internet.
