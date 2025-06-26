package sim.explainer.library.framework.explainer;

import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.util.utilstructure.SymmetricPair;

import java.util.HashMap;
import java.util.Set;

/**
 * Represents a table used to trace back similarity records across different levels of a tree.
 */
public class BacktraceTable {

    private final HashMap<Integer, HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord>> table = new HashMap<>();

    /**
     * Constructs an empty {@code BacktraceTable}.
     */
    public BacktraceTable() {}

    /**
     * Adds a similarity record to the backtrace table at the specified level.
     *
     * @param level the level in the table
     * @param treeNode1 the first tree node
     * @param treeNode2 the second tree node
     * @param record the similarity record to add
     */
    public void addRecord(int level, TreeNode<Set<String>> treeNode1, TreeNode<Set<String>> treeNode2, SimRecord record) {
        SymmetricPair<TreeNode<Set<String>>> pair = new SymmetricPair<>(treeNode1, treeNode2);
        table.computeIfAbsent(level, k -> new HashMap<>())
                .merge(pair, record, (existingRecord, newRecord) ->
                        existingRecord.getDeg().compareTo(newRecord.getDeg()) <= 0 ? newRecord : existingRecord);
    }

    /**
     * Returns the backtrace table.
     *
     * @return the backtrace table
     */
    public HashMap<Integer, HashMap<SymmetricPair<TreeNode<Set<String>>>, SimRecord>> getTable() {
        return table;
    }
}
