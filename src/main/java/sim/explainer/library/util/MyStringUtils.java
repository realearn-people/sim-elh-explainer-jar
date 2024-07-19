package sim.explainer.library.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MyStringUtils {

    private static final String TREE_STR = "tree";

    public static String removeCharactersFrom(String str, Integer... index) {
        StringBuilder builder = new StringBuilder(str);

        for (Integer i : index) {
            builder.deleteCharAt(i);
        }

        return builder.toString();
    }

    public static String generateTreeLabel(String concept) {
        StringBuilder builder = new StringBuilder(concept);
        builder.append(StringUtils.SPACE);
        builder.append(TREE_STR);

        return builder.toString();
    }

    public static String generateExistential(String roleName, String conceptName) {
        StringBuilder builder = new StringBuilder("some");
        builder.append(StringUtils.SPACE);
        builder.append(roleName);
        builder.append(StringUtils.SPACE);
        builder.append(conceptName);

        return builder.toString();
    }

    public static String mapConcepts(String unfoldedConceptDescription, HashMap<String, String> unfoldedConceptMap) {
        // Initialize the result with the unfoldedConceptDescription
        String result = unfoldedConceptDescription;

        boolean changesMade;
        do {
            changesMade = false;

            // Iterate through the map and replace occurrences in the result
            for (Map.Entry<String, String> entry : unfoldedConceptMap.entrySet()) {
                String unfoldedConcept = entry.getKey();
                String originalConcept = entry.getValue();

                if (result.contains(unfoldedConcept)) {
                    result = result.replace(unfoldedConcept, originalConcept);
                    changesMade = true;
                }
            }

        } while (changesMade); // Loop until no changes are made

        return result;
    }

}
