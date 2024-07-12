package sim.explainer.library.util.syntaxanalyzer;

public abstract class ParserHandler extends Handler {

    protected static final String EXISTENTIAL_RESTRICTION_SYMBOL = "some";
    protected static final String AND_SYMBOL = "and";

    protected static final String PATTERN_NAME = "[a-zA-Z]+[A-Za-z0-9_\\\']*";

}
