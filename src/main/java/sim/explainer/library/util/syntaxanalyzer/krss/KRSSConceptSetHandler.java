package sim.explainer.library.util.syntaxanalyzer.krss;

import sim.explainer.library.enumeration.KRSSConstant;
import sim.explainer.library.exception.ErrorCode;
import sim.explainer.library.exception.JSimPiException;
import sim.explainer.library.util.syntaxanalyzer.ChainOfResponsibilityHandler;
import sim.explainer.library.util.syntaxanalyzer.Handler;
import sim.explainer.library.util.syntaxanalyzer.HandlerContextImpl;
import org.apache.commons.lang3.StringUtils;

public class KRSSConceptSetHandler extends Handler {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void invoke(HandlerContextImpl context) {
        if (context == null) {
            throw new JSimPiException("Unable to invoke krss concept set handler as context is null.", ErrorCode.KrssConceptSetHandler_IllegalArguments);
        }

        if (context.getTopLevelDescription().equals(KRSSConstant.TOP_CONCEPT.getStr())) {
            // Do nothing
        }

        else {
            String[] elements = StringUtils.split(context.getTopLevelDescription());

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
