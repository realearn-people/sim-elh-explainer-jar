package sim.explainer.library.framework.explainer;

import sim.explainer.library.framework.descriptiontree.TreeNode;
import java.util.HashMap;
import java.util.Set;

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

            TreeNode<Set<String>> current_tree = (TreeNode<Set<String>>) tmp_source.keySet().toArray()[0];

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
}
