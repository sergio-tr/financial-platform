package io.sergiotr.financialplatform.identity.application.synthetic.password;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Loads the local DEV/TEST common-password deny list (new passwords only). */
public final class SyntheticCommonPasswordBlocklist {

    private static final String RESOURCE = "/synthetic/common-password-blocklist.txt";

    public Set<String> loadFromClasspath() {
        InputStream in = SyntheticCommonPasswordBlocklist.class.getResourceAsStream(RESOURCE);
        if (in == null) {
            throw new IllegalStateException("missing blocklist resource " + RESOURCE);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Set<String> entries = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                entries.add(trimmed);
            }
            if (entries.isEmpty()) {
                throw new IllegalStateException("blocklist is empty");
            }
            return Set.copyOf(entries);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load blocklist", ex);
        }
    }

    public static SyntheticPasswordPolicy defaultPolicy() {
        return new SyntheticPasswordPolicy(new SyntheticCommonPasswordBlocklist().loadFromClasspath());
    }
}