package com.mzom.xtraqueur;

import java.io.Serializable;

// TODO: Implement hashCode() since where are implementing equals()
class XTaskFields implements Serializable {

    // Task display name
    private String name;

    // Task fee for every completion
    private double fee;

    // Task color used in the app UI
    private int color;


    XTaskFields(String name, double fee, int color) {

        this.name = name;
        this.fee = fee;
        this.color = color;

    }


    String getName() {

        return this.name;

    }

    double getFee() {

        return this.fee;

    }

    int getColor() {

        return this.color;

    }


    void setName(String name) {
        this.name = name;
    }

    void setFee(double fee) {
        this.fee = fee;
    }

    void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {

        return obj.getClass() == getClass() && XTaskFieldsUtilities.areEqual(this, obj);

    }
}
