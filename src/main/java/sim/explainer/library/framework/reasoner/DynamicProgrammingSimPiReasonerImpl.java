package sim.explainer.library.framework.reasoner;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.descriptiontree.BreadthFirstTreeIterator;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.framework.explainer.SimRecord;
import sim.explainer.library.util.TimeUtils;

import java.math.BigDecimal;
import java.util.*;

@Component("dynamicProgrammingSimPiReasonerImpl")
public class DynamicProgrammingSimPiReasonerImpl extends TopDownSimPiReasonerImpl {

    private Map<Integer, Map<Integer, BigDecimal>> nodePairHdValMap = new HashMap<Integer, Map<Integer, BigDecimal>>();

    private List<DateTime> markedTime = new ArrayList<DateTime>();

    public DynamicProgrammingSimPiReasonerImpl(PreferenceProfile preferenceProfile) {
        super(preferenceProfile);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void addNodePairHdValMap(Integer node1Id, Integer node2Id, BigDecimal hdVal) {
        if (node1Id == null || node2Id == null || hdVal == null) {
            throw new JSimPiException("Unable to add node pair hd val map as node1Id["
                    + node1Id + "], node2Id[" + node2Id + "], and hdVal[" + hdVal + "] are null.", ErrorCode.DynamicProgrammingSimPiReasonerImpl_IllegalArguments);
        }

        Map<Integer, BigDecimal> node2IdHdValMap = nodePairHdValMap.get(node1Id);
        if (node2IdHdValMap == null) {
            node2IdHdValMap = new HashMap<Integer, BigDecimal>();
        }
        node2IdHdValMap.put(node2Id, hdVal);
        this.nodePairHdValMap.put(node1Id, node2IdHdValMap);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected BigDecimal eHdPi(int level, SimRecord record, TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) { // level can be any, its will not affect
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to ehd as node1[" + node1 + "] and node2[" + node2 + "] are null.", ErrorCode.DynamicProgrammingSimPiReasonerImpl_IllegalArguments);
        }

        BigDecimal gammaValue = gammaPi(record, node1.getEdgeToParent(), node2.getEdgeToParent());

        BigDecimal discountFactor = preferenceProfile.getRoleDiscountFactor().get(node1.getEdgeToParent());
        if (discountFactor == null) {
            discountFactor = preferenceProfile.getDefaultRoleDiscountFactor();
        }

        BigDecimal nuPrime = BigDecimal.ONE.subtract(discountFactor);

        BigDecimal simSubTree = nodePairHdValMap.get(node1.getId()).get(node2.getId());
        if (simSubTree == null) {
            simSubTree = BigDecimal.ZERO;
        }

        return nuPrime.multiply(simSubTree).add(discountFactor).multiply(gammaValue);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        if (tree1 == null || tree2 == null) {
            throw new JSimPiException("Unable to measure directed similarity as tree1[" + tree1
                    + "] and tree2[" + tree2 + "] are null.", ErrorCode.DynamicProgrammingSimPiReasonerImpl_IllegalArguments);
        }

        this.backtraceTable = new BacktraceTable();

        markedTime.clear();

        markedTime.add(DateTime.now());
        BreadthFirstTreeIterator<Set<String>> breadthFirstTree1 = (BreadthFirstTreeIterator<Set<String>>) tree1.iterator(0);
        markedTime.add(DateTime.now());

        markedTime.add(DateTime.now());
        BreadthFirstTreeIterator<Set<String>> breadthFirstTree2 = (BreadthFirstTreeIterator<Set<String>>) tree2.iterator(0);
        markedTime.add(DateTime.now());

        int heightTree1 = breadthFirstTree1.getNodesOnEachLevel().size();

        markedTime.add(DateTime.now());
        for (int i = heightTree1 - 1; i >= 0; i--) {
            List<TreeNode<Set<String>>> list1 = breadthFirstTree1.getNodesOnEachLevel().get(i);
            List<TreeNode<Set<String>>> list2 = breadthFirstTree2.getNodesOnEachLevel().get(i);

            for (TreeNode<Set<String>> treeNode1 : list1) {

                for (int j = 0; list2 != null && j < list2.size(); j++) {
                    TreeNode<Set<String>> treeNode2 = list2.get(j);

                    SimRecord record = new SimRecord();

                    BigDecimal phd = phdPi(record, treeNode1, treeNode2);

                    if (i == heightTree1 - 1) {
                        this.addNodePairHdValMap(treeNode1.getId(), treeNode2.getId(), phd);
                        record.setDeg(phd);
                        this.backtraceTable.addRecord(i, treeNode1, treeNode2, record);
                    }

                    else {

                        BigDecimal mu = muPi(treeNode1);
                        BigDecimal eSetHd = eSetHdPi(i, record, treeNode1, treeNode2);
                        BigDecimal primitiveOperations = mu.multiply(phd);
                        BigDecimal edgeOperations = BigDecimal.ONE.subtract(mu).multiply(eSetHd);
                        BigDecimal hdVal = primitiveOperations.add(edgeOperations);
                        this.addNodePairHdValMap(treeNode1.getId(), treeNode2.getId(), hdVal);
                        record.setDeg(hdVal);
                        this.backtraceTable.addRecord(i, treeNode1, treeNode2, record);
                    }
                }
            }
        }
        BigDecimal value = nodePairHdValMap.get(0).get(0);
        markedTime.add(DateTime.now());

        return value;
    }

    @Override
    public List<String> getExecutionTimes() {
        List<String> results = new LinkedList<String>();

        for (int i = 0; i < markedTime.size(); i = i + 2) {
            results.add(TimeUtils.getTotalTimeDifferenceStringInMillis(markedTime.get(i), markedTime.get(i + 1)));
        }

        return results;
    }
}
