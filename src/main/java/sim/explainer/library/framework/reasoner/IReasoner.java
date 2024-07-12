package sim.explainer.library.framework.reasoner;

import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface IReasoner {

    BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2);

    void setRoleUnfoldingStrategy(IRoleUnfolder iRoleUnfolder);

    List<String> getExecutionTimes();

}
