package sim.explainer.library.framework.explainer;

import java.math.BigDecimal;
import java.util.HashSet;

public class SimRecord {
    private BigDecimal deg = new BigDecimal(0.0); // homomorphism degree
    private HashSet<String> pri = new HashSet<>(); // a set of primitives between 2 comparing concepts that derives deg.
    private HashSet<String> exi = new HashSet<>(); // a set of existentials between 2 comparing existentials that derives deg.
    private HashSet<String> emb = new HashSet<>(); // a set of embeddings in embedding space that derives deg.

    public SimRecord(BigDecimal deg, HashSet<String> pri, HashSet<String> exi, HashSet<String> emb) {
        this.deg = deg;
        this.pri = pri;
        this.exi = exi;
        this.emb = emb;
    }

    public SimRecord() {
    }

    public BigDecimal getDeg() {
        return deg;
    }

    public HashSet<String> getPri() {
        return pri;
    }

    public HashSet<String> getExi() {
        return exi;
    }

    public HashSet<String> getEmb() {
        return emb;
    }

    public void setDeg(BigDecimal deg) {
        this.deg = deg;
    }

    public void appendPri(String pri) {
        this.pri.add(pri);
    }

    public void appendExi(String exi) {
        this.exi.add(exi);
    }

    public void appendEmb(String emb) {
        this.emb.add(emb);
    }

    @Override
    public String toString() {
        return "SimRecord{" +
                "deg=" + deg +
                ", pri=" + pri +
                ", exi=" + exi +
                ", emb=" + emb +
                '}';
    }
}
