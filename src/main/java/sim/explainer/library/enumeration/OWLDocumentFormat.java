package sim.explainer.library.enumeration;

public enum OWLDocumentFormat {

    // teeradaj@20160310: https://www.w3.org/TR/2012/REC-owl2-overview-20121211/
    MANCHESTER_SYNTAX_DOCUMENT("Manchester OWL Syntax"),
    KRSS_SYNTAX_DOCUMENT("KRSS OWL Syntax");

    private final String str;

    OWLDocumentFormat(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return this.str;
    }
}
