package sim.explainer.library.util.syntaxanalyzer;

public abstract class ChainOfResponsibilityHandler<E extends IChainOfResponsibilityContext> {

    private ChainOfResponsibilityHandler<E> nextHandler;

    public ChainOfResponsibilityHandler<E> getNextHandler() {
        return nextHandler;
    }

    /**
     * Allow call chaining by returning a reference to the current instance.
     *
     * @param nextHandler
     * @return
     */
    public ChainOfResponsibilityHandler<E> setNextHandler(ChainOfResponsibilityHandler<E> nextHandler) {
        this.nextHandler = nextHandler;
        return this;
    }

    public abstract void invoke(E context);
}
