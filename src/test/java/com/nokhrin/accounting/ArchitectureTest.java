package com.nokhrin.accounting;


import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.nokhrin.accounting",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT = "com.nokhrin.accounting";

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers =
            noClasses()
                    .that().resideInAPackage(ROOT + ".domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            ROOT + ".api..",
                            ROOT + ".application..",
                            ROOT + ".infrastructure.."
                    );

    @ArchTest
    static final ArchRule applicationDoesNotDependOnApiOrInfrastructure =
            noClasses()
                    .that().resideInAPackage(ROOT + ".application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            ROOT + ".api..",
                            ROOT + ".infrastructure.."
                    );

    @ArchTest
    static final ArchRule apiDoesNotDependOnInfrastructure =
            noClasses()
                    .that().resideInAPackage(ROOT + ".api..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(ROOT + ".infrastructure..");

    @ArchTest
    static final ArchRule generatedModelsDoNotDependOnDomain = noClasses()
            .that().resideInAPackage(ROOT + ".generated.model..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".domain..");

    @ArchTest
    static final ArchRule noCyclesBetweenSlices =
            slices()
                    .matching(ROOT + ".(*)..")
                    .should().beFreeOfCycles();
}
