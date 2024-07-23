package sim.explainer.library.framework.explainer;

import sim.explainer.library.framework.descriptiontree.TreeNode;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents a table used to trace back similarity records across different levels of a tree.
 */
public class BacktraceTable {

    private final HashMap<Integer, HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>>> table = new HashMap<>();

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
        table.computeIfAbsent(level, k -> new HashMap<>())
                .computeIfAbsent(treeNode1, k -> new HashMap<>())
                .merge(treeNode2, record, (existingRecord, newRecord) ->
                        existingRecord.getDeg().compareTo(newRecord.getDeg()) <= 0 ? newRecord : existingRecord);
    }

    /**
     * Returns the backtrace table.
     *
     * @return the backtrace table
     */
    public HashMap<Integer, HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>>> getTable() {
        return table;
    }
}
