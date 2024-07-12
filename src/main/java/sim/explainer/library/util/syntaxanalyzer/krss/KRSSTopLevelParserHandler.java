package sim.explainer.library.util.syntaxanalyzer.krss;

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

public class KRSSTopLevelParserHandler extends ParserHandler {

    private static final Logger logger = LoggerFactory.getLogger(KRSSTopLevelParserHandler.class);

    private static final String PARENTHESIS_AND = ParserUtils.OPEN_PARENTHESIS_STR + AND_SYMBOL + StringUtils.SPACE;
    private static final String PARENTHESIS_SOME = ParserUtils.OPEN_PARENTHESIS_STR + EXISTENTIAL_RESTRICTION_SYMBOL + StringUtils.SPACE;

    protected static final String PATTERN_NAME = "[a-zA-Z]+[0-9_']*|[0-9_']+";


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected String returnTopLevelConceptStringIfAvailable(HandlerContextImpl context, String concept) {
        if (context == null || concept == null) {
            throw new JSimPiException("Unable to return top level concept string if existed as context[" + context + "] and conceptDescription[" + concept + "] are null.", ErrorCode.KrssTopLevelParserHandler_IllegalArguments);
        }

        String compactFormat = ParserUtils.compactConceptDescriptionString(concept);

        int beginParenthesis = StringUtils.indexOf(compactFormat, ParserUtils.OPEN_PARENTHESIS_STR);
        int lastParenthesis = ParserUtils.getLastMatchedCloseParenthesis(compactFormat);

        int firstAndPosition = compactFormat.indexOf(PARENTHESIS_AND);
        int firstSomePosition = compactFormat.indexOf(PARENTHESIS_SOME);


        if (logger.isDebugEnabled()) {

            logger.debug("compact format: " + compactFormat);
            logger.debug("first parenthesis: " + beginParenthesis);
            logger.debug("last parenthesis: " + lastParenthesis);
        }

        // Validate input
        if (beginParenthesis > -1 && lastParenthesis == -1) {
            throw new JSimPiException("Unable to return top level concept string if existed due to inequivalent parenthesis number.", ErrorCode.KrssTopLevelParserHandler_InEquivalentParenthesisNumbers);
        }

        if ((firstAndPosition != -1 && firstAndPosition < firstSomePosition) || (firstAndPosition > -1 && firstSomePosition == -1)) {
            String str = MyStringUtils.removeCharactersFrom(compactFormat, lastParenthesis);
            return StringUtils.replaceOnce(str, PARENTHESIS_AND, StringUtils.EMPTY);
        }

        else if ((firstSomePosition != -1 && firstSomePosition < firstAndPosition) || (firstAndPosition == -1 && firstSomePosition > -1)) {

            String str = StringUtils.substring(compactFormat, firstSomePosition, lastParenthesis + 1);
            String strWithOutParenthesises = MyStringUtils.removeCharactersFrom(str, 0, str.length() - 2);
            String role = storeRoleAndNestedConceptPair(context, strWithOutParenthesises);
            String roleForm = ParserUtils.convertToRoleForm(role);

            return StringUtils.strip(StringUtils.replaceOnce(compactFormat, str, roleForm));
        }

        return null;
    }

    protected String storeRoleAndNestedConceptPair(HandlerContextImpl context, String str) {
        if (context == null || str == null) {
            throw new JSimPiException("Unable to store role and nested concept pair as context[" + context + "] and str[" + str + "] are null.", ErrorCode.ManchesterTopLevelParserHandler_IllegalArguments);
        }

        String role = null;

        Pattern pattern = Pattern.compile("^some (" + PATTERN_NAME + ") ");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            role = matcher.group(1);

            if (logger.isDebugEnabled()) {
                logger.debug("role: " + role);
            }

            StringBuilder builder = new StringBuilder(EXISTENTIAL_RESTRICTION_SYMBOL);
            builder.append(StringUtils.SPACE);
            builder.append(role);
            builder.append(StringUtils.SPACE);

            String nestedConcept = StringUtils.trim(StringUtils.replaceOnce(str, builder.toString(), StringUtils.EMPTY));

            if (logger.isDebugEnabled()) {
                logger.debug("nested concept: " + nestedConcept);
            }

            context.addToEdgePrimitiveConceptExistentialMap(role, nestedConcept);
        }

        else {

            throw new JSimPiException("Unable to match pattern[" + pattern.toString() + "].", ErrorCode.KrssTopLevelParserHandler_InvalidSyntaxException);
        }

        return role;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke top level extraction handler as context is null.", ErrorCode.KrssConceptSetHandler_IllegalArguments);
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
