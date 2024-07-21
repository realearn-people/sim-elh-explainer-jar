package sim.explainer.library.controller;

import org.springframework.stereotype.Controller;
import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.service.SimilarityService;
import sim.explainer.library.service.ValidationService;
import sim.explainer.library.enumeration.ImplementationMethod;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class OWLSimilarityController {
    private ValidationService validationService;
    private SimilarityService similarityService;

    public OWLSimilarityController(ValidationService validationService, SimilarityService similarityService) {
        this.validationService = validationService;
        this.similarityService = similarityService;
    }

    private void validateInputs(String conceptName1, String conceptName2) {
        if (!validationService.validateIfOWLClassNamesExist(conceptName1, conceptName2)) {
            throw new JSimPiException("Unable to measure similarity with OWL sim as conceptName1["
                    + conceptName1 + "] and conceptName2[" + conceptName2 + "] are invalid names.", ErrorCode.OwlSimilarityController_InvalidConceptNames);
        }
    }

    public BigDecimal measureSimilarity(String conceptName1, String conceptName2, ImplementationMethod type, FileTypeConstant conceptType) {
        if(conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with " + type.getDescription() + " as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.OwlSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        return similarityService.measureConceptWithType(conceptName1, conceptName2, type, conceptType);
    }

    public List<BacktraceTable> getBacktraceTables() {
        return similarityService.getBacktraceTables();
    }

}
