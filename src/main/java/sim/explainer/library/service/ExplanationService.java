package sim.explainer.library.service;

import org.springframework.stereotype.Component;
import sim.explainer.library.framework.BackTraceTable;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeNode;

import java.math.BigDecimal;
import java.util.*;

/**
 * runchana:2023-31-07
 * The ExplanationService class's method will be invoked inside SimilarityService.
 * This class will be used for extracting similarity explanation from the BackTraceTable class that contains computation.
 */
@Component("explanationService")
public class ExplanationService {

    public static StringBuilder explanation = new StringBuilder();

    public void explainSimilarity(BackTraceTable backTraceTable) {

        BigDecimal degree = null;

        String conceptName1 = "";
        String conceptName2 = "";

        StringBuilder res = new StringBuilder();

        List<String[]> priList = new ArrayList<>();
        List<List<String>> exiList = new ArrayList<>();

        // runchana:2023-31-07 iterate through each value in backTraceTable
        Deque<Map.Entry<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>>> lastTwoEntries = new LinkedList<>();

        for (Map.Entry<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>> backtrace : backTraceTable.getBackTraceTable().entrySet()) {

            Map<Integer, String[]> keyMap = backtrace.getKey();

            // runchana:2023-31-07 retrieve concept names from an outermost mapping key
            for (int i = 0; i < keyMap.size(); i++) {
                String[] arrayValue = keyMap.get(i);
                if (arrayValue != null) {
                    for (int j = 0; j < arrayValue.length; j++) {
                        if (j == 0) {
                            conceptName1 = arrayValue[0];
                        } else {
                            conceptName2 = arrayValue[1];
                        }
                    }
                }
            }

            // runchana:2023-31-07 keep only the last two from linkedHashMap to ensure that it's a new concept pair
            lastTwoEntries.add(backtrace);
            if (lastTwoEntries.size() > 2) {
                lastTwoEntries.removeFirst();
            }
        }

        for (Map.Entry<Map<Integer, String[]>, Map<String, Map<Tree<Set<String>>, BigDecimal>>> backtrace : lastTwoEntries) {

            Map<String, Map<Tree<Set<String>>, BigDecimal>> valueMap = backtrace.getValue();

            // runchana:2023-31-07 retrieve primitives and degree along with its concept name
            for (Map.Entry<String, Map<Tree<Set<String>>, BigDecimal>> entry : valueMap.entrySet()) {
                String[] priArr = new String[1];
                List<String> exiEach = new ArrayList<>();

                String key = entry.getKey(); // concept name
                String exi;

                for (Map.Entry<Tree<Set<String>>, BigDecimal> child : entry.getValue().entrySet()) {

                    // runchana:2023-31-07 find a set of existential in each concept name
                    for (Map.Entry<Integer, TreeNode<Set<String>>> tree : child.getKey().getNodes().entrySet()) {
                        exi = tree.getValue().getEdgeToParent();

                        if (exi != null) {
                            exiEach.add(exi);
                        }
                    }

                    // runchana:2023-31-07 retrieve a homomorphism degree
                    degree = child.getValue();

                    // runchana:2023-31-07 retrieve a primitive concept
                    priArr[0] = child.getKey().getNodes().get(0).toString();

                    removeUnwantedChar(priArr);

                    priList.add(priArr);
                    exiList.add(exiEach);
                }

                res.append("\t\t|--- ").append(key).append("\n\t\t\t|--- Concepts: ").append(Arrays.toString(priArr));

                if (!exiEach.isEmpty()){
                    res.append("\n\t\t\t\t|--- Roles: ").append(exiEach);
                }

                res.append("\n");
            }

        }

        // runchana:2023-31-07 find matched concepts and existential for explanation details
        Set<String> matchingCon = findMatchingConcept(priList);
        Set<String> matchingRole = findMatchingRole(exiList);

        if (matchingCon.size() == 0) {
            matchingCon.add("nothing");
        }

        explanation.append(conceptName1).append("\t").append(conceptName2).append("\t").append(degree.setScale(5, BigDecimal.ROUND_HALF_UP)).append("\n");

        // runchana:2023-10-12 explanation part with new format, using \t
        explanation.append("\t").append("The similarity between ").append(conceptName1).append(" and ").append(conceptName2)
                .append(" is ").append(degree.setScale(5, BigDecimal.ROUND_HALF_UP));
        explanation.append(" because they have ").append(matchingCon).append(" in common.");

        if (!matchingRole.isEmpty()) {
            explanation.append("\n\t").append(" Moreover, both of them also ").append(matchingRole).append(".");
        }

        explanation.append("\n").append(res);
    }

    /**
     * runchana:2023-31-07
     * Remove unwanted character (') that is usually appear in primitive concepts
     * to make an explanation looks more natural in human terms.
     * @param wordsArray array of primitive concepts
     */
    private void removeUnwantedChar(String[] wordsArray) {
        for (int i = 0; i < wordsArray.length; i++) {
            wordsArray[i] = wordsArray[i].replaceAll("'", "");
        }
    }

    /**
     * runchana:2023-31-07
     * Find matched primitive concepts from a concept pair
     * so that its similarity degree is explainable in an understandable term.
     * @param wordsList List of string[] that keeps primitive concepts of concept1 and concept2
     * @return A set of string that contains matched primitive concepts
     */
    private static Set<String> findMatchingConcept(List<String[]> wordsList) {
        Set<String> matchingWords = new HashSet<>();

        if (!wordsList.isEmpty()) {
            String[] concept1 = wordsList.get(0);
            String[] concept2 = wordsList.get(1);

            int c1_length = concept1.length;
            int c2_length = concept2.length;

            for (int i = 0; i < c1_length; i++) {
                String[] conName1 = concept1[i].split("\\s+");

                for (int j = 0; j < c2_length; j++) {
                    String[] conName2 = concept2[i].split("\\s+");

                    for (String cn1 : conName1) {
                        for (String cn2 : conName2) {
                            if (cn1.equals(cn2)) {
                                matchingWords.add(cn1);
                            }
                        }
                    }
                }
            }
        }

        return matchingWords;
    }

    /**
     * runchana:2023-31-07
     * Find matched roles (existential) from a concept pair
     * so that its similarity degree is explainable in an understandable term.
     * @param listOfLists List of List<String> that keeps existential of concept1 and concept2
     * @return A set of string that contains matched existential
     */
    public Set<String> findMatchingRole(List<List<String>> listOfLists) {
        Set<String> matchingStrings = new HashSet<>();

        List<String> firstSet = listOfLists.get(0);
        List<String> secondSet = listOfLists.get(1);

        if (!firstSet.isEmpty()) {
            for (String list : firstSet) {
                for (String str : secondSet) {
                    if (str.equals(list) && !str.equals("** DO NOT HAVE ANY ROLES **")) {
                        matchingStrings.add(str);
                    }
                }
            }
        }

        return matchingStrings;
    }
}