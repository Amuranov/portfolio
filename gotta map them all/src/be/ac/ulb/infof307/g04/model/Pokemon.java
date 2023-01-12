package be.ac.ulb.infof307.g04.model;

import java.io.Serializable;


/**
 * COntains all the informations concerning a pokemon
 * Implements Serializable because some pokemon objects will be saved in a extern file 
 *
 */
public class Pokemon implements Serializable {
	
	private int id;
	private String name;
	private int type1;
	private int type2;
	private String stringType1;
	private String stringType2;
	private int life;
	private int attack;
	private int defense;
	private int attackSpecial;
	private int defenseSpecial;
	private int speed;
	private String imageMiniature;
	private String imageBig;
	
	public Pokemon(){ //Need for jersey
	
	}


	public Pokemon(int id, String name, int type1, int type2, int life, 
				   int attack, int defense, int attackSpecial,
				   int defenseSpecial, int speed, String imageMiniature, String imageBig, String stringType1, String stringType2) 
	{
		this.id = id;
		this.name = name;
		this.type1 = type1;
		this.type2 = type2;
		this.stringType1 = stringType1;
		this.stringType2 = stringType2;
		this.life = life;
		this.attack = attack;
		this.defense = defense;
		this.attackSpecial = attackSpecial;
		this.defenseSpecial = defenseSpecial;
		this.speed = speed;
		this.imageMiniature = imageMiniature;
		this.imageBig = imageBig;
	}
	
	public Pokemon(int id, String name, String stringType1, String stringType2) {
		this.id = id;
		this.name = name;
		this.stringType1 = stringType1;
		this.stringType2 = stringType2;
	}

	public String getStringType1() {
		return stringType1;
	}

	public void setStringType1(String stringType1) {
		this.stringType1 = stringType1;
	}

	public String getStringType2() {
			return stringType2;
	}

	public void setStringType2(String stringType2) {
		this.stringType2 = stringType2;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getType1() {
		return type1;
	}

	public void setType1(int type1) {
		this.type1 = type1;
	}

	public int getType2() {
		return type2;
	}
	
	public void setType2(int type2) {
		this.type2 = type2;
	}
	
	public int getAttack() {
		return attack;
	}
	
	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}
	
	public int getDefense() {
		return defense;
	}
	
	public void setDefense(int defense) {
		this.defense = defense;
	}
	
	public int getAttackSpecial() {
		return attackSpecial;
	}
	
	public void setAttackSpecial(int attackSpecial) {
		this.attackSpecial = attackSpecial;
	}
	
	public int getDefenseSpecial() {
		return defenseSpecial;
	}

	public void setDefenseSpecial(int defenseSpecial) {
		this.defenseSpecial = defenseSpecial;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public String getImageMiniature() {
		return imageMiniature;
	}
	
	public void setImageMiniature(String imageMiniature) {
		this.imageMiniature = imageMiniature;
	}
	
	public String getImageBig() {
		return imageBig;
	}
	
	public void setImageBig(String imageBig) {
		this.imageBig = imageBig;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Pokemon && this.name.equals(((Pokemon) obj).name);
	}

	@Override
	public String toString() {
		return name;
	}
}
