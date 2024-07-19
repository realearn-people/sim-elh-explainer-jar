package sim.explainer.library.framework.reasoner;

import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface IReasoner {

    BacktraceTable getBacktraceTable();

    BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2, HashMap<String, String> mapper);

    void setRoleUnfoldingStrategy(IRoleUnfolder iRoleUnfolder);

    List<String> getExecutionTimes();

}
