package sim.explainer.library.util.syntaxanalyzer.manchester;

import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.syntaxanalyzer.ParserHandler;
import sim.explainer.library.util.MyStringUtils;
import sim.explainer.library.util.ParserUtils;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManchesterTopLevelParserHandler extends ParserHandler {

    private static final Logger logger = LoggerFactory.getLogger(ManchesterTopLevelParserHandler.class);

    private static final String PATTERN_NAME_SOME = PATTERN_NAME + StringUtils.SPACE + EXISTENTIAL_RESTRICTION_SYMBOL + StringUtils.SPACE;
    private static final String PATTERN_NAME_SOME_NAME = PATTERN_NAME_SOME + PATTERN_NAME;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected String returnTopLevelConceptStringIfAvailable(HandlerContextImpl context, String concept) {
        if (context == null || concept == null) {
            throw new JSimPiException("Unable to return top level concept string if existed as context[" + context + "] and conceptDescription[" + concept + "] are null.", ErrorCode.ManchesterTopLevelParserHandler_IllegalArguments);
        }

        // Invoke business logic
        String compactFormat = ParserUtils.compactConceptDescriptionString(concept);

        int beginParenthesis = StringUtils.indexOf(compactFormat, ParserUtils.OPEN_PARENTHESIS_STR);
        int lastParenthesis = ParserUtils.getLastMatchedCloseParenthesis(compactFormat);

        // Validate input
        if (beginParenthesis > -1 && lastParenthesis == -1) {
            throw new JSimPiException("Unable to return top level concept string if existed due to inequivalent parenthesis number.", ErrorCode.ManchesterTopLevelParserHandler_InEquivalentParenthesisNumbers);
        }

        // If there exist parenthesises, we need to simplify them before extraction
        if (beginParenthesis > -1) {

            // If compactFormat contains existential restrictions
            if (StringUtils.contains(compactFormat, EXISTENTIAL_RESTRICTION_SYMBOL)) {
                String group = StringUtils.substring(compactFormat, beginParenthesis, lastParenthesis + 1);

                // Transform to String literals
                String groupStr = StringUtils.replacePattern(group, "\\(", "\\\\(");
                groupStr = StringUtils.replacePattern(groupStr, "\\)", "\\\\)");

                String patternStr = PATTERN_NAME_SOME + groupStr;
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(compactFormat);

                // If compactFormat is already in a form of "role some (concept)"
                if (matcher.find()) {
                    String role = storeRoleAndNestedConceptPair(context, matcher.group());
                    String roleForm = ParserUtils.convertToRoleForm(role);

                    if (logger.isDebugEnabled()) {
                        logger.debug("1. compactFormat is already in a form of \"role some (concept)\".");
                    }

                    return compactFormat.replaceFirst(patternStr, roleForm);
                }

                // Proceed to check if compactFormat is already in a form of "(role some concept)"
                else {

                    String str = MyStringUtils.removeCharactersFrom(group, 0, group.length() - 2);

                    pattern = Pattern.compile(PATTERN_NAME_SOME_NAME);
                    matcher = pattern.matcher(str);

                    // If so, just store role and nested concept
                    if(matcher.find()) {
                        String role = storeRoleAndNestedConceptPair(context, str);
                        String roleForm = ParserUtils.convertToRoleForm(role);

                        if (logger.isDebugEnabled()) {
                            logger.debug("2. compactFormat is already in a form of \"(role some concept)\".");
                        }

                        return compactFormat.replaceFirst(groupStr, roleForm);
                    }

                    // Otherwise, there exist nested parenthesises.
                    // Hence, just remove them
                    else {

                        if (logger.isDebugEnabled()) {
                            logger.debug("3. compactFormat contains nested parenthesises.");
                        }

                        return MyStringUtils.removeCharactersFrom(compactFormat, beginParenthesis, lastParenthesis - 1);
                    }

                }
            }

            // Otherwise, it is in a form of "(primitive and primitive)"
            else {

                if (logger.isDebugEnabled()) {
                    logger.debug("4. compactFormat is already in a form of \"(primitive and primitive)\".");
                }

                return MyStringUtils.removeCharactersFrom(compactFormat, beginParenthesis, lastParenthesis - 1);
            }

        }

        // Check if compactFormat is in a form of "concept and role some concept"
        else if (StringUtils.contains(compactFormat, EXISTENTIAL_RESTRICTION_SYMBOL)) {
            Pattern pattern = Pattern.compile(PATTERN_NAME_SOME_NAME);
            Matcher matcher = pattern.matcher(compactFormat);

            // If so,
            if (matcher.find()) {
                String role = storeRoleAndNestedConceptPair(context, matcher.group());
                String roleForm = ParserUtils.convertToRoleForm(role);

                if (logger.isDebugEnabled()) {
                    logger.debug("5. compactFormat is already in a form of \"concept and role some concept\".");
                }

                return compactFormat.replaceFirst(PATTERN_NAME_SOME_NAME, roleForm);
            }

            // Otherwise, it only contains primitive concepts
            else {

                if (logger.isDebugEnabled()) {
                    logger.debug("compactFormat: " + compactFormat);
                    logger.debug("pattern: " + PATTERN_NAME_SOME_NAME);
                }

                throw new JSimPiException("Unable to return top level concept string if available as compactFormat has incorrect syntax.", ErrorCode.ManchesterTopLevelParserHandler_InvalidSyntaxException);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("NULL case: compactFormat is already top level concept string.");
        }

        return null;
    }

    protected String storeRoleAndNestedConceptPair(HandlerContextImpl context, String str) {
        if (context == null || str == null) {
            throw new JSimPiException("Unable to store role and nested concept pair as context[" + context + "] and str[" + str + "] are null.", ErrorCode.ManchesterTopLevelParserHandler_IllegalArguments);
        }

        String role = null;
        if (StringUtils.contains(str, EXISTENTIAL_RESTRICTION_SYMBOL)) {
            Pattern pattern = Pattern.compile("^" + PATTERN_NAME);
            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                role = matcher.group();
            }

            else {
                throw new JSimPiException("Unable to match pattern[" + pattern.toString() + "].", ErrorCode.ManchesterTopLevelParserHandler_InvalidSyntaxException);
            }

            StringBuilder builder = new StringBuilder(role);
            builder.append(StringUtils.SPACE);
            builder.append(EXISTENTIAL_RESTRICTION_SYMBOL);
            builder.append(StringUtils.SPACE);


            String nestedConcept = StringUtils.trim(StringUtils.replaceOnce(str, builder.toString(), StringUtils.EMPTY));

            if (nestedConcept.charAt(0) == '(' && nestedConcept.charAt(nestedConcept.length() - 1) == ')') {
                nestedConcept = MyStringUtils.removeCharactersFrom(nestedConcept, 0, nestedConcept.length() - 2);
            }

            context.addToEdgePrimitiveConceptExistentialMap(role, nestedConcept);
        }

        return role;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke top level extraction handler as context is null.", ErrorCode.ManchesterTopLevelParserHandler_IllegalArguments);
        }

        String conceptDescription = context.getConceptDescription();
        context.setTopLevelDescription(conceptDescription);

        String topLevelStr = returnTopLevelConceptStringIfAvailable(context, conceptDescription);

        // null means conceptDescription does not contain existential.
        while (topLevelStr != null) {
            context.setTopLevelDescription(topLevelStr);
            topLevelStr = returnTopLevelConceptStringIfAvailable(context, topLevelStr);
        }

        ChainOfResponsibilityHandler nextHandler = getNextHandler();
        if (nextHandler != null) {
            nextHandler.invoke(context);
        }
    }
}
