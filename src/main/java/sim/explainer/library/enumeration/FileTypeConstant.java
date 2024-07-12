package sim.explainer.library.enumeration;

public enum FileTypeConstant {
    OWL_FILE("OWL Filetype"),
    KRSS_FILE("KRSS Filetype"),
    INVALID_FILE("INVALID Filetype");

    private final String description;

    FileTypeConstant(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
