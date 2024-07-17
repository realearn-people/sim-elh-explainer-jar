package sim.explainer.library.service;

import sim.explainer.library.enumeration.FileTypeConstant;
import sim.explainer.library.enumeration.TypeConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.KRSSServiceContext;
import sim.explainer.library.framework.OWLServiceContext;
import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeBuilder;
import sim.explainer.library.framework.reasoner.*;
import sim.explainer.library.framework.unfolding.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class SimilarityService {

    private final BigDecimal TWO = new BigDecimal("2");

    private IReasoner topDownSimReasonerImpl;
    private IReasoner topDownSimPiReasonerImpl;
    private IReasoner dynamicProgrammingSimReasonerImpl;
    private IReasoner dynamicProgrammingSimPiReasonerImpl;

    private IConceptUnfolder conceptDefinitionUnfolderManchesterSyntax;
    private IConceptUnfolder conceptDefinitionUnfolderKRSSSyntax;
    private IRoleUnfolder superRoleUnfolderManchesterSyntax;
    private IRoleUnfolder superRoleUnfolderKRSSSyntax;

    private TreeBuilder treeBuilder = new TreeBuilder();

    private BacktraceTable backtraceTable_forward = new BacktraceTable();
    private BacktraceTable backtraceTable_backward = new BacktraceTable();

    public SimilarityService(OWLServiceContext owlServiceContext, KRSSServiceContext krssServiceContext, PreferenceProfile preferenceProfile) {
        this.conceptDefinitionUnfolderManchesterSyntax = new ConceptDefinitionUnfolderManchesterSyntax(owlServiceContext);
        this.conceptDefinitionUnfolderKRSSSyntax = new ConceptDefinitionUnfolderKRSSSyntax(krssServiceContext);
        this.superRoleUnfolderManchesterSyntax = new SuperRoleUnfolderManchesterSyntax(owlServiceContext);
        this.superRoleUnfolderKRSSSyntax = new SuperRoleUnfolderKRSSSyntax(krssServiceContext);

        this.topDownSimReasonerImpl = new TopDownSimReasonerImpl(preferenceProfile);
        this.topDownSimPiReasonerImpl = new TopDownSimPiReasonerImpl(preferenceProfile);
        this.dynamicProgrammingSimReasonerImpl = new DynamicProgrammingSimReasonerImpl(preferenceProfile);
        this.dynamicProgrammingSimPiReasonerImpl = new DynamicProgrammingSimPiReasonerImpl(preferenceProfile);
    }

    public Tree<Set<String>> unfoldAndConstructTree(IConceptUnfolder iConceptUnfolder, String conceptName1) {
        String unfoldConceptName1 = iConceptUnfolder.unfoldConceptDefinitionString(conceptName1);

        if (iConceptUnfolder instanceof ConceptDefinitionUnfolderManchesterSyntax) {
            return treeBuilder.constructAccordingToManchesterSyntax(conceptName1, unfoldConceptName1);
        }

        else {
            return treeBuilder.constructAccordingToKRSSSyntax(conceptName1, unfoldConceptName1);
        }
    }

    private BigDecimal computeSimilarity(IReasoner iReasoner, IRoleUnfolder iRoleUnfolder, Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        iReasoner.setRoleUnfoldingStrategy(iRoleUnfolder);

        BigDecimal forwardDistance = iReasoner.measureDirectedSimilarity(tree1, tree2);
        this.backtraceTable_forward = iReasoner.getBacktraceTable();
        BigDecimal backwardDistance = iReasoner.measureDirectedSimilarity(tree2, tree1);
        this.backtraceTable_backward = iReasoner.getBacktraceTable();

        return forwardDistance.add(backwardDistance).divide(TWO);
    }

    /**
     * Measure a similarity degree from given concepts with a specified concept and measurement types.
     *
     * @param conceptName1 first concept
     * @param conceptName2 second concept
     * @param measurementType  concept type, i.e., KRSS or OWL
     * @param conceptType  measurement type, i.e., dynamic/top down and sim/simpi
     * @return similarity degree of that concept pair
     */
    public BigDecimal measureConceptWithType(String conceptName1, String conceptName2, TypeConstant measurementType, FileTypeConstant conceptType) {

        IConceptUnfolder conceptT;
        IRoleUnfolder roleUnfolderT;
        IReasoner reasonerT;
        BigDecimal result;

        if (conceptName1 == null || conceptName2 == null) {
            throw new JSimPiException("Unable measure with " + measurementType + " as conceptName1[" + conceptName1 + "] and " +
                    "conceptName2[" + conceptName2 + "] are null.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (conceptType == FileTypeConstant.KRSS_FILE) {
            conceptT = conceptDefinitionUnfolderKRSSSyntax;
            roleUnfolderT = superRoleUnfolderKRSSSyntax;
        } else if (conceptType  == FileTypeConstant.OWL_FILE) {
            conceptT = conceptDefinitionUnfolderManchesterSyntax;
            roleUnfolderT = superRoleUnfolderManchesterSyntax;
        } else {
            throw new JSimPiException("Unable measure with this file type.", ErrorCode.OWLSimService_IllegalArguments);
        }

        if (measurementType == TypeConstant.DYNAMIC_SIM) {
            reasonerT = dynamicProgrammingSimReasonerImpl;
        } else if (measurementType == TypeConstant.DYNAMIC_SIMPI) {
            reasonerT = dynamicProgrammingSimPiReasonerImpl;
        } else if (measurementType == TypeConstant.TOPDOWN_SIM) {
            reasonerT = topDownSimReasonerImpl;
        } else if (measurementType == TypeConstant.TOPDOWN_SIMPI) {
            reasonerT = topDownSimPiReasonerImpl;
        } else {
            throw new JSimPiException("Unable measure with this approach.", ErrorCode.OWLSimService_IllegalArguments);
        }

        Tree<Set<String>> tree1 = unfoldAndConstructTree(conceptT, conceptName1);
        Tree<Set<String>> tree2 = unfoldAndConstructTree(conceptT, conceptName2);

        result = computeSimilarity(reasonerT, roleUnfolderT, tree1, tree2);

        return result;
    }

    public List<BacktraceTable> getBacktraceTables() {
        List<BacktraceTable> backtraceTables = new ArrayList<>();
        backtraceTables.add(backtraceTable_forward);
        backtraceTables.add(backtraceTable_backward);
        return backtraceTables;
    }
}
