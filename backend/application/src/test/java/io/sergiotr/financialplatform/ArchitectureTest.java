package io.sergiotr.financialplatform;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.sergiotr.financialplatform.testsupport.ArchitectureRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
class ArchitectureTest {

    private static final String BASE = "io.sergiotr.financialplatform";

    @Test
    void modulithStructureIsValid() {
        ApplicationModules.of(FinancialPlatformApplication.class).verify();
    }

    @Test
    void codeResidesInBasePackage() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
        ArchitectureRules.classesResideInBasePackage().check(classes);
    }
}