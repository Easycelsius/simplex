package com;

import java.util.Vector;

public class Var {
    private final Vector<Double> vector = new Vector<Double>();

    // a + bM + cM^2 + ... + kM^n
    public Var(double... values) {
        for (int i = 0; i < values.length; i++) {
            vector.add(values[i]);
        }
    }

    public Var add(final Var var) {
        Vector<Double> longerVector = vector.size() >= var.vector.size() ? vector : var.vector;
        Vector<Double> shorterVector = vector.size() < var.vector.size() ? vector : var.vector;
        double[] newValues = new double[longerVector.size()];

        for (int i = 0; i < shorterVector.size(); i++) {
            newValues[i] = shorterVector.get(i) + longerVector.get(i);
        }

        for (int i = shorterVector.size(); i < longerVector.size(); i++) {
            newValues[i] = longerVector.get(i);
        }

        return new Var(newValues);
    }

    public Var mul(final Var var) {
        double[] newValues = new double[vector.size() * var.vector.size()];

        for (int i = 0; i < vector.size(); i++) {
            for (int j = 0; j < var.vector.size(); j++) {
                newValues[i + j] += vector.get(i) * var.vector.get(j);
            }
        }

        return new Var(newValues);
    }

    public double getValue() {
        double value = 0;
        for (int i = 0; i < vector.size(); i++) {
            if (i == 0) {
                value += vector.get(i);
            } else {
                value += vector.get(i) * Double.POSITIVE_INFINITY * i;
            }
        }
        return value;
    }

    public int getDegree() {
        return vector.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.size(); i++) {
            if (i == 0) {
                sb.append(vector.get(i));
            } else if (i == 1) {
                if (vector.get(i) < 0) {
                    sb.append(" - " + Math.abs(vector.get(i)) + "M");
                } else {
                    sb.append(" + " + Math.abs(vector.get(i)) + "M");
                }
            } else {
                if (vector.get(i) < 0) {
                    sb.append(" - " + Math.abs(vector.get(i)) + "M^" + i);
                } else {
                    sb.append(" + " + Math.abs(vector.get(i)) + "M^" + i);
                }
            }
        }
        return sb.toString();
    }
}
