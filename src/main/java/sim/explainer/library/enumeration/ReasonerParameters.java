package sim.explainer.library.enumeration;

public enum ReasonerParameters {

    SIM_CONSTANT_NU("reasoner.sim.constant.nu", "0.4");

    private final String str;
    private final String val;

    ReasonerParameters(String str, String val) {
        this.str = str;
        this.val = val;
    }

    public String getStr() {
        return str;
    }

    public String getVal() {
        return val;
    }
}
