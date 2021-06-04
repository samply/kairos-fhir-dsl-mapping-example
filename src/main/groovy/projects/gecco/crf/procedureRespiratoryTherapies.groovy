package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/respiratorytherapies-procedure
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */


procedure {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemRespThera = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_SAUERSTOFFTHERAPIE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemRespThera){
    return
  }
  if (crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "RespiratoryTherapies/" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies"
    }

    status = "unknown"

    category {
      coding{
        system = "http://snomed.info/sct"
        code = "277132007"
      }
    }

    code {
      crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def LOINCcode = matchResponseToLOINC(item[CatalogEntry.CODE] as String)
        if (LOINCcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/ops"
            code = LOINCcode
          }
        }
      }
      crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }
    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    performedDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}


static String matchResponseToLOINC(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "8-70" // ????
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "53950000"
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}