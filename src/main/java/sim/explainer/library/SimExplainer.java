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

public class SimExplainer {
    private FileTypeConstant fileType;

    private final PreferenceProfile preferenceProfile = new PreferenceProfile();

    private final OWLServiceContext owlServiceContext = new OWLServiceContext();
    private final KRSSServiceContext krssServiceContext = new KRSSServiceContext();

    private final SimilarityService similarityService = new SimilarityService(owlServiceContext, krssServiceContext, preferenceProfile);
    private final ValidationService validationService = new ValidationService(owlServiceContext, krssServiceContext);

    private static ExplanationConverterService explanationConverterService = new ExplanationConverterService();

    private final HashMap<SymmetricPair<String>, ExplanationService> explanationMap = new HashMap<>();

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

    public void ReadInputPrimitiveConceptImportances(String pathToFile) throws IOException {
        String[] primitiveConceptImportances = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveConceptImportance : primitiveConceptImportances) {
            String[] str = StringUtils.split(primitiveConceptImportance);
            preferenceProfile.addPrimitiveConceptImportance(str[0], new BigDecimal(str[1]));
        }
    }

    public void ReadInputRoleImportances(String pathToFile) throws IOException {
        String[] roleImportances = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String roleImportance : roleImportances) {
            String[] str = StringUtils.split(roleImportance);
            preferenceProfile.addRoleImportance(str[0], new BigDecimal(str[1]));
        }
    }

    public void ReadInputPrimitiveConceptsSimilarities(String pathToFile) throws IOException {
        String[] primitiveConceptsSimilarities = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveConceptsSimilarity : primitiveConceptsSimilarities) {
            String[] str = StringUtils.split(primitiveConceptsSimilarity);
            preferenceProfile.addPrimitveConceptsSimilarity(str[0], str[1], new BigDecimal(str[2]));
        }
    }

    public void ReadInputPrimitiveRolesSimilarities(String pathToFile) throws IOException {
        String[] primitiveRolesSimilarities = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String primitiveRolesSimilarity : primitiveRolesSimilarities) {
            String[] str = StringUtils.split(primitiveRolesSimilarity);
            preferenceProfile.addPrimitiveRolesSimilarity(str[0], str[1], new BigDecimal(str[2]));
        }
    }

    public void ReadInputRoleDiscountFactors(String pathToFile) throws IOException {
        String[] roleDiscountFactors = StringUtils.split(FileUtils.readFileToString(new File(pathToFile)), "\n");
        for (String roleDiscountFactor : roleDiscountFactors) {
            String[] str = StringUtils.split(roleDiscountFactor);
            preferenceProfile.addRoleDiscountFactor(str[0], new BigDecimal(str[1]));
        }

    }

    public void setDefaultRoleDiscountFactor(BigDecimal value) {
        preferenceProfile.setDefaultRoleDiscountFactor(value);
    }


    public void resetPreferenceProfile() {
        preferenceProfile.reset();
    }

    public BigDecimal similarity(ImplementationMethod optionVal, String concept1, String concept2) {
        if (optionVal == null) {
            throw new JSimPiException("Option not provide", ErrorCode.Application_IllegalArguments);
        }
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provide", ErrorCode.Application_IllegalArguments);
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

    private void addExplanationMap(String concept1, String concept2, BigDecimal similarity, BacktraceTable backtraceTable_forward, BacktraceTable backtraceTable_backward) {
        ExplanationService explanationService;

        explanationService = new ExplanationService(similarity, backtraceTable_forward, backtraceTable_backward);

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        explanationMap.put(pair, explanationService);
    }

    public String treeHierachy(String... concepts) {
        if (concepts == null || concepts.length == 0) {
            throw new JSimPiException("Concept not provide", ErrorCode.Application_IllegalArguments);
        }

        StringBuilder builder = new StringBuilder();

        for (String concept: concepts) {
            for (Map.Entry<SymmetricPair<String>, ExplanationService> entry: explanationMap.entrySet()) {
                ExplanationService explanationService = entry.getValue();

                try {
                    builder.append(explanationService.treeHierarchy(concept));
                    break;
                } catch (JSimPiException e) {
                    throw new JSimPiException("[" + concept + "] have not been processed yet: " + e.toString(), ErrorCode.Application_IllegalArguments);
                }
            }
        }

        return builder.toString();
    }

    public Explanation getExplanation(String concept1, String concept2) {
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provide", ErrorCode.Application_IllegalArguments);
        }

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        if (!explanationMap.containsKey(pair)) {
            throw new JSimPiException("Have not been calculate similarity between [" + concept1 + "] and [" + concept2 + "]  yet.", ErrorCode.Application_IllegalArguments);
        }

        Explanation explanation = new Explanation();
        explanation.similarity = explanationMap.get(pair).getSimilarity();
        explanation.forward = explanationMap.get(pair).explanationTree(ReasoningDirectionConstant.FORWARD);
        explanation.backward = explanationMap.get(pair).explanationTree(ReasoningDirectionConstant.BACKWARD);

        return explanation;
    }

    static class Explanation {
        public BigDecimal similarity;
        public String forward;
        public String backward;
    }

    public JSONObject getExplanationAsJson(String concept1, String concept2) {
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provided", ErrorCode.Application_IllegalArguments);
        }

        SymmetricPair<String> pair = new SymmetricPair<>(concept1, concept2);

        if (!explanationMap.containsKey(pair)) {
            throw new JSimPiException("Have not been calculate similarity between [" + concept1 + "] and [" + concept2 + "]  yet.", ErrorCode.Application_IllegalArguments);
        }

        JSONObject explanation = new JSONObject();
        explanation.put("similarity", explanationMap.get(pair).getSimilarity());
        explanation.put("forward", explanationMap.get(pair).explanationTreeAsJson(ReasoningDirectionConstant.FORWARD));
        explanation.put("backward", explanationMap.get(pair).explanationTreeAsJson(ReasoningDirectionConstant.BACKWARD));

        return explanation;
    }

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

    public void setApiTimeout(int apiTimeout) {
        explanationConverterService.setApiTimeout(apiTimeout);
    }

    public void setApiKey(String apiKey) {
        explanationConverterService.setApiKey(apiKey);
    }

    public JSONObject getExplantionAsNaturalLanguage(String concept1, String concept2) {
        JSONObject explanation = getExplanationAsJson(concept1, concept2);

        JSONObject forward_explanation = ExplanationConverterService.convertExplanation(explanation.getJSONObject("forward"));
        JSONObject backward_explanation = ExplanationConverterService.convertExplanation(explanation.getJSONObject("backward"));

        JSONObject result = new JSONObject();
        result.put("similarity", explanation.getBigDecimal("similarity"));
        result.put("forward", forward_explanation);
        result.put("backward", backward_explanation);

        return result;
    }

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
