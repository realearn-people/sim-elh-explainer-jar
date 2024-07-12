package sim.explainer.library.framework;

import org.springframework.stereotype.Component;
import sim.explainer.library.framework.descriptiontree.Tree;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * runchana:2023-31-07
 * The BackTraceTable class stores every computation, a set of primitive concepts, and a set of existential.
 * The key of the map is a mapping of index (0, 1, 2, and so on) with a pair of concept names (concept 1 and 2).
 * The value of the map is a hashmap of a concept name,
 * its primitive concepts and existential, along with homomorphism degree.
 */
@Component
public class BackTraceTable {
    private static Integer index = 0;

    private Map<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>> backTraceTable = new LinkedHashMap<>();

    private String[] cnPair = new String[2];

    /**
     * runchana:2023-31-07
     * Inert values to the innermost list of tree nodes and map it with the outermost map (index, a pair of concept names)
     * @param node A tree of the specified concept name
     * @param values A computed similarity degree of that specified concept name and its pair
     * @param order A number to indicate which concept name it is, 1 or 2.
     */
    public void inputTreeNodeValue(Tree<Set<String>> node, BigDecimal values, int order) {

        Map<Tree<Set<String>>, BigDecimal> treeNodeMap = new HashMap<>();
        Map<String, Map<Tree<Set<String>>, BigDecimal>> innerMap = new HashMap<>();

        treeNodeMap.put(node, values);

        if (order == 1){
            innerMap.put(cnPair[0], treeNodeMap);
        } else {
            innerMap.put(cnPair[1], treeNodeMap);
        }

        Map<Integer, String[]> outermostMap = setKeyMap(index, cnPair);

        if (!backTraceTable.containsKey(outermostMap)) {
            backTraceTable.put(outermostMap, innerMap);
            index++;
        }
    }

    /**
     * runchana:2023-31-07
     * Input a key for an outermsot map by using concept name 1 and 2.
     * @param concept1 concept name 1
     * @param concept2 concept name 2
     */
    public void inputConceptName(String concept1, String concept2) {
        cnPair[0] = concept1;
        cnPair[1] = concept2;
    }

    /**
     * runchana:2023-31-07
     * Set a key of the entire mapping with mapping of index and a pair concept names.
     * @param index integer number that keeps increasing in each time the method is called
     * @param cnPair a pair of concept names in string[]
     * @return outermost map (for a key)
     */
    public Map<Integer, String[]> setKeyMap(Integer index, String[] cnPair) {
        Map<Integer, String[]> outermostMap = new HashMap<>();
        outermostMap.put(index, cnPair);
        return outermostMap;
    }

    public Map<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>> getBackTraceTable() {
        return backTraceTable;
    }

    public String[] getCnPair() {
        return cnPair;
    }
}
