package sim.explainer.library.framework.descriptiontree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DepthFirstTreeIterator<T> implements Iterator<TreeNode<T>>{

    private List<TreeNode<T>> list;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void buildList(Map<Integer, TreeNode<T>> tree, int nodeId) {
        list.add(tree.get(nodeId));

        for (TreeNode<T> child : tree.get(nodeId).getChildren()) {
            this.buildList(tree, child.getId());
        }
    }

    public DepthFirstTreeIterator(Map<Integer, TreeNode<T>> tree, int nodeId) {
        this.list = new LinkedList<TreeNode<T>>();

        if (tree.containsKey(nodeId)) {
            this.buildList(tree, nodeId);
        }
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public TreeNode next() {
        return null;
    }
}
