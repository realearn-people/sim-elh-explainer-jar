package sim.explainer.library.util;

import org.apache.commons.lang3.StringUtils;

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

}
