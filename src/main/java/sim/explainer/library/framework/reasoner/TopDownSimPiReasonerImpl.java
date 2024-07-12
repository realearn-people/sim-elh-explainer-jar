package sim.explainer.library.framework.reasoner;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;
import sim.explainer.library.service.PreferenceProfile;
import sim.explainer.library.util.TimeUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Component("topDownSimPiReasonerImpl")
public class TopDownSimPiReasonerImpl implements IReasoner {

    private static final Logger logger = LoggerFactory.getLogger(TopDownSimPiReasonerImpl.class);

    protected PreferenceProfile preferenceProfile;

    @Resource(name="superRoleUnfolderManchesterSyntax")
    private IRoleUnfolder iRoleUnfolder;

    private List<DateTime> markedTime = new ArrayList<DateTime>();

    public TopDownSimPiReasonerImpl(PreferenceProfile preferenceProfile) {
        this.preferenceProfile = preferenceProfile;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private BigDecimal measureDirectedSimilarity(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to measure directed similarity as node1[" +
                    node1 + "] and node2[" + node2 + "] are null." , ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        BigDecimal muPi = muPi(node1);

        BigDecimal primitiveOperations = muPi.multiply(phdPi(node1, node2));

        BigDecimal edgeOperations = BigDecimal.ONE.subtract(muPi).multiply(eSetHdPi(node1, node2));

        return primitiveOperations.add(edgeOperations);
    }

    private BigDecimal sumPrimitiveConceptImportance(TreeNode<Set<String>> node) {
        Map<String, BigDecimal> primitiveConceptImportance = preferenceProfile.getPrimitiveConceptImportance();

        Set<String> primitives = node.getData();
        BigDecimal sumOfPrimitives = BigDecimal.ZERO;
        for (String primitive : primitives) {

            BigDecimal weight = primitiveConceptImportance.get(primitive);
            if (weight == null) {
                weight = BigDecimal.ONE;
            }
            sumOfPrimitives = sumOfPrimitives.add(weight);
        }
        return sumOfPrimitives;
    }

    private BigDecimal sumRoleImportance(TreeNode<Set<String>> node) {
        Map<String, BigDecimal> roleImportance = preferenceProfile.getRoleImportance();

        List<TreeNode<Set<String>>> edges = node.getChildren();
        BigDecimal sumOfEdges = BigDecimal.ZERO;
        for (TreeNode<Set<String>> edge : edges) {

            String role = edge.getEdgeToParent();
            BigDecimal weight = roleImportance.get(role);
            if (weight == null) {
                weight = BigDecimal.ONE;
            }
            sumOfEdges = sumOfEdges.add(weight);
        }
        return sumOfEdges;
    }

    private BigDecimal sumRoleImportance(Set<String> roles) {

        BigDecimal sum = BigDecimal.ZERO;

        for (String role : roles) {

            BigDecimal weight = preferenceProfile.getRoleImportance().get(role);
            if (weight == null) {
                weight = BigDecimal.ONE;
            }

            sum = sum.add(weight);
        }

        return sum;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected BigDecimal muPi(TreeNode<Set<String>> node) {
        if (node == null) {
            throw new JSimPiException("Unable to mu pi as node is null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        if (node.getData().isEmpty() && node.getChildren().isEmpty()) {
            return BigDecimal.ONE;
        }

        BigDecimal sumOfPrimitives = sumPrimitiveConceptImportance(node);

        if (logger.isDebugEnabled()) {
            logger.debug("muPi - sumOfPrimitives: " + sumOfPrimitives);
        }

        BigDecimal sumOfEdges = sumRoleImportance(node);

        if (logger.isDebugEnabled()) {
            logger.debug("muPi - sumOfEdges: " + sumOfEdges);
        }

        BigDecimal divisor = sumOfPrimitives.add(sumOfEdges);

        return sumOfPrimitives.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
    }

    protected BigDecimal phdPi(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to phd pi as node1[" + node1 + "] and node2[" + node2 + "] are null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        if (sumPrimitiveConceptImportance(node1).equals(BigDecimal.ZERO)) {
            return BigDecimal.ONE;
        }

        else if (sumPrimitiveConceptImportance(node2).equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        else {
            Map<String, Map<String, BigDecimal>> primitiveConceptsSimilarity = preferenceProfile.getPrimitiveConceptsSimilarity();
            Map<String, BigDecimal> primitiveConceptImportance = preferenceProfile.getPrimitiveConceptImportance();

            BigDecimal sumWeightedSimilarity = BigDecimal.ZERO;

            Set<String> primitivesNode1 = node1.getData();
            for (String primitiveNode1 : primitivesNode1) {

                BigDecimal max = BigDecimal.ZERO;

                Set<String> primitivesNode2 = node2.getData();
                for (String primitiveNode2 : primitivesNode2) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("phd pi : primitiveNode1[" + primitiveNode1 + "] and primitiveNode2[" + primitiveNode2 + "]");
                    }

                    BigDecimal val = BigDecimal.ZERO;
                    Map<String, BigDecimal> mapNode1 = primitiveConceptsSimilarity.get(primitiveNode1);

                    if (primitiveNode1.equals(primitiveNode2)) {
                        val = BigDecimal.ONE;
                    }

                    else if (mapNode1 == null) {
                        val = BigDecimal.ZERO;
                    }

                    else if (mapNode1.containsKey(primitiveNode2)) {
                        val = mapNode1.get(primitiveNode2);
                    }

                    if (val.compareTo(max) > 0) {
                        max = val;
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("phd pi(primitiveNode1[" + primitiveNode1 + "], primitiveNode2) = " + max);
                }

                BigDecimal importance = primitiveConceptImportance.get(primitiveNode1);
                if (importance == null) {
                    importance = BigDecimal.ONE;
                }

                BigDecimal weightedSimilarity = max.multiply(importance);
                sumWeightedSimilarity = sumWeightedSimilarity.add(weightedSimilarity);
            }

            BigDecimal divisor = sumPrimitiveConceptImportance(node1);

            if (logger.isDebugEnabled()) {
                logger.debug("phd pi: sumWeightedSimilarity[" + sumWeightedSimilarity + "] divisor[" + divisor + "]");
            }

            return sumWeightedSimilarity.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
        }
    }

    protected BigDecimal eSetHdPi(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to e set hd pi as node1[" + node1 + "] and node2[" + node2 + "] are null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        if (sumRoleImportance(node1).equals(BigDecimal.ZERO)) {
            return BigDecimal.ONE;
        }

        else if (sumRoleImportance(node2).equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        else {

            BigDecimal sum = BigDecimal.ZERO;

            List<TreeNode<Set<String>>> node1Children = node1.getChildren();
            List<TreeNode<Set<String>>> node2Children = node2.getChildren();

            for (TreeNode<Set<String>> node1Child : node1Children) {

                BigDecimal max = BigDecimal.ZERO;

                for (TreeNode<Set<String>> node2Child : node2Children) {

                    BigDecimal ehdPiValue = eHdPi(node1Child, node2Child);

                    if (max.compareTo(ehdPiValue) < 0) {
                        max = ehdPiValue;
                    }
                }

                BigDecimal roleImportance = preferenceProfile.getRoleImportance().get(node1Child.getEdgeToParent());
                if (roleImportance == null) {
                    roleImportance = BigDecimal.ONE;
                }

                BigDecimal weightedRoleVal = roleImportance.multiply(max);

                sum = sum.add(weightedRoleVal);
            }

            BigDecimal divisor = sumRoleImportance(node1);

            return sum.divide(divisor, 5, BigDecimal.ROUND_UP);
        }
    }

    protected BigDecimal eHdPi(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to e hd pi as node1[" + node1 + "] and node2[" + node2 + "] are null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        BigDecimal gammaPiVal = gammaPi(node1.getEdgeToParent(), node2.getEdgeToParent());

        BigDecimal discountFactor = preferenceProfile.getRoleDiscountFactor().get(node1.getEdgeToParent());
        if (discountFactor == null) {
            discountFactor = new BigDecimal("0.4");
        }

        BigDecimal nuPrime = BigDecimal.ONE.subtract(discountFactor);
        BigDecimal simSubTree = measureDirectedSimilarity(node1, node2);

        return nuPrime.multiply(simSubTree).add(discountFactor).multiply(gammaPiVal);
    }

    protected BigDecimal gammaPi(String edge1, String edge2) {
        if (edge1 == null || edge2 == null) {
            throw new JSimPiException("Unable to gamma pi as edge1[" + edge1 + "] and edge2[" + edge2 + "] are null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        Set<String> edgeSet1 = iRoleUnfolder.unfoldRoleHierarchy(edge1);
        Set<String> edgeSet2 = iRoleUnfolder.unfoldRoleHierarchy(edge2);

        if (sumRoleImportance(edgeSet1).equals(BigDecimal.ZERO)) {
            return BigDecimal.ONE;
        }

        else {

            BigDecimal sum = BigDecimal.ZERO;
            Map<String, Map<String, BigDecimal>> rolesSimilarity = preferenceProfile.getPrimitiveRolesSimilarity();
            Map<String, BigDecimal> roleImportance = preferenceProfile.getRoleImportance();

            for (String role1 : edgeSet1) {

                BigDecimal max = BigDecimal.ZERO;

                for (String role2 : edgeSet2) {
                    BigDecimal val = BigDecimal.ZERO;
                    Map<String, BigDecimal> mapNode1 = rolesSimilarity.get(role1);

                    if (role1.equals(role2)) {
                        val = BigDecimal.ONE;
                    }

                    else if (mapNode1 == null) {
                        val = BigDecimal.ZERO;
                    }

                    else if (mapNode1.containsKey(role2)) {
                        val = mapNode1.get(role2);
                    }

                    if (val.compareTo(max) > 0) {
                        max = val;
                    }
                }

                BigDecimal importance = roleImportance.get(role1);
                if (importance == null) {
                    importance = BigDecimal.ONE;
                }

                BigDecimal weightedSimVal = importance.multiply(max);

                sum = sum.add(weightedSimVal);
            }

            BigDecimal divisor = sumRoleImportance(edgeSet1);

            return sum.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        if (tree1 == null || tree2 == null || preferenceProfile == null) {
            throw new JSimPiException("Unable to measure directed similarity as tree1["
                    + tree1 + "] and tree2[" + tree2 + " are null.", ErrorCode.TopDownSimPiReasonerImpl_IllegalArguments);
        }

        TreeNode<Set<String>> rootTree1 = tree1.getNodes().get(0);
        TreeNode<Set<String>> rootTree2 = tree2.getNodes().get(0);

        markedTime.clear();

        markedTime.add(DateTime.now());
        BigDecimal value = measureDirectedSimilarity(rootTree1, rootTree2);
        markedTime.add(DateTime.now());

        return value;
    }

    @Override
    public void setRoleUnfoldingStrategy(IRoleUnfolder iRoleUnfolder) {
        this.iRoleUnfolder = iRoleUnfolder;
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
