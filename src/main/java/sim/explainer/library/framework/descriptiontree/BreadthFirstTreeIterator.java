package sim.explainer.library.framework.descriptiontree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BreadthFirstTreeIterator<T> implements Iterator<TreeNode<T>> {

    private static final Logger logger = LoggerFactory.getLogger(BreadthFirstTreeIterator.class);

    private static final int ROOT = 0;

    private LinkedList<TreeNode<T>> list;
    private Map<Integer, List<TreeNode<T>>> nodesOnEachLevel;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void buildList(Map<Integer, TreeNode<T>> tree, int nodeId, int level) {
        if (level == ROOT) {
            List<TreeNode<T>> rootNode = new ArrayList<TreeNode<T>>();
            rootNode.add(tree.get(ROOT));
            nodesOnEachLevel.put(ROOT, rootNode);
            level = level + 1;
        }

        for (TreeNode<T> child : tree.get(nodeId).getChildren()) {
            if (!nodesOnEachLevel.containsKey(level)) {
                nodesOnEachLevel.put(level, new ArrayList<TreeNode<T>>());
            }

            nodesOnEachLevel.get(level).add(child);

            if (logger.isDebugEnabled()) {
                logger.debug(child.toString());
            }

            this.buildList(tree, child.getId(), level + 1);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public BreadthFirstTreeIterator(Map<Integer, TreeNode<T>> tree, int nodeId) {
        this.list = new LinkedList<TreeNode<T>>();
        nodesOnEachLevel = new HashMap<Integer, List<TreeNode<T>>>();

        if (tree.containsKey(nodeId)) {
            this.buildList(tree, nodeId, ROOT);

            for (Map.Entry<Integer, List<TreeNode<T>>> entry : nodesOnEachLevel.entrySet()) {
                for (TreeNode<T> child : entry.getValue()) {
                    list.add(tree.get(child.getId()));
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !list.isEmpty();
    }

    @Override
    public TreeNode next() {
        return list.poll();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getter //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<Integer, List<TreeNode<T>>> getNodesOnEachLevel() {
        return nodesOnEachLevel;
    }
}
