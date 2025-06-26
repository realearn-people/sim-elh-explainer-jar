package sim.explainer.library.util;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sim.explainer.library.enumeration.OWLDocumentFormat;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;

@Component
public class OWLConceptDefinitionUtil {

    private static final Logger logger = LoggerFactory.getLogger(OWLConceptDefinitionUtil.class);

    public static String generateFullConceptDefinitionManchesterSyntax(OWLDataFactory owlDataFactory, OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, String conceptName) {
        if(owlDataFactory == null || owlOntologyManager == null || owlOntology == null || conceptName == null) {
            throw new JSimPiException("Unable to generate full concept definition as owlDataFactory[" +
                    owlDataFactory + "], owlOntologyManager[" + owlOntologyManager + "], owlOntology[" +
                    owlOntology + "], and conceptName[" + conceptName + "] are null.", ErrorCode.OWLConceptDefinitionUtil_IllegalArguments);
        }

        // 1. Get an OwlClass instance.
        OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlDataFactory,owlOntologyManager, owlOntology, conceptName);

        // 2. Get a definition of owlClass instance and validate it.
        String fullDefinition = OWLOntologyUtil.getFullConceptDefinition(owlOntology, owlClass, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);
        String primitiveDefinition = OWLOntologyUtil.getPrimitiveConceptDefinition(owlOntology, owlClass, OWLDocumentFormat.MANCHESTER_SYNTAX_DOCUMENT);

        StringBuilder definitionBuilder = null;

        if (fullDefinition != null && primitiveDefinition != null) {
            throw new JSimPiException("Unable to generate full concept definition due to non-unique definition.", ErrorCode.OWLConceptDefinitionUtil_NotUniqueDefinition);
        }

        else if (fullDefinition == null && primitiveDefinition != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exists only primitive definition: " + primitiveDefinition);
            }

            definitionBuilder = new StringBuilder(ParserUtils.generateFreshName(conceptName));
            definitionBuilder.append(" and ");
            definitionBuilder.append(primitiveDefinition);
        }

        else if (fullDefinition != null && primitiveDefinition == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exists only full definition: " + fullDefinition);
            }

            definitionBuilder = new StringBuilder(fullDefinition);
        }

        return definitionBuilder != null ? definitionBuilder.toString() : null;
    }


}
