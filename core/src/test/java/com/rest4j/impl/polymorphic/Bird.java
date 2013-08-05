package com.rest4j.impl.polymorphic;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Bird extends AbstractPet {
	double beakStrength; // in Watts

	public double getBeakStrength() {
		return beakStrength;
	}

	public void setBeakStrength(double beakStrength) {
		this.beakStrength = beakStrength;
	}
}
