package sim.explainer.library.enumeration;

public enum TypeConstant {
    DYNAMIC_SIM("dynamic programming Sim"),
    DYNAMIC_SIMPI("dynamic programming SimPi"),
    TOPDOWN_SIM("top down Sim"),
    TOPDOWN_SIMPI("top down SimPi");

    private final String description;

    TypeConstant(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}