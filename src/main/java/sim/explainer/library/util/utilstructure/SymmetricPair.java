package sim.explainer.library.util.utilstructure;

import java.util.Objects;

public class SymmetricPair<T> {
    private T first;
    private T second;

    public SymmetricPair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymmetricPair<?> that = (SymmetricPair<?>) o;
        return (Objects.equals(first, that.first) && Objects.equals(second, that.second)) ||
                (Objects.equals(first, that.second) && Objects.equals(second, that.first));
    }

    public boolean equalsOrder(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymmetricPair<?> that = (SymmetricPair<?>) o;
        return (Objects.equals(first, that.first) && Objects.equals(second, that.second));
    }

    @Override
    public int hashCode() {
        return Objects.hash(first) + Objects.hash(second); // Order-independent hash code
    }

    @Override
    public String toString() {
        return "(" + first +
                ", " + second +
                ')';
    }
}