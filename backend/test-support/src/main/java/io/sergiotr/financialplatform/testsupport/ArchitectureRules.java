package io.sergiotr.financialplatform.testsupport;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public final class ArchitectureRules {

    private ArchitectureRules() {}

    public static ArchRule classesResideInBasePackage() {
        return classes().should().resideInAPackage("io.sergiotr.financialplatform..");
    }
}