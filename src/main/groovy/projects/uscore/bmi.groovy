package projects.uscore

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.Unity

import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.*
import static de.kairos.fhir.centraxx.metamodel.LaborValue.*
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.*
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Vital Sign Observation Body Mass Index.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-bmi.html
 *
 * hints:
 * - Observation are specified by LOINC codes.
 * - Units are specified by  UCUM codes.
 *
 * Note: The mapping requires labor methods, labor values and units defined in CXX that math the specification of the
 * profile! For more information, see project readme.txt
 *
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 */
observation {
  if ("US_CORE_BMI" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-bmi")
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "39156-5"
    }
  }

  final def laborFindingLaborValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "BODY_MASS_INDEX" }

  valueQuantity {
    value = laborFindingLaborValue[NUMERIC_VALUE]
    unit = laborFindingLaborValue[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
        .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
    system = "http://unitsofmeasure.org"
    code = "kg/m2"
  }
}