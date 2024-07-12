package sim.explainer.library.util.syntaxanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HandlerContextImpl implements IChainOfResponsibilityContext {

    private String conceptDescription;

    private String topLevelDescription;

    private Set<String> primitiveConceptSet = new HashSet<String>();
    private Map<String, Set<String>> edgePrimitiveConceptExistentialMap = new HashMap<String, Set<String>>();

    public Set<String> addToPrimitiveConceptSet(String concept) {
        this.primitiveConceptSet.add(concept);
        return this.primitiveConceptSet;
    }

    public Map<String, Set<String>> addToEdgePrimitiveConceptExistentialMap(String role, String concept) {
        Set<String> concepts = this.edgePrimitiveConceptExistentialMap.get(role);

        if (concepts == null) {
            concepts = new HashSet<String>();
        }

        concepts.add(concept);
        this.edgePrimitiveConceptExistentialMap.put(role, concepts);

        return this.edgePrimitiveConceptExistentialMap;
    }

    public void clear() {
        this.conceptDescription = null;
        this.topLevelDescription = null;
        this.primitiveConceptSet.clear();
        this.edgePrimitiveConceptExistentialMap.clear();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters and Setters /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getTopLevelDescription() {
        return topLevelDescription;
    }

    public void setTopLevelDescription(String topLevelDescription) {
        this.topLevelDescription = topLevelDescription;
    }

    public Set<String> getPrimitiveConceptSet() {
        return primitiveConceptSet;
    }

    public Map<String, Set<String>> getEdgePrimitiveConceptExistentialMap() {
        return edgePrimitiveConceptExistentialMap;
    }

    public String getConceptDescription() {
        return conceptDescription;
    }

    public void setConceptDescription(String conceptDescription) {
        this.conceptDescription = conceptDescription;
    }
}
