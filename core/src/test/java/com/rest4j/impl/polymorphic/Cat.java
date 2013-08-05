package com.rest4j.impl.polymorphic;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class Cat extends AbstractPet {
	boolean longFur;

	public boolean isLongFur() {
		return longFur;
	}

	public void setLongFur(boolean longFur) {
		this.longFur = longFur;
	}
}
