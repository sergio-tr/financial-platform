package io.sergiotr.financialplatform.platform;

/**
 * Process role scaffold for the single backend artifact (SER-34). {@code ALL} is dev/test only.
 */
public enum RuntimeRole {
    GATEWAY,
    WORKER,
    ALL
}