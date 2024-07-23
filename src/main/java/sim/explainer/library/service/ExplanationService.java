package sim.explainer.library.service;

import org.json.JSONArray;
import org.springframework.stereotype.Service;
import sim.explainer.library.enumeration.ReasoningDirectionConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.explainer.SimRecord;
import sim.explainer.library.util.utilstructure.SymmetricPair;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * Service class to provide explanation functionalities for similarity measures and reasoning.
 */
@Service
public class ExplanationService {
    private BigDecimal similarity;
    private BacktraceTable forwardBacktraceTable;
    private BacktraceTable backwardBacktraceTable;

    /**
     * Constructs an {@code ExplanationService} with the given similarity and backtrace tables.
     *
     * @param similarity the similarity score between two concepts
     * @param forwardBacktraceTable the forward backtrace table
     * @param backwardBacktraceTable the backward backtrace table
     */
    public ExplanationService(BigDecimal similarity, BacktraceTable forwardBacktraceTable, BacktraceTable backwardBacktraceTable) {
        this.similarity = similarity;
        this.forwardBacktraceTable = forwardBacktraceTable;
        this.backwardBacktraceTable = backwardBacktraceTable;
    }

    /**
     * Generates an ASCII representation of the tree hierarchy for the given concept.
     *
     * @param concept the concept to generate the tree hierarchy for
     * @return the ASCII representation of the tree hierarchy
     * @throws JSimPiException if the tree for the concept is not found
     */
    public String treeHierarchy(String concept) {
        TreeNode<Set<String>> root = findRootNode(concept);

        if (root == null) {
            throw new JSimPiException("Tree not found", ErrorCode.Application_IllegalArguments);
        }

        StringBuilder result = new StringBuilder();
        buildTreeAscii(root, result, "", true);

        return result.toString();
    }

    private void buildTreeAscii(TreeNode<Set<String>> node, StringBuilder result, String prefix, boolean isTail) {
        result.append(prefix).append(isTail ? "└── " : "├── ")
                .append(node.getEdgeToParent() == null ? node.getConceptName() : node.getEdgeToParent())
                .append(" : ")
                .append(node.getData())
                .append("\n");
        for (int i = 0; i < node.getChildren().size() - 1; i++) {
            buildTreeAscii(node.getChildren().get(i), result, prefix + (isTail ? "    " : "│   "), false);
        }
        if (node.getChildren().size() > 0) {
            buildTreeAscii(node.getChildren().get(node.getChildren().size() - 1), result, prefix + (isTail ? "    " : "│   "), true);
        }
    }

    /**
     * Generates a JSON representation of the tree hierarchy for the given concept.
     *
     * @param concept the concept to generate the tree hierarchy for
     * @return the JSON representation of the tree hierarchy
     * @throws JSimPiException if the tree for the concept is not found
     */
    public JSONObject treeHierarchyAsJson(String concept) {
        TreeNode<Set<String>> root = findRootNode(concept);

        if (root == null) {
            throw new JSimPiException("Tree not found", ErrorCode.Application_IllegalArguments);
        }

        root = root.copy();
        root.setEdgeToParent(null);

        return buildTreeHierarchyAsJson(root);
    }

    private JSONObject buildTreeHierarchyAsJson(TreeNode<Set<String>> node) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("roleName", node.getEdgeToParent() == null ? null : node.getEdgeToParent());
        jsonObject.put("conceptName", node.getConceptName());

        JSONArray primitiveConcepts = new JSONArray(node.getData());
        jsonObject.put("primitiveConcepts", primitiveConcepts);

        JSONArray existentials = new JSONArray();
        for (TreeNode<Set<String>> child : node.getChildren()) {
            existentials.put(buildTreeHierarchyAsJson(child));
        }
        jsonObject.put("existentials", existentials);

        return jsonObject;
    }

    /**
     * Generates an ASCII representation of the explanation tree for the given reasoning direction.
     *
     * @param direction the reasoning direction (FORWARD or BACKWARD)
     * @return the ASCII representation of the explanation tree
     */
    public String explanationTree(ReasoningDirectionConstant direction) {
        BacktraceTable backtraceTable = getBacktraceTable(direction);
        StringBuilder result = new StringBuilder();

        HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord> levelMap = backtraceTable.getTable().get(0);
        if (levelMap == null) {
            return "No data available at level 0.";
        }

        for (SymmetricPair<TreeNode<Set<String>>> rootPair : levelMap.keySet()) {
            buildExplanationTreeAscii(backtraceTable, rootPair.getFirst(), result, "", true, 0);
        }

        return result.toString();
    }

    private void buildExplanationTreeAscii(BacktraceTable backtraceTable, TreeNode<Set<String>> node, StringBuilder result, String prefix, boolean isTail, int level) {
        if (!backtraceTable.getTable().containsKey(level)) {
            return;
        }

        SymmetricPair<TreeNode<Set<String>>> pair = backtraceTable.getTable().get(level).keySet().stream()
                .filter(p -> p.getFirst().equals(node) || p.getSecond().equals(node))
                .findFirst().orElse(null);

        if (pair == null) {
            return;
        }

        TreeNode<Set<String>> comparingNode = pair.getFirst().equals(node) ? pair.getSecond() : pair.getFirst();
        SimRecord simRecord = backtraceTable.getTable().get(level).get(pair);

        result.append(prefix).append(isTail ? "└── " : "├── ")
                .append("[")
                .append(node.getConceptName())
                .append("] : [")
                .append(comparingNode.getConceptName())
                .append("] - ")
                .append(simRecord)
                .append("\n");
        for (int i = 0; i < node.getChildren().size() - 1; i++) {
            buildExplanationTreeAscii(backtraceTable, node.getChildren().get(i), result, prefix + (isTail ? "    " : "│   "), false, level + 1);
        }
        if (node.getChildren().size() > 0) {
            buildExplanationTreeAscii(backtraceTable, node.getChildren().get(node.getChildren().size() - 1), result, prefix + (isTail ? "    " : "│   "), true, level + 1);
        }
    }

    /**
     * Generates a natural explanation of the explanation tree for the given reasoning direction in JSON format.
     *
     * @param direction the reasoning direction (FORWARD or BACKWARD)
     * @return the JSON representation of the natural explanation
     */
    public JSONObject explanationTreeNaturalExplanation(ReasoningDirectionConstant direction) {
        BacktraceTable backtraceTable = getBacktraceTable(direction);

        HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord> levelMap = backtraceTable.getTable().get(0);
        if (levelMap == null || levelMap.isEmpty()) {
            return new JSONObject().put("error", "No data available at level 0.");
        }

        TreeNode<Set<String>> root = levelMap.keySet().iterator().next().getFirst();
        return ExplanationConverterService.convertExplanationWholeTree(buildExplanationTreeAsJson(backtraceTable, root, 0));
    }

    /**
     * Generates a JSON representation of the explanation tree for the given reasoning direction.
     *
     * @param direction the reasoning direction (FORWARD or BACKWARD)
     * @return the JSON representation of the explanation tree
     */
    public JSONObject explanationTreeAsJson(ReasoningDirectionConstant direction) {
        BacktraceTable backtraceTable = getBacktraceTable(direction);

        HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord> levelMap = backtraceTable.getTable().get(0);
        if (levelMap == null || levelMap.isEmpty()) {
            return new JSONObject().put("error", "No data available at level 0.");
        }

        TreeNode<Set<String>> root = levelMap.keySet().iterator().next().getFirst();
        return buildExplanationTreeAsJson(backtraceTable, root, 0);
    }

    private JSONObject buildExplanationTreeAsJson(BacktraceTable backtraceTable, TreeNode<Set<String>> node, int level) {
        if (!backtraceTable.getTable().containsKey(level)) {
            return null;
        }

        SymmetricPair<TreeNode<Set<String>>> pair = backtraceTable.getTable().get(level).keySet().stream()
                .filter(p -> p.getFirst().equals(node) || p.getSecond().equals(node))
                .findFirst().orElse(null);

        if (pair == null) {
            return null;
        }

        TreeNode<Set<String>> comparingNode = pair.getFirst().equals(node) ? pair.getSecond() : pair.getFirst();
        SimRecord simRecord = backtraceTable.getTable().get(level).get(pair);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("comparingConcept1", node.getConceptName());
        jsonObject.put("comparingConcept2", comparingNode.getConceptName());
        jsonObject.put("deg", simRecord.getDeg());
        jsonObject.put("pri", new JSONArray(simRecord.getPri().stream().map(SymmetricPair::toString).collect(Collectors.toList())));
        jsonObject.put("exi", new JSONArray(simRecord.getExi().stream().map(SymmetricPair::toString).collect(Collectors.toList())));
        jsonObject.put("emb", new JSONObject(simRecord.getEmb()));

        List<JSONObject> childrenJson = new ArrayList<>();
        for (TreeNode<Set<String>> child : node.getChildren()) {
            JSONObject childJson = buildExplanationTreeAsJson(backtraceTable, child, level + 1);
            if (childJson != null) {
                childrenJson.add(childJson);
            }
        }

        jsonObject.put("children", childrenJson);
        return jsonObject;
    }

    /**
     * Gets the similarity score.
     *
     * @return the similarity score
     */
    public BigDecimal getSimilarity() {
        return similarity;
    }

    /**
     * Gets the backward backtrace table.
     *
     * @return the backward backtrace table
     */
    public BacktraceTable getBackwardBacktraceTable() {
        return backwardBacktraceTable;
    }

    /**
     * Gets the forward backtrace table.
     *
     * @return the forward backtrace table
     */
    public BacktraceTable getForwardBacktraceTable() {
        return forwardBacktraceTable;
    }

    private TreeNode<Set<String>> findRootNode(String concept) {
        TreeNode<Set<String>> root = null;

        for (HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord> levelMap : forwardBacktraceTable.getTable().values()) {
            for (SymmetricPair<TreeNode<Set<String>>> pair : levelMap.keySet()) {
                if (pair.getFirst().getConceptName().equals(concept) || pair.getSecond().getConceptName().equals(concept)) {
                    root = pair.getFirst().getConceptName().equals(concept) ? pair.getFirst() : pair.getSecond();
                    break;
                }
            }
            if (root != null) {
                break;
            }
        }

        if (root == null) {
            for (HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord> levelMap : backwardBacktraceTable.getTable().values()) {
                for (SymmetricPair<TreeNode<Set<String>>> pair : levelMap.keySet()) {
                    if (pair.getFirst().getConceptName().equals(concept) || pair.getSecond().getConceptName().equals(concept)) {
                        root = pair.getFirst().getConceptName().equals(concept) ? pair.getFirst() : pair.getSecond();
                        break;
                    }
                }
                if (root != null) {
                    break;
                }
            }
        }

        return root;
    }

    private BacktraceTable getBacktraceTable(ReasoningDirectionConstant direction) {
        if (direction.equals(ReasoningDirectionConstant.FORWARD)) {
            return forwardBacktraceTable;
        } else {
            return backwardBacktraceTable;
        }
    }
}
