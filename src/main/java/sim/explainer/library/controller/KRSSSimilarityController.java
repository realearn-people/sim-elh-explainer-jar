package sim.explainer.library.controller;

import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.service.SimilarityService;
import org.springframework.stereotype.Controller;
import sim.explainer.library.enumeration.ImplementationMethod;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.service.ValidationService;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class KRSSSimilarityController {
    private SimilarityService similarityService;
    private ValidationService validationService;

    public KRSSSimilarityController(ValidationService validationService, SimilarityService similarityService) {
        this.validationService = validationService;
        this.similarityService = similarityService;
    }

    private void validateInputs(String conceptName1, String conceptName2) {
        if (!validationService.validateIfKRSSClassNamesExist(conceptName1, conceptName2)) {
            throw new JSimPiException("Unable to measure similarity with KRSS sim as conceptName1["
                    + conceptName1 + "] and conceptName2[" + conceptName2 + "] are invalid names.", ErrorCode.KrssSimilarityController_InvalidConceptNames);
        }
    }

    /**
     * runchana:2023-31-07
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1
     * @param conceptName2
     * @param type         concept type, i.e., KRSS or OWL
     * @param conceptType  measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     * @return
     */
    public BigDecimal measureSimilarity(String conceptName1, String conceptName2, ImplementationMethod type, FileTypeConstant conceptType) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with " + type.getDescription() + " as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.KrssSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        return similarityService.measureConceptWithType(conceptName1, conceptName2, type, conceptType);
    }

    public List<BacktraceTable> getBacktraceTables() {
        return similarityService.getBacktraceTables();
    }
}