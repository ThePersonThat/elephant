package edu.sumdu.tss.elephant.helper;

public class Pair<T1, T2> {
    public T1 key;
    public T2 value;

    public Pair() {
    }

    public Pair(T1 key, T2 value) {
        this.key = key;
        this.value = value;
    }
}
