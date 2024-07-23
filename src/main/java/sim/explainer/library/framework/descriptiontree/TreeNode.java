package sim.explainer.library.framework.descriptiontree;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreeNode<T> {

    private T data;
    private List<TreeNode<T>> children = new LinkedList<TreeNode<T>>();
    private int id;
    private String edgeToParent;
    private String conceptName;

    public TreeNode(String conceptName, String edgeToParent, T data, int id) {
        this.edgeToParent = edgeToParent;
        this.data = data;
        this.id = id;
        this.conceptName = conceptName;
    }

    protected TreeNode<T> addChild(String conceptName, String edgeToParent, T node, int nodeId) {
        TreeNode<T> child = new TreeNode<T>(conceptName, edgeToParent, node, nodeId);
        this.children.add(child);

        return child;
    }

    @Override
    public String toString() {
        if (data instanceof Set) {
            Set<String> stringSet = (Set<String>) data;

            StringBuilder builder = new StringBuilder(StringUtils.SPACE);
            for (String str : stringSet) {
                builder.append(str);
                builder.append(StringUtils.SPACE);
            }

            return StringUtils.strip(builder.toString()).toString();
        }

        else {
            return data.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters and Setters /////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public T getData() {
        return  this.data;
    }

    public List<TreeNode<T>> getChildren() {
        return this.children;
    }

    public int getId() {
        return id;
    }

    public String getEdgeToParent() {
        return edgeToParent;
    }

    public String getConceptName() {
        return conceptName;
    }

    public TreeNode<T> copy() {
        // Create a new TreeNode instance with the same properties but no children yet
        TreeNode<T> newNode = new TreeNode<>(this.conceptName, this.edgeToParent, this.data, this.id);

        // Recursively copy each child and add it to the new node
        for (TreeNode<T> child : this.children) {
            TreeNode<T> childCopy = child.copy(); // Recursive copy
            newNode.children.add(childCopy); // Add the copied child to the new node's children
        }

        return newNode;
    }

    public void setEdgeToParent(String edgeToParent) {
        this.edgeToParent = edgeToParent;
    }
}
