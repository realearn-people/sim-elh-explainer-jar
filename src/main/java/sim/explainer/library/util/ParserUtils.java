package sim.explainer.library.util;

import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;

public class ParserUtils {

    public static final char OPEN_PARENTHESIS_CHAR = '(';
    public static final char CLOSE_PARENTHESIS_CHAR = ')';

    public static final String OPEN_PARENTHESIS_STR = "(";
    public static final String CLOSE_PARENTHESIS_STR = ")";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String compactConceptDescriptionString(String conceptDescription) {
        if (conceptDescription == null) {
            throw new JSimPiException("Unable to compact concept description string as conceptDescription is null.", ErrorCode.ParserUtils_IllegalArguments);
        }

        String tmpConcept = StringUtils.replacePattern(conceptDescription, "\\s+", StringUtils.SPACE);
        tmpConcept = StringUtils.replacePattern(tmpConcept, " \\)", CLOSE_PARENTHESIS_STR);
        tmpConcept = StringUtils.replacePattern(tmpConcept, "\\( ", OPEN_PARENTHESIS_STR);

        return tmpConcept;
    }

    public static int getLastMatchedCloseParenthesis(String concept) {
        if (concept == null) {
            throw new JSimPiException("Unable to get last matched close parenthesis", ErrorCode.ParserUtils_IllegalArguments);
        }

        Stack<Character> parenthesis = new Stack<Character>();

        for (int i = 0; i < concept.length(); i++) {

            if(concept.charAt(i) == '(') {
                parenthesis.push(concept.charAt(i));
            }

            else if (concept.charAt(i) == ')') {

                if (parenthesis.empty()) {
                    return -1;
                }

                Character c = parenthesis.pop();
                if (c == '(' && parenthesis.empty()) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static String generateFreshName(String name) {
        if (name == null) {
            throw new JSimPiException("Unable to generate fresh name as name is null.", ErrorCode.ParserUtils_IllegalArguments);
        }

        StringBuilder builder = new StringBuilder(name);
        builder.append("'");

        return builder.toString();
    }

    public static String convertToRoleForm(String role) {
        if (role == null) {
            throw new JSimPiException("Unable to convert to role form as role is null.", ErrorCode.ParserUtils_IllegalArguments
            ) ;
        }

        StringBuilder builder = new StringBuilder("<");
        builder.append(role);
        builder.append(">");

        return builder.toString();
    }
}
