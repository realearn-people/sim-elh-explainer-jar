package sim.explainer.library.service;

import sim.explainer.library.enumeration.ReasoningDirectionConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.explainer.SimRecord;

import java.util.HashMap;
import java.util.Set;

public class ExplanationService {
    private BacktraceTable forwardBacktraceTable;
    private BacktraceTable backwardBacktraceTable;

    public ExplanationService(BacktraceTable forwardBacktraceTable, BacktraceTable backwardBacktraceTable) {
        this.forwardBacktraceTable = forwardBacktraceTable;
        this.backwardBacktraceTable = backwardBacktraceTable;
    }

    public String treeHierarchy(String concept) {
        TreeNode<Set<String>> root = null;

        for (HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>> levelMap : forwardBacktraceTable.getTable().values()) {
            for (TreeNode<Set<String>> treeNode : levelMap.keySet()) {
                if (treeNode.getConceptName().equals(concept)) {
                    root = treeNode;
                    break;
                }
            }
            if (root != null) {
                break;
            }
        }

        for (HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>> levelMap : backwardBacktraceTable.getTable().values()) {
            for (TreeNode<Set<String>> treeNode : levelMap.keySet()) {
                if (treeNode.getConceptName().equals(concept)) {
                    root = treeNode;
                    break;
                }
            }
            if (root != null) {
                break;
            }
        }

        if (root == null) {
            throw new JSimPiException("Tree not found", ErrorCode.Application_IllegalArguments);
        }

        StringBuilder result = new StringBuilder();
        buildTreeAscii(root, result, "", true);

        return result.toString();
    }

    private void buildTreeAscii(TreeNode<Set<String>> node, StringBuilder result, String prefix, boolean isTail) {
        result.append(prefix).append(isTail ? "└── " : "├── ")
                .append(node.getEdgeToParent() == null ? node.getConceptName() : node.getEdgeToParent()) // root concept
                .append(" : ")
                .append(node.getConceptDescription())
                .append("\n");
        for (int i = 0; i < node.getChildren().size() - 1; i++) {
            buildTreeAscii(node.getChildren().get(i), result, prefix + (isTail ? "    " : "│   "), false);
        }
        if (node.getChildren().size() > 0) {
            buildTreeAscii(node.getChildren().get(node.getChildren().size() - 1), result, prefix + (isTail ? "    " : "│   "), true);
        }
    }

    public String explanationTree(ReasoningDirectionConstant direction) {
        BacktraceTable backtraceTable;
        if (direction.equals(ReasoningDirectionConstant.FORWARD)) {
            backtraceTable = forwardBacktraceTable;
        } else {
            backtraceTable = backwardBacktraceTable;
        }

        StringBuilder result = new StringBuilder();

        // Find all root nodes at level 0
        HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>> levelMap = backtraceTable.getTable().get(0);
        if (levelMap == null) {
            System.out.println("No data available at level 0.");
            return "No data available at level 0.";
        }

        for (TreeNode<Set<String>> root : levelMap.keySet()) {
            buildExplanationTreeAscii(backtraceTable, root, result, "", true, 0);
        }

        return result.toString();
    }

    private void buildExplanationTreeAscii(BacktraceTable backtraceTable, TreeNode<Set<String>> node, StringBuilder result, String prefix, boolean isTail, int level) {
        if (!backtraceTable.getTable().containsKey(level)) {
            return;
        }

        TreeNode<Set<String>> comparing_node = backtraceTable.getTable().get(level).get(node).keySet().iterator().next();

        result.append(prefix).append(isTail ? "└── " : "├── ")
                .append("[")
                .append(node.getConceptName())
                .append("] : [")
                .append(comparing_node.getConceptName())
                .append("] - ")
                .append(backtraceTable.getTable().get(level).get(node).get(comparing_node))
                .append("\n");
        for (int i = 0; i < node.getChildren().size() - 1; i++) {
            buildExplanationTreeAscii(backtraceTable, node.getChildren().get(i), result, prefix + (isTail ? "    " : "│   "), false, level + 1);
        }
        if (node.getChildren().size() > 0) {
            buildExplanationTreeAscii(backtraceTable, node.getChildren().get(node.getChildren().size() - 1), result, prefix + (isTail ? "    " : "│   "), true, level + 1);
        }
    }
}
