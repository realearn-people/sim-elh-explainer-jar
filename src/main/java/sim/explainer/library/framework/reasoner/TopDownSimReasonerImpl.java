package sim.explainer.library.framework.reasoner;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.text.StrBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;
import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.util.TimeUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Component("topDownSimReasonerImpl")
public class TopDownSimReasonerImpl implements IReasoner {

    private static final Logger logger = LoggerFactory.getLogger(TopDownSimReasonerImpl.class);

    protected PreferenceProfile preferenceProfile;

    @Resource(name="superRoleUnfolderManchesterSyntax")
    private IRoleUnfolder iRoleUnfolder;

    private List<DateTime> markedTime = new ArrayList<DateTime>();

    protected BacktraceTable backtraceTable = new BacktraceTable();

    public TopDownSimReasonerImpl(PreferenceProfile preferenceProfile) {
        this.preferenceProfile = preferenceProfile;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private BigDecimal measureDirectedSimilarity(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to measure directed similarity as node1[" +
                    node1 + "] and node2[" + node2 + "] are null." , ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        BigDecimal mu = mu(node1);

        BigDecimal primitiveOperations = mu.multiply(phd(node1, node2));

        BigDecimal edgeOperations = BigDecimal.ONE.subtract(mu).multiply(eSetHd(node1, node2));

        return primitiveOperations.add(edgeOperations);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected BigDecimal mu(TreeNode<Set<String>> node) {
        if (node == null) {
            throw new JSimPiException("Unable to mu as node[" + node + "] is null.",
                    ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        if (node.getData().isEmpty() && node.getChildren().isEmpty()) {
            return BigDecimal.ONE;
        }

        StringBuilder builder1 = new StringBuilder().append(node.getData().size());
        BigDecimal numberOfPrimitives = new BigDecimal(builder1.toString());

        StringBuilder builder2 = new StringBuilder().append(node.getChildren().size());
        BigDecimal numberOfOutgoingEdges = new BigDecimal(builder2.toString());

        BigDecimal divisor = numberOfPrimitives.add(numberOfOutgoingEdges);

        return numberOfPrimitives.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
    }

    // method to compute average of the best matching of primitive concepts
    protected BigDecimal phd(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to phd as node1[" + node1 + "] and node2[" +
                    node2 + "] are null.", ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        if (node1.getData().isEmpty()) {
            return BigDecimal.ONE;
        }

        else {
            Set<String> common = Sets.intersection(node1.getData(), node2.getData());
            StringBuilder builder1 = new StringBuilder().append(common.size());
            BigDecimal numerator = new BigDecimal(builder1.toString());

            StringBuilder builder2 = new StringBuilder().append(node1.getData().size());
            BigDecimal divisor = new BigDecimal(builder2.toString());

            return numerator.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
        }
    }

    // method to compute degree of potential homomorphism on a matching edge
    protected BigDecimal eSetHd(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to e-set-hd as node1[" + node1 + "] and node2[" +
                    node2 + "] are null.", ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        List<TreeNode<Set<String>>> node1Children = node1.getChildren();
        List<TreeNode<Set<String>>> node2Children = node2.getChildren();

        if (node1Children.isEmpty()) {
            return BigDecimal.ONE;
        }

        else if (node2Children.isEmpty()) {
            return BigDecimal.ZERO;
        }

        else {

            BigDecimal sum = BigDecimal.ZERO;

            for (TreeNode<Set<String>> node1Child : node1Children) {

                BigDecimal max = BigDecimal.ZERO;

                for (TreeNode<Set<String>> node2Child : node2Children) {

                    BigDecimal ehdValue = eHd(node1Child, node2Child);

                    if (max.compareTo(ehdValue) < 0) {
                        max = ehdValue;
                    }
                }

                sum = sum.add(max);
            }

            StringBuilder builder = new StringBuilder().append(node1Children.size());
            BigDecimal divisor = new BigDecimal(builder.toString());

            return sum.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
        }
    }

    protected BigDecimal eHd(TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        if (node1 == null || node2 == null) {
            throw new JSimPiException("Unable to ehd as node1[" + node1 , ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("e-hd: node1: " + node1.getData().toString() + " node2: " + node2.getData().toString());

        }

        BigDecimal gammaValue = gamma(node1.getEdgeToParent(), node2.getEdgeToParent());

        BigDecimal nu = preferenceProfile.getDefaultRoleDiscountFactor();

        BigDecimal nuPrime = BigDecimal.ONE.subtract(nu);

        if (logger.isDebugEnabled()) {
            logger.debug("e-hd: gammaValue[" + gammaValue.toPlainString() + "], nu[" + nu.toPlainString() + "], nuPrime[" + nuPrime + "].");
        }

        BigDecimal simSubTree = measureDirectedSimilarity(node1, node2);

        if (logger.isDebugEnabled()) {
            logger.debug("e-hd: simSubTree[" + simSubTree + "].");
            logger.debug("nuPrime.multiply(simSubTree) = " + nuPrime.multiply(simSubTree));
            logger.debug("nuPrime.multiply(simSubTree).add(nu) " + nuPrime.multiply(simSubTree).add(nu));
            logger.debug("nuPrime.multiply(simSubTree).add(nu).multiply(gammaValue) " + nuPrime.multiply(simSubTree).add(nu).multiply(gammaValue));
        }

        return nuPrime.multiply(simSubTree).add(nu).multiply(gammaValue);
    }

    // involve with 'roles'
    protected BigDecimal gamma(String edge1, String edge2) {
        if (edge1 == null || edge2 == null) {
            throw new JSimPiException("Unable to gamma as edge1[" +
                    edge1 + "] and edge2[" + edge2 + "] are null." , ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("gamma: edge1[" + edge1 + "], edge2[" + edge2 + "].");
        }

        Set<String> edgeSet1 = iRoleUnfolder.unfoldRoleHierarchy(edge1);
        Set<String> edgeSet2 = iRoleUnfolder.unfoldRoleHierarchy(edge2);

        Set<String> intersection = Sets.intersection(edgeSet1, edgeSet2);

        StringBuilder builder1 = new StringBuilder().append(intersection.size());
        BigDecimal numerator = new BigDecimal(builder1.toString());

        StrBuilder builder2 = new StrBuilder().append(edgeSet1.size());
        BigDecimal divisor = new BigDecimal(builder2.toString());

        if (logger.isDebugEnabled()) {
            logger.debug("gamma: numerator[" + numerator + "], divisor[" + divisor + "].");
        }

        return numerator.divide(divisor, 5, BigDecimal.ROUND_HALF_UP);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public BacktraceTable getBacktraceTable() {
        return backtraceTable;
    }

    @Override
    public BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        if (tree1 == null || tree2 == null) {
            throw new JSimPiException("Unable to measure directed similarity as tree1[" + tree1 + "] " +
                    "and tree2[" + tree2 + "] are null.", ErrorCode.TopDownSimReasonerImpl_IllegalArguments);
        }

        this.backtraceTable = new BacktraceTable();

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
