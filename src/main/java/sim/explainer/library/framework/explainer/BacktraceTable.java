package sim.explainer.library.framework.explainer;

import sim.explainer.library.framework.descriptiontree.TreeNode;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class BacktraceTable {

    private HashMap<Integer, HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>>> table = new HashMap<>();

    public BacktraceTable() {}

    public void addRecord(int level, TreeNode<Set<String>> treeNode1, TreeNode<Set<String>> treeNode2, SimRecord record) {
        HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>> tmp_level;
        HashMap<TreeNode<Set<String>>, SimRecord> tmp_source;

        // check there exist level
        if (!table.containsKey(level)) {
            tmp_level = new HashMap<>();
            table.put(level, tmp_level);
        }

        // check there exist recent source treeNode
        if (!table.get(level).containsKey(treeNode1)) {
            tmp_source = new HashMap<>();

            tmp_source.put(treeNode2, record); // put record instantly when it initializes.

            table.get(level).put(treeNode1, tmp_source);
        }

        // there exist record of source node
        else {
            tmp_source = table.get(level).get(treeNode1);

            TreeNode<Set<String>> current_tree = tmp_source.keySet().iterator().next();

            // if recent record have less deg than new record
            if (tmp_source.get(current_tree).getDeg().compareTo(record.getDeg()) <= 0) {
                tmp_source.remove(current_tree);
                tmp_source.put(treeNode2, record);
            }
        }
    }

    public HashMap<Integer, HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>>> getTable() {
        return table;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BacktraceTable {\n");
        for (Map.Entry<Integer, HashMap<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>>> levelEntry : table.entrySet()) {
            sb.append("  Level ").append(levelEntry.getKey()).append(":\n");
            for (Map.Entry<TreeNode<Set<String>>, HashMap<TreeNode<Set<String>>, SimRecord>> sourceEntry : levelEntry.getValue().entrySet()) {
                sb.append("    TreeNode1: ").append(sourceEntry.getKey().toString()).append("\n");
                for (Map.Entry<TreeNode<Set<String>>, SimRecord> targetEntry : sourceEntry.getValue().entrySet()) {
                    sb.append("      TreeNode2: ").append(targetEntry.getKey().toString()).append("\n");
                    sb.append("      SimRecord: ").append(targetEntry.getValue().toString()).append("\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
