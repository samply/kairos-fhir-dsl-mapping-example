package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/Chronicneurologicalormentaldiseases
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemNeuro = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_NEURO_ERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if(!crfItemNeuro){
    return
  }
  if (crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "NeuroDisease/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases"
    }

    extension {
      url = "https://simplifier.net/forschungsnetzcovid-19/uncertaintyofpresence"
      valueCodeableConcept {
        coding {
          system = "http://snomed.info/sct"
          code = "261665006"
        }
      }
    }
    category {
      coding {
        system = "http://snomed.info/sct"
        code = "394591006"
      }
      coding {
        system = "http://snomed.info/sct"
        code = "394587001"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }


    code {
      crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }
    recordedDate {
      date = normalizeDate(crfItemNeuro[CrfItem.CREATIONDATE] as String)
    }
  }
}


static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_ANGSTERKRANKUNG"):
      return "F41.9"
    case ("COV_DEPRESSION"):
      return "F32.9"
    case ("COV_PSYCHOSE"):
      return "F29"
    case ("COV_PARKINSON"):
      return "G20"
    case ("COV_DEMENZ"):
      return "F03"
    case ("COV_MS"):
      return "G35"
    case ("COV_NEUROMUSK_ERKRANKUNG"):
      return "G70.9"
    case ("COV_EPILEPSIE"):
      return "G40.9"
    case ("COV_MIGRAENE"):
      return "G43.9"
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_ANGSTERKRANKUNG"):
      return "197480006"
    case ("COV_DEPRESSION"):
      return "35489007"
    case ("COV_PSYCHOSE"):
      return "69322001"
    case ("COV_PARKINSON"):
      return "49049000"
    case ("COV_DEMENZ"):
      return "52448006"
    case ("COV_MS"):
      return "24700007"
    case ("COV_NEUROMUSK_ERKRANKUNG"):
      return "257277002"
    case ("COV_EPILEPSIE"):
      return "84757009"
    case ("COV_MIGRAENE"):
      return "37796009"
    case ("COV_APOPLEX_RESIDUEN"):
      return "440140008"
    case ("COV_APOPLEX_O_RESIDUEN"):
      return "429993008"
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}