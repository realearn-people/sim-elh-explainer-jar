package sim.explainer.library.framework.explainer;

import sim.explainer.library.util.utilstructure.SymmetricPair;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SimRecord {
    private BigDecimal deg = new BigDecimal(0.0); // homomorphism degree
    private HashSet<SymmetricPair<String>> pri = new HashSet<>(); // a set of primitives between 2 comparing concepts that derives deg.
    private HashSet<SymmetricPair<String>> exi = new HashSet<>(); // a set of existentials between 2 comparing existentials that derives deg.
    private HashMap<SymmetricPair<String>, Set<SymmetricPair<String>>> emb = new HashMap<>(); // a set of embeddings in embedding space that derives deg.

    public SimRecord() {
    }

    public BigDecimal getDeg() {
        return deg;
    }

    public HashSet<SymmetricPair<String>> getPri() {
        return pri;
    }

    public HashSet<SymmetricPair<String>> getExi() {
        return exi;
    }

    public HashMap<SymmetricPair<String>, Set<SymmetricPair<String>>> getEmb() {
        return emb;
    }

    public void setDeg(BigDecimal deg) {
        this.deg = deg;
    }

    public void appendPri(String pri1, String pri2) {
        SymmetricPair<String> pair = new SymmetricPair<>(pri1, pri2);
        this.pri.add(pair);
    }

    public void appendExi(String exi1, String exi2) {
        SymmetricPair<String> pair = new SymmetricPair<>(exi1, exi2);
        this.exi.add(pair);
    }

    public void appendEmb(String name1, String name2, String value1, String value2) {
        SymmetricPair<String> pairName = new SymmetricPair<>(name1, name2);
        SymmetricPair<String> pairEmb = new SymmetricPair<>(value1, value2);

        Set<SymmetricPair<String>> embeddings = emb.getOrDefault(pairName, new HashSet<>());
        embeddings.add(pairEmb);
        emb.put(pairName, embeddings);
    }

    public void appendEmb(String name1, String name2, HashSet<SymmetricPair<String>> values) {
        SymmetricPair<String> pairName = new SymmetricPair<>(name1, name2);

        Set<SymmetricPair<String>> embeddings = emb.getOrDefault(pairName, new HashSet<>());
        embeddings.addAll(values);
        emb.put(pairName, embeddings);
    }

    public void setEmb(HashMap<SymmetricPair<String>, Set<SymmetricPair<String>>> emb) {
        this.emb = emb;
    }

    @Override
    public String toString() {
        return String.format("SimRecord{deg=%s, pri=%s, exi=%s, emb=%s}",
                deg, pri, exi, emb);
    }
}
