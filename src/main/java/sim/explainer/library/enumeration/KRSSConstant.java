package sim.explainer.library.enumeration;

public enum KRSSConstant {
    TOP_CONCEPT("TOP"),
    TOP_ROLE("top");

    private final String str;

    KRSSConstant(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return this.str;
    }
}
