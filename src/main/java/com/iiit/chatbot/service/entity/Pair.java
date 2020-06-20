package com.iiit.chatbot.service.entity;

public class Pair<T,V> {

    private T firstValue_;
    private V secondValue_;

    /**
     * Constructor to initalize the Pair object with pair values.
     * @param t - First pair value
     * @param v - Second pair value
     */
    public Pair(T t, V v) {
        firstValue_ = t;
        secondValue_ = v;
    }

    /**
     * @return the firstValue
     */
    public final T getFirstValue() {
        return firstValue_;
    }

    public final void setFirstValue(T firstValue) {
        firstValue_ = firstValue;
    }

    public final V getSecondValue() {
        return secondValue_;
    }

    public final void setSecondValue(V secondValue) {
        secondValue_ = secondValue;
    }

    public final void setPairValue(T firstValue, V secondValue) {
        firstValue_ = firstValue;
        secondValue_ = secondValue;
    }

    @Override
    public String toString() {
        String s = (firstValue_ + "     " + secondValue_);
        return s;
    }

}
