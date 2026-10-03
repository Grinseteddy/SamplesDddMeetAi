package org.larder.platform.test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Hexagonal rules every Bounded Context module must satisfy. Isolation between
 * Bounded Contexts is enforced by Maven (a context module may not depend on another
 * one); these rules guard the inside of a context.
 */
public final class BoundedContextArchitectureRules {

    private BoundedContextArchitectureRules() {
    }

    public static void verify(String basePackage) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);

        noClasses().that().resideInAPackage(basePackage + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        basePackage + ".application..", basePackage + ".adapter..",
                        "org.springframework..", "jakarta.persistence..")
                .because("the domain model is free of frameworks and adapters")
                .allowEmptyShould(true)
                .check(classes);

        noClasses().that().resideInAPackage(basePackage + ".application..")
                .should().dependOnClassesThat().resideInAPackage(basePackage + ".adapter..")
                .because("use cases talk to adapters only through ports")
                .allowEmptyShould(true)
                .check(classes);

        noClasses().that().resideInAPackage(basePackage + ".adapter.in..")
                .should().dependOnClassesThat().resideInAPackage(basePackage + ".adapter.out..")
                .because("inbound adapters reach outbound adapters only through the application")
                .allowEmptyShould(true)
                .check(classes);
    }
}
