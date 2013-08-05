package com.rest4j.impl.polymorphic;

/**
 * @author Joseph Kapizza <joseph@rest4j.com>
 */
public class PetMapping {

	public PetType getPetType(AbstractPet pet) {
		if (pet instanceof Cat) return PetType.cat;
		else return PetType.bird;
	}

}
