package sim.explainer.library.framework.unfolding;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public interface IConceptUnfolder {

    String unfoldConceptDefinitionString(String conceptName);

    public HashMap<String, String> getUnfoldedConceptMap();
}
