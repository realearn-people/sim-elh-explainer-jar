package sim.explainer.library.service;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.semanticweb.owlapi.model.OWLClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.framework.KRSSServiceContext;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.util.OWLOntologyUtil;

import java.io.File;

@Service
public class ValidationService {
    private OWLServiceContext owlServiceContext;
    private KRSSServiceContext krssServiceContext;

    public ValidationService(OWLServiceContext owlServiceContext, KRSSServiceContext krssServiceContext) {
        this.owlServiceContext = owlServiceContext;
        this.krssServiceContext = krssServiceContext;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // OWL /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean validateIfOWLClassNamesExist(String... conceptNames) {
        if (conceptNames == null) {
            return false;
        }

        else {
            for (String conceptName : conceptNames) {
                OWLClass owlClass = OWLOntologyUtil.getOWLClass(owlServiceContext.getOwlDataFactory(), owlServiceContext.getOwlOntologyManager(), owlServiceContext.getOwlOntology(), conceptName);

                if (!OWLOntologyUtil.containClassName(owlServiceContext.getOwlOntology(), owlClass)) {
                    return false;
                }
            }
        }

        return true;
    }


    public boolean validateIfLatestOWLFile(String owlFilePath) {
        if (owlFilePath == null) {
            return false;
        }

        if (owlServiceContext.getOwlFile() == null) {
            return false;
        }

        LastModifiedFileComparator lastModifiedFileComparator = new LastModifiedFileComparator();
        if (lastModifiedFileComparator.compare(new File(owlFilePath), owlServiceContext.getOwlFile()) > 0) {
            return true;
        }

        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // KRSS ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean validateIfKRSSClassNamesExist(String... conceptNames) {
        if (conceptNames == null) {
            return false;
        } else {
            for (String conceptName : conceptNames) {
                if (!krssServiceContext.getFullConceptDefinitionMap().containsKey(conceptName) &&
                        !krssServiceContext.getPrimitiveConceptDefinitionMap().containsKey(conceptName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateIfLatestKRSSFile(String krssFilePath) {
        if (krssFilePath == null) {
            return false;
        }

        if (krssServiceContext.getKrssFile() == null) { // Assuming there is a method to get the current KRSS file
            return false;
        }

        LastModifiedFileComparator lastModifiedFileComparator = new LastModifiedFileComparator();
        if (lastModifiedFileComparator.compare(new File(krssFilePath), krssServiceContext.getKrssFile()) > 0) {
            return true;
        }

        return false;
    }

    public static FileTypeConstant checkOWLandKRSSFile(File file) {
        if (file.getName().endsWith(".owl") || file.getName().endsWith(".owx")) {
            return FileTypeConstant.OWL_FILE;
        } else if (file.getName().endsWith(".krss")) {
            return FileTypeConstant.KRSS_FILE;
        }
        return FileTypeConstant.INVALID_FILE;
    }

}