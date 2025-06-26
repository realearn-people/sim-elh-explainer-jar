package sim.explainer.library.util.syntaxanalyzer.manchester;

import sim.explainer.library.enumeration.OWLConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.Handler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManchesterConceptSetHandler extends Handler {

    private static final Logger logger = LoggerFactory.getLogger(ManchesterConceptSetHandler.class);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke concept set handler as context is null.", ErrorCode.ManchesterConceptSetHandler_IllegalArguments);
        };

        if (logger.isDebugEnabled()) {
            logger.debug("ManchesterConceptSetHandler" +
                    " - context topLevelDescription[" + context.getTopLevelDescription() + "]");
        }

        if (context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_1.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_2.getOwlSyntax())
                || context.getTopLevelDescription().equals(OWLConstant.TOP_CONCEPT_3.getOwlSyntax())) {
            // Do nothing
        }

        else {
            String[] elements = StringUtils.splitByWholeSeparator(context.getTopLevelDescription(), "and");

            if (logger.isDebugEnabled()) {
                logger.debug("ManchesterConceptSetHandler - elements length[" + elements.length + "]");
            }

            for (String element : elements) {

                if (!StringUtils.containsAny(element, '<', '>') && StringUtils.isNotBlank(element)) {
                    context.addToPrimitiveConceptSet(StringUtils.trim(element));
                }
            }
        }

        ChainOfResponsibilityHandler nextHandler = getNextHandler();
        if (nextHandler != null) {
            nextHandler.invoke(context);
        }
    }
}
