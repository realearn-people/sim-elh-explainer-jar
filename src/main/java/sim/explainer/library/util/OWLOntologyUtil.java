package sim.explainer.library.util;

import org.apache.commons.lang3.StringUtils;
import sim.explainer.library.enumeration.OWLDocumentFormat;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import java.util.Set;

public class OWLOntologyUtil {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static OWLObjectRenderer getObjectRenderer(OWLDocumentFormat owlDocumentFormat) {
        if (owlDocumentFormat == null) {
            throw new JSimPiException("Unable to get object renderer as owlDocumentFormat is null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        OWLObjectRenderer renderer = null;

        switch (owlDocumentFormat) {
            case MANCHESTER_SYNTAX_DOCUMENT:

                renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
                break;

            default:

                renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
                break;
        }

        return renderer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean containClassName(OWLOntology owlOntology, OWLClass owlClass) {
        if (owlOntology == null || owlClass == null) {
            throw new JSimPiException("Unable to contain class name as owlOntology[" + owlOntology + "] and owlClass[" + owlClass + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        return owlOntology.containsClassInSignature(owlClass.getIRI());
    }

    public static boolean containObjectPropertyName(OWLOntology owlOntology, OWLObjectProperty owlObjectProperty) {
        if (owlOntology == null || owlObjectProperty == null) {
            throw new JSimPiException("Unable to contain object property object property name as owlOntology[" + owlOntology
                    + "] and owlObjectProperty[" + owlObjectProperty + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        return owlOntology.containsObjectPropertyInSignature(owlObjectProperty.getIRI());
    }

    public static OWLOntology convertAsOWLDocumentFormat(OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, OWLDocumentFormat owlDocumentFormat, Boolean... abbreviated) {
        if (owlOntologyManager == null || owlOntology == null || owlDocumentFormat == null) {
            throw new JSimPiException("Unable to convert as owl document format as owlOntologyManager[" +
                    owlOntologyManager +"], owlOntology[" + owlOntology + "], and owlDocumentFormat[" +
                    owlDocumentFormat + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        boolean printedFullIRI = true;
        if (abbreviated.length != 0 && abbreviated[0] == Boolean.FALSE) {
            printedFullIRI = false;
        }

        // teeradaj@20150310: http://www.programcreek.com/java-api-examples/index.php?api=org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat
        //
        // Some ontology formats support prefix names amd prefix IRIs. Hence, when we save the ontology
        // in the new format, we have to copy the prefixes over so that we have nicely abbreviated IRIs
        // in the new ontology document.
        OWLOntologyFormat owlOntologyFormat = owlOntologyManager.getOntologyFormat(owlOntology);

        switch (owlDocumentFormat) {
            case MANCHESTER_SYNTAX_DOCUMENT:

                ManchesterOWLSyntaxOntologyFormat manchesterOWLSyntaxOntologyFormat = new ManchesterOWLSyntaxOntologyFormat();
                if(printedFullIRI && owlOntologyFormat.isPrefixOWLOntologyFormat()) {
                    manchesterOWLSyntaxOntologyFormat.copyPrefixesFrom(owlOntologyFormat.asPrefixOWLOntologyFormat());
                }

                owlOntologyManager.setOntologyFormat(owlOntology, manchesterOWLSyntaxOntologyFormat);
                break;

            default:
                break;
        }

        return owlOntology;
    }

    public static String getDescriptionLogicName(OWLOntologyManager owlOntologyManager, OWLOntology owlOntology) {
        if (owlOntologyManager == null || owlOntology == null) {
            throw new JSimPiException("Unable to get description logic name as owlOntologyManager[" +
                    owlOntologyManager + "] and owlOntology[" + owlOntology + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        Set<OWLOntology> importClosure = owlOntologyManager.getImportsClosure(owlOntology);
        DLExpressivityChecker dlExpressivityChecker = new DLExpressivityChecker(importClosure);

        return dlExpressivityChecker.getDescriptionLogicName();
    }

    public static String getFullConceptDefinition(OWLOntology owlOntology, OWLClass owlClass, OWLDocumentFormat owlDocumentFormat) {
        if (owlOntology == null || owlClass == null || owlDocumentFormat == null) {
            throw new JSimPiException("Unable to get full definition as owlOntology[" +
                    owlOntology + "], owlClass[" + owlClass + "]. and owlDocumentFormat[" +
                    owlDocumentFormat + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        OWLObjectRenderer renderer = getObjectRenderer(owlDocumentFormat);

        Set<OWLClassExpression> equivalentClasses = owlClass.getEquivalentClasses(owlOntology);
        if (equivalentClasses.size() > 1) {
            throw new JSimPiException(owlClass.toString() + " has more than one definition.", ErrorCode.OWLOntologyUtil_NotUniqueDefinition);
        }

        String definition = null;

        for (OWLClassExpression classExpression : equivalentClasses) {
            definition = StringUtils.replacePattern(renderer.render(classExpression), "\\s+", StringUtils.SPACE);
        }

        return definition;
    }

    public static String getPrimitiveConceptDefinition(OWLOntology owlOntology, OWLClass owlClass, OWLDocumentFormat owlDocumentFormat) {
        if (owlOntology == null || owlClass == null || owlDocumentFormat == null) {
            throw new JSimPiException("Unable to get primitive definition as owlOntology[" +
                    owlOntology + "], owlClass[" + owlClass + "], and owlDocumentFormat[" +
                    owlDocumentFormat + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        OWLObjectRenderer renderer = getObjectRenderer(owlDocumentFormat);

        Set<OWLClassExpression> subsumeClasses = owlClass.getSuperClasses(owlOntology);

        if (subsumeClasses.size() > 1) {
            throw new JSimPiException(owlClass.toString() + " has more than one definition.", ErrorCode.OWLOntologyUtil_NotUniqueDefinition);
        }

        String definition = null;

        for (OWLClassExpression classExpression : subsumeClasses) {
            definition = StringUtils.replacePattern(renderer.render(classExpression), "\\s+", StringUtils.SPACE);
        }

        return definition;
    }

    public static OWLClass getOWLClass(OWLDataFactory owlDataFactory, OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, String abbreviatedConceptName) {
        if (owlDataFactory == null || owlOntologyManager == null || owlOntology == null || abbreviatedConceptName == null) {
            throw new JSimPiException("Unable to get owl class as owlDataFactory[" +
                    owlDataFactory + "], owlOntologyManager[" + owlOntologyManager + "], and owlOntology[" +
                    owlOntology + "], and abbreviatedConceptName[" + abbreviatedConceptName +
                    "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        OWLOntologyFormat owlOntologyFormat = owlOntologyManager.getOntologyFormat(owlOntology);

        return owlDataFactory.getOWLClass(abbreviatedConceptName, owlOntologyFormat.asPrefixOWLOntologyFormat());
    }

    public static OWLObjectProperty getOWLObjectProperty(OWLDataFactory owlDataFactory, OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, String roleName) {
        if (owlDataFactory == null || owlOntologyManager == null || owlOntology == null || roleName == null) {
            throw new JSimPiException("Unable to get owl object property as owlDataFactory[" + owlDataFactory + "], owlOntologyManager[" +
                    owlOntologyManager + "], owlOntology[" + owlOntology + "], and roleName[" + roleName + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        OWLOntologyFormat owlOntologyFormat = owlOntologyManager.getOntologyFormat(owlOntology);

        return owlDataFactory.getOWLObjectProperty(roleName, owlOntologyFormat.asPrefixOWLOntologyFormat());
    }

    public static boolean isValidFreshConceptName(OWLDataFactory owlDataFactory, OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, String freshConceptName) {
        if (owlDataFactory == null || owlOntologyManager == null || owlOntology == null || freshConceptName == null) {
            throw new JSimPiException("Unable to is valid fresh concept name as owlDataFactory[" + owlDataFactory + "], owlOntologyManager[" +
                    owlOntologyManager + "], owlOntology[" + owlOntology + "], and freshConceptName[" + freshConceptName + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        int lastSingleQuote = StringUtils.lastIndexOf(freshConceptName, '\'');
        if (lastSingleQuote != freshConceptName.length() - 1) {
            return false;
        }

        else {
            String concept = StringUtils.chop(freshConceptName);
            OWLClass owlClass = getOWLClass(owlDataFactory, owlOntologyManager, owlOntology, concept);

            if (containClassName(owlOntology, owlClass)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidFreshRoleName(OWLDataFactory owlDataFactory, OWLOntologyManager owlOntologyManager, OWLOntology owlOntology, String freshRoleName) {
        if (owlDataFactory == null || owlOntologyManager == null || owlOntology == null || freshRoleName == null) {
            throw new JSimPiException("Unable to is valid fresh role name as owlDataFactory[" + owlDataFactory + "], owlOntologyManager[" +
                    owlOntologyManager + "], owlOntology[" + owlOntology + "], and freshRoleName[" + freshRoleName + "] are null.", ErrorCode.OWLOntologyUtil_IllegalArguments);
        }

        int lastSingleQuote = StringUtils.lastIndexOf(freshRoleName, '\'');
        if (lastSingleQuote != freshRoleName.length() - 1) {
            return false;
        }

        else {
            String property = StringUtils.chop(freshRoleName);
            OWLObjectProperty owlObjectProperty = getOWLObjectProperty(owlDataFactory, owlOntologyManager, owlOntology, property);

            if (containObjectPropertyName(owlOntology, owlObjectProperty)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The purpose of this method is to help debugging.
     *
     * @param owlOntologyManager
     * @param owlOntology
     */
    public static void printOutOWLOntologyViaSystemOut(OWLOntologyManager owlOntologyManager, OWLOntology owlOntology) {
        if (owlOntologyManager == null || owlOntology == null) {
            return;
        }

        try {
            owlOntologyManager.saveOntology(owlOntology, new SystemOutDocumentTarget());
        }

        catch (OWLOntologyStorageException e) {
            throw new JSimPiException("Unable to print out OWL ontology [" + owlOntology.toString() + "].", e
                    , ErrorCode.OWLOntologyUtil_OWLOntologyStorageException);
        }
    }
}
