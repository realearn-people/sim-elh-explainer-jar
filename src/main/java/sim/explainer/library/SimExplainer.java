package sim.explainer.library;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.enumeration.TypeConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.KRSSServiceContext;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.service.ExplanationService;
import sim.explainer.library.service.PreferenceProfile;
import sim.explainer.library.service.SimilarityService;
import sim.explainer.library.service.ValidationService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SimExplainer {
    private File ontologyFile;
    private FileTypeConstant fileType;

    private PreferenceProfile preferenceProfile = new PreferenceProfile();

    private OWLServiceContext owlServiceContext = new OWLServiceContext();
    private KRSSServiceContext krssServiceContext = new KRSSServiceContext();

    private ValidationService validationService = new ValidationService(owlServiceContext, krssServiceContext);
    private SimilarityService similarityService = new SimilarityService(owlServiceContext, krssServiceContext, preferenceProfile);

    public SimExplainer(String directoryPath) {
        Path onto_dir = Paths.get(directoryPath);

        // ontology path
        try (Stream<Path> stream = Files.walk(onto_dir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        String fileAbsPath = file.toAbsolutePath().toString();

                        if (fileName.endsWith(".krss") || fileName.endsWith(".owl")) {
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

                        if (fileName.endsWith(".krss") || fileName.endsWith(".owl")) {
                            load_ontology(fileAbsPath);
                        }
                    });
        } catch (IOException e) {
            throw new JSimPiException("File not found", ErrorCode.Application_InvalidPath);
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
        this.ontologyFile = new File(ontologyPath);

        this.fileType = ValidationService.checkOWLandKRSSFile(this.ontologyFile);

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

    public String simMeasurementResult(TypeConstant optionVal, String concept1, String concept2) {
        if (optionVal == null) {
            throw new JSimPiException("Option not provide", ErrorCode.Application_IllegalArguments);
        }
        if (concept1 == null || concept2 == null) {
            throw new JSimPiException("Concept not provide", ErrorCode.Application_IllegalArguments);
        }

        return similarityService.measureConceptWithType(concept1, concept2, optionVal, this.fileType).toString();

//        String explanation = ExplanationService.explanation.toString();
//        return explanation;
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
                break;

            default:
                throw new JSimPiException("File type not supported", ErrorCode.Application_InvalidFileType);
        }

        return conceptNames;
    }
}
