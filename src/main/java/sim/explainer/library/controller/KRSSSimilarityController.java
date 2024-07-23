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

/**
 * Controller for measuring similarity between KRSS concepts.
 */
@Controller
public class KRSSSimilarityController {
    private final SimilarityService similarityService;
    private final ValidationService validationService;

    /**
     * Constructs a {@code KRSSSimilarityController} with the given validation and similarity services.
     *
     * @param validationService the validation service
     * @param similarityService the similarity service
     */
    public KRSSSimilarityController(ValidationService validationService, SimilarityService similarityService) {
        this.validationService = validationService;
        this.similarityService = similarityService;
    }

    /**
     * Validates the given concept names to ensure they exist in the KRSS ontology.
     *
     * @param conceptName1 the first concept name
     * @param conceptName2 the second concept name
     * @throws JSimPiException if any of the concept names are invalid
     */
    private void validateInputs(String conceptName1, String conceptName2) {
        if (!validationService.validateIfKRSSClassNamesExist(conceptName1, conceptName2)) {
            throw new JSimPiException("Unable to measure similarity with KRSS sim as conceptName1["
                    + conceptName1 + "] and conceptName2[" + conceptName2 + "] are invalid names.",
                    ErrorCode.KrssSimilarityController_InvalidConceptNames);
        }
    }

    /**
     * Measures the similarity between two KRSS concepts using the specified implementation method and concept type.
     *
     * @param conceptName1 the first concept name
     * @param conceptName2 the second concept name
     * @param type the implementation method
     * @param fileType the file type
     * @return the similarity score between the two concepts
     * @throws JSimPiException if any of the concept names are null or invalid
     */
    public BigDecimal measureSimilarity(String conceptName1, String conceptName2, ImplementationMethod type, FileTypeConstant fileType) {
        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable to measure similarity with " + type.getDescription() + " as conceptName1[" + conceptName1
                    + "] and conceptName2[" + conceptName2 + "] are null.",
                    ErrorCode.KrssSimilarityController_IllegalArguments);
        }

        validateInputs(conceptName1, conceptName2);

        return similarityService.measureConceptWithType(conceptName1, conceptName2, type, fileType);
    }

    /**
     * Returns the backtrace tables generated during the similarity measurement.
     *
     * @return the list of backtrace tables
     */
    public List<BacktraceTable> getBacktraceTables() {
        return similarityService.getBacktraceTables();
    }
}
