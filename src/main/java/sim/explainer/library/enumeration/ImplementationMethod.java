package sim.explainer.library.enumeration;

public enum ImplementationMethod {
    DYNAMIC_SIM("dynamic programming Sim"),
    DYNAMIC_SIMPI("dynamic programming SimPi"),
    TOPDOWN_SIM("top down Sim"),
    TOPDOWN_SIMPI("top down SimPi");

    private final String description;

    ImplementationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}