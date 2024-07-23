package sim.explainer.library;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import sim.explainer.library.controller.KRSSSimilarityController;
import sim.explainer.library.controller.OWLSimilarityController;
import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.enumeration.ReasoningDirectionConstant;
import sim.explainer.library.enumeration.ImplementationMethod;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.KRSSServiceContext;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.service.ExplanationConverterService;
import sim.explainer.library.service.ExplanationService;
import sim.explainer.library.service.SimilarityService;
import sim.explainer.library.service.ValidationService;
import sim.explainer.library.util.utilstructure.SymmetricPair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * The {@code SimExplainer} class is responsible for loading ontologies, processing preference profiles,
 * and calculating similarity between concepts. It supports both OWL and KRSS file types and provides
 * functionality to retrieve and output similarity explanations in various formats.
 */
public class SimExplainer {

    private FileTypeConstant fileType;
    private final PreferenceProfile preferenceProfile = new PreferenceProfile();
    private final OWLServiceContext owlServiceContext = new OWLServiceContext();
    private final KRSSServiceContext krssServiceContext = new KRSSServiceContext();
    private final SimilarityService similarityService = new SimilarityService(owlServiceContext, krssServiceContext, preferenceProfile);
    private final ValidationService validationService = new ValidationService(owlServiceContext, krssServiceContext);
    private static ExplanationConverterService explanationConverterService = new ExplanationConverterService();
    private final HashMap<SymmetricPair<String>, ExplanationService> explanationMap = new HashMap<>();

    /**
     * Constructs a {@code SimExplainer} object and initializes it by loading ontologies and preference
     * profile files from the specified directory.
     *
     * @param directoryPath the path to the directory containing the ontology and preference profile files
     */
    public SimExplainer(String directoryPath) {
        Path onto_dir = Paths.get(directoryPath);

        // ontology path
        try (Stream<Path> stream = Files.walk(onto_dir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        String fileAbsPath = file.toAbsolutePath().toString();

                        if (fileName.endsWith(".krss") || fileName.endsWith(".owl") || fileName.endsWith(".owx")) {
                            load_ontology(fileAbsPath);
                        }

                        try {
                            if (fileName.startsWith("primitive-concept-importance")) {
                                ReadInputPrimitiveConceptImportances(fileAbsPath);
                            } else if (fileName.startsWith("role-importance")) {
                                ReadInputRoleImportances(fileAbsPath);
                            } else if (fileName.startsWith("primitive-concepts-similarity")) {
                                ReadInputPrimitiveConceptsSimilarities(fileAbsPath);
                            } else if (fileName.startsWith("primitive-roles-similarity")) {
                                ReadInputPrimitiveRolesSimilarities(fileAbsPath);
                            } else if (fileName.startsWith("role-discount-factor")) {
                                ReadInputRoleDiscountFactors(fileAbsPath);
                            }
                        } catch (IOException e) {
                            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
                        }
                    });
        } catch (IOException e) {
            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
        }
    }

    /**
     * Constructs a {@code SimExplainer} object and initializes it by loading ontologies from the specified
     * ontology directory and preference profiles from the specified preference profile directory.
     *
     * @param ontologyDirectoryPath the path to the directory containing the ontology files
     * @param preferenceProfileDirectoryPath the path to the directory containing the preference profile files
     */
    public SimExplainer(String ontologyDirectoryPath, String preferenceProfileDirectoryPath) {
        Path onto_dir = Paths.get(ontologyDirectoryPath);

        // ontology path
        try (Stream<Path> stream = Files.walk(onto_dir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        String fileAbsPath = file.toAbsolutePath().toString();

                        if (fileName.endsWith(".krss") || fileName.endsWith(".owl") || fileName.endsWith(".owx")) {
                            load_ontology(fileAbsPath);
                        }
                    });
        } catch (IOException e) {
            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
        }

        if (preferenceProfileDirectoryPath == null) {
            return;
        }

        Path prefer_dir = Paths.get(preferenceProfileDirectoryPath);

        try (Stream<Path> stream = Files.walk(prefer_dir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        String fileAbsPath = file.toAbsolutePath().toString();

                        try {
                            if (fileName.startsWith("primitive-concept-importance")) {
                                ReadInputPrimitiveConceptImportances(fileAbsPath);
                            } else if (fileName.startsWith("role-importance")) {
                                ReadInputRoleImportances(fileAbsPath);
                            } else if (fileName.startsWith("primitive-concepts-similarity")) {
                                ReadInputPrimitiveConceptsSimilarities(fileAbsPath);
                            } else if (fileName.startsWith("primitive-roles-similarity")) {
                                ReadInputPrimitiveRolesSimilarities(fileAbsPath);
                            } else if (fileName.startsWith("role-discount-factor")) {
                                ReadInputRoleDiscountFactors(fileAbsPath);
                            }
                        } catch (IOException e) {
                            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
                        }

                    });
        } catch (IOException e) {
            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
        }
    }

    /**
     * Constructs a {@code SimExplainer} object and initializes it by loading the ontology and preference
     * profile files from the specified paths.
     *
     * @param ontologyPath the path to the ontology file
     * @param primitiveConceptImportancePath the path to the primitive concept importance file
     * @param roleImportancePath the path to the role importance file
     * @param primitiveConceptsSimilarityPath the path to the primitive concepts similarity file
     * @param primitiveRolesSimilarityPath the path to the primitive roles similarity file
     * @param roleDiscountFactorPath the path to the role discount factor file
     */
    public SimExplainer(
            String ontologyPath,
            String primitiveConceptImportancePath,
            String roleImportancePath,
            String primitiveConceptsSimilarityPath,
            String primitiveRolesSimilarityPath,
            String roleDiscountFactorPath) {

        // ontology
        load_ontology(ontologyPath);

        // preferences profile file
        try {
            if (primitiveConceptImportancePath != null) {
                this.ReadInputPrimitiveConceptImportances(primitiveConceptImportancePath);
            }
            if (roleImportancePath != null) {
                this.ReadInputRoleImportances(roleImportancePath);
            }
            if (primitiveConceptsSimilarityPath != null) {
                this.ReadInputPrimitiveConceptsSimilarities(primitiveConceptsSimilarityPath);
            }
            if (primitiveRolesSimilarityPath != null) {
                this.ReadInputPrimitiveRolesSimilarities(primitiveRolesSimilarityPath);
            }
            if (roleDiscountFactorPath != null) {
                this.ReadInputRoleDiscountFactors(roleDiscountFactorPath);
            }
        } catch (IOException exception) {
            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
        }
    }

    /**
     * Loads the ontology from the specified path and initializes the appropriate service context based on
     * the file type.
     *
     * @param ontologyPath the path to the ontology file
     * @throws JSimPiException if the file type is not supported
     */
    private void load_ontology(String ontologyPath) {
        File ontologyFile = new File(ontologyPath);

        this.fileType = ValidationService.checkOWLandKRSSFile(ontologyFile);

        switch (fileType) {
            case OWL_FILE:
                owlServiceContext.init(ontologyPath);
                break;
            case KRSS_FILE:
                krssServiceContext.init(ontologyPath);
                break;
            default:
                throw new JSimPiException("File type not supported", ErrorCode.Application_InvalidFileType);
        }
    }

    /**
     * Reads primitive concept importances from the specified file and adds them to the preference profile.
     *
     * @param pathToFile the path to the file containing primitive concept importances
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void ReadInputPrimitiveConceptImportances(String pathToFile) throws IOException {
        String[] primitiveConceptImportances = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveConceptImportance : primitiveConceptImportances) {
            String[] str = StringUtils.split(primitiveConceptImportance);
            preferenceProfile.addPrimitiveConceptImportance(str[0], new BigDecimal(str[1]));
        }
    }

    /**
     * Reads role importances from the specified file and adds them to the preference profile.
     *
     * @param pathToFile the path to the file containing role importances
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void ReadInputRoleImportances(String pathToFile) throws IOException {
        String[] roleImportances = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String roleImportance : roleImportances) {
            String[] str = StringUtils.split(roleImportance);
            preferenceProfile.addRoleImportance(str[0], new BigDecimal(str[1]));
        }
    }

    /**
     * Reads primitive concepts similarities from the specified file and adds them to the preference profile.
     *
     * @param pathToFile the path to the file containing primitive concepts similarities
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void ReadInputPrimitiveConceptsSimilarities(String pathToFile) throws IOException {
        String[] primitiveConceptsSimilarities = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveConceptsSimilarity : primitiveConceptsSimilarities) {
            String[] str = StringUtils.split(primitiveConceptsSimilarity);
            preferenceProfile.addPrimitveConceptsSimilarity(str[0], str[1], new BigDecimal(str[2]));
        }
    }

    /**
     * Reads primitive roles similarities from the specified file and adds them to the preference profile.
     *
     * @param pathToFile the path to the file containing primitive roles similarities
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void ReadInputPrimitiveRolesSimilarities(String pathToFile) throws IOException {
        String[] primitiveRolesSimilarities = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveRolesSimilarity : primitiveRolesSimilarities) {
            String[] str = StringUtils.split(primitiveRolesSimilarity);
            preferenceProfile.addPrimitiveRolesSimilarity(str[0], str[1], new BigDecimal(str[2]));
        }
    }

    /**
     * Reads role discount factors from the specified file and adds them to the preference profile.
     *
     * @param pathToFile the path to the file containing role discount factors
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void ReadInputRoleDiscountFactors(String pathToFile) throws IOException {
        String[] roleDiscountFactors = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String roleDiscountFactor : roleDiscountFactors) {
            String[] str = StringUtils.split(roleDiscountFactor);
            preferenceProfile.addRoleDiscountFactor(str[0], new BigDecimal(str[1]));
        }
    }

    /**
     * Sets the default role discount factor in the preference profile.
     *
     * @param value the default role discount factor
     */
    public void setDefaultRoleDiscountFactor(BigDecimal value) {
        preferenceProfile.setDefaultRoleDiscountFactor(value);
    }

    /**
     * Resets the preference profile to its default state.
     */
    public void resetPreferenceProfile() {
        preferenceProfile.reset();
    }

    /**
     * Calculates the similarity between two concepts using the specified implementation method.
     *
     * @param optionVal the implementation method to use for similarity calculation
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @return the similarity score between the two concepts
     * @throws JSimPiException if any of the arguments are null or if the file type is not supported
     */
    public BigDecimal similarity(ImplementationMethod optionVal, String concept1, String concept2) {
        if (optionVal == null) {
            throw new JSimPiException("Option not provided", ErrorCode.Application_IllegalArguments);
        }
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        // result variable
        BigDecimal result;

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        if (explanationMap.containsKey(pair)) {
            return explanationMap.get(pair).getSimilarity();
        }

        switch (this.fileType) {
            case KRSS_FILE -> {
                KRSSSimilarityController krssSimilarityController = new KRSSSimilarityController(validationService, similarityService);

                result = krssSimilarityController.measureSimilarity(concept1, concept2, optionVal, this.fileType);
                List<BacktraceTable> backtraceTables = krssSimilarityController.getBacktraceTables();

                addExplanationMap(concept1, concept2, result, backtraceTables.get(0), backtraceTables.get(1));
            }
            case OWL_FILE -> {
                OWLSimilarityController owlSimilarityController = new OWLSimilarityController(validationService, similarityService);

                result = owlSimilarityController.measureSimilarity(concept1, concept2, optionVal, this.fileType);
                List<BacktraceTable> backtraceTables = owlSimilarityController.getBacktraceTables();

                addExplanationMap(concept1, concept2, result, backtraceTables.get(0), backtraceTables.get(1));
            }
            default -> throw new JSimPiException("File type not supported.", ErrorCode.Application_InvalidFileType);
        }

        return result;
    }

    /**
     * Adds an explanation of the similarity between two concepts to the explanation map.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @param similarity the similarity score between the two concepts
     * @param backtraceTable_forward the forward backtrace table
     * @param backtraceTable_backward the backward backtrace table
     */
    private void addExplanationMap(String concept1, String concept2, BigDecimal similarity, BacktraceTable backtraceTable_forward, BacktraceTable backtraceTable_backward) {
        ExplanationService explanationService;

        explanationService = new ExplanationService(similarity, backtraceTable_forward, backtraceTable_backward);

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        explanationMap.put(pair, explanationService);
    }

    /**
     * Returns the tree hierarchy explanation for the given concepts.
     *
     * @param concepts the concepts to retrieve the hierarchy for
     * @return the tree hierarchy explanation as a string
     * @throws JSimPiException if no concepts are provided
     */
    public String treeHierachy(String... concepts) {
        if (concepts == null || concepts.length == 0) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        StringBuilder builder = new StringBuilder();

        for (String concept : concepts) {
            for (Map.Entry<SymmetricPair<String>, ExplanationService> entry : explanationMap.entrySet()) {
                ExplanationService explanationService = entry.getValue();

                try {
                    builder.append(explanationService.treeHierarchy(concept));
                    break;
                } catch (JSimPiException e) {
                    // do nothing
                }
            }
        }

        return builder.toString();
    }

    /**
     * Returns the tree hierarchy explanation for the given concept as a JSON object.
     *
     * @param concept the concept to retrieve the hierarchy for
     * @return the tree hierarchy explanation as a JSON object
     * @throws JSimPiException if the concept is null
     */
    public JSONObject treeHierarchyAsJson(String concept) {
        if (concept == null) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        JSONObject resultJson = new JSONObject();

        for (Map.Entry<SymmetricPair<String>, ExplanationService> entry : explanationMap.entrySet()) {
            ExplanationService explanationService = entry.getValue();

            try {
                return explanationService.treeHierarchyAsJson(concept);
            } catch (JSimPiException e) {
                // do nothing
            }
        }

        throw new JSimPiException("[" + concept + "] has not been processed yet", ErrorCode.Application_IllegalArguments);
    }

    /**
     * Writes the tree hierarchy explanation for the given concept to a file in JSON format.
     *
     * @param concept the concept to retrieve the hierarchy for
     * @param outputPath the path to the output file
     * @return the tree hierarchy explanation as a JSON object
     */
    public JSONObject treeHierarchyAsJson(String concept, String outputPath) {
        JSONObject jsonResult = treeHierarchyAsJson(concept);

        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(jsonResult.toString(4)); // Write JSON with indentation
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        return jsonResult;
    }

    /**
     * Retrieves the explanation for the similarity between two concepts.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @return the explanation of the similarity
     * @throws JSimPiException if any of the concepts are null or if the similarity between the concepts
     *                          has not been calculated yet
     */
    public Explanation getExplanation(String concept1, String concept2) {
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        if (!explanationMap.containsKey(pair)) {
            throw new JSimPiException("Similarity between [" + concept1 + "] and [" + concept2 + "] has not been calculated yet.", ErrorCode.Application_IllegalArguments);
        }

        ExplanationService explanationService = null;
        for (SymmetricPair<String> key : explanationMap.keySet()) {
            if (key.equals(pair)) {
                if (key.equalsOrder(pair)) {
                    explanationService = explanationMap.get(pair);
                } else {
                    ExplanationService tmp = explanationMap.get(pair);
                    explanationService = new ExplanationService(tmp.getSimilarity(), tmp.getBackwardBacktraceTable(), tmp.getForwardBacktraceTable());
                }
            }
        }

        Explanation explanation = new Explanation();
        explanation.similarity = explanationService.getSimilarity();
        explanation.forward = explanationService.explanationTree(ReasoningDirectionConstant.FORWARD);
        explanation.backward = explanationService.explanationTree(ReasoningDirectionConstant.BACKWARD);

        return explanation;
    }

    class Explanation {
        public BigDecimal similarity;
        public String forward;
        public String backward;
    }


    /**
     * Returns the explanation for the similarity between two concepts as a JSON object.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @return the explanation of the similarity as a JSON object
     * @throws JSimPiException if any of the concepts are null or if the similarity between the concepts
     *                          has not been calculated yet
     */
    public JSONObject getExplanationAsJson(String concept1, String concept2) {
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        if (!explanationMap.containsKey(pair)) {
            throw new JSimPiException("Similarity between [" + concept1 + "] and [" + concept2 + "] has not been calculated yet.", ErrorCode.Application_IllegalArguments);
        }

        ExplanationService explanationService = null;
        for (SymmetricPair<String> key : explanationMap.keySet()) {
            if (key.equals(pair)) {
                if (key.equalsOrder(pair)) {
                    explanationService = explanationMap.get(pair);
                } else {
                    ExplanationService tmp = explanationMap.get(pair);
                    explanationService = new ExplanationService(tmp.getSimilarity(), tmp.getBackwardBacktraceTable(), tmp.getForwardBacktraceTable());
                }
            }
        }

        JSONObject explanation = new JSONObject();
        explanation.put("similarity", explanationService.getSimilarity());
        explanation.put("forward", explanationService.explanationTreeAsJson(ReasoningDirectionConstant.FORWARD));
        explanation.put("backward", explanationService.explanationTreeAsJson(ReasoningDirectionConstant.BACKWARD));

        return explanation;
    }

    /**
     * Writes the explanation for the similarity between two concepts to a file in JSON format.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @param outputPath the path to the output file
     * @return the explanation of the similarity as a JSON object
     */
    public JSONObject getExplanationAsJson(String concept1, String concept2, String outputPath) {
        JSONObject explanation = getExplanationAsJson(concept1, concept2);

        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(explanation.toString(4)); // Write JSON with indentation
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        return explanation;
    }

    /**
     * Sets the API timeout for the explanation converter service.
     *
     * @param apiTimeout the API timeout in milliseconds
     */
    public void setApiTimeout(int apiTimeout) {
        explanationConverterService.setApiTimeout(apiTimeout);
    }

    /**
     * Sets the API key for the explanation converter service.
     *
     * @param apiKey the API key
     */
    public void setApiKey(String apiKey) {
        explanationConverterService.setApiKey(apiKey);
    }

    /**
     * Converts the explanation for the similarity between two concepts into natural language.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @return the explanation as natural language in JSON format
     */
    public JSONObject getExplantionAsNaturalLanguage(String concept1, String concept2) {
        JSONObject explanation = getExplanationAsJson(concept1, concept2);

        return ExplanationConverterService.convertExplanationBiDirectionTree(explanation);
    }

    /**
     * Converts the explanation for the similarity between two concepts into natural language and writes it to a file.
     *
     * @param concept1 the first concept
     * @param concept2 the second concept
     * @param outputPath the path to the output file
     * @return the explanation as natural language in JSON format
     */
    public JSONObject getExplantionAsNaturalLanguage(String concept1, String concept2, String outputPath) {
        JSONObject explanation = getExplantionAsNaturalLanguage(concept1, concept2);

        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(explanation.toString(4)); // Write JSON with indentation
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }

        return explanation;
    }

    /**
     * Retrieves a list of concept names from the ontology.
     *
     * @return the list of concept names
     * @throws JSimPiException if the file type is not supported
     */
    public List<String> retrieveConceptName() {
        List<String> conceptNames = new ArrayList<>();

        switch (fileType) {
            case OWL_FILE:
                ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

                conceptNames.addAll(owlServiceContext.getOwlOntology().getClassesInSignature().stream()
                        .map(shortFormProvider::getShortForm)
                        .filter(className -> !className.equals("Thing"))
                        .toList());
                break;

            case KRSS_FILE:
                conceptNames.addAll(krssServiceContext.getFullConceptDefinitionMap().keySet());
                conceptNames.addAll(krssServiceContext.getPrimitiveConceptDefinitionMap().keySet());
                break;

            default:
                throw new JSimPiException("File type not supported", ErrorCode.Application_InvalidFileType);
        }

        return conceptNames;
    }
}
