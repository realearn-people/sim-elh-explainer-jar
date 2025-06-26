package sim.explainer.library.enumeration;

public enum OWLConstant {
    TOP_CONCEPT_1("Top1", "Top", "[owl:Thing]"),
    TOP_CONCEPT_2("Top2", "Top", "Thing"),
    TOP_CONCEPT_3("Top3", "Top", "owl:Thing"),
    TOP_ROLE("", "", "[owl:topObjectProperty]");

    private final String str;
    private final String descriptionLogicSyntax;
    private final String owlSyntax;

    OWLConstant(String str, String descriptionLogicSyntax, String owlSyntax) {
        this.str = str;
        this.descriptionLogicSyntax = descriptionLogicSyntax;
        this.owlSyntax = owlSyntax;
    }

    public String getStr() {
        return str;
    }

    public String getDescriptionLogicSyntax() {
        return descriptionLogicSyntax;
    }

    public String getOwlSyntax() {
        return owlSyntax;
    }

    @Override
    public String toString() {
        return this.str;
    }
}
