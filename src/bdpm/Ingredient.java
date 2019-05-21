package bdpm;


import java.util.HashSet;
import java.util.Set;

public class Ingredient { //superclass of SpecificIngredient
	String ingredientName;
	String codeSubstance;
	Set<String> otherNames = new HashSet<String>();
	Set<SpecificIngredient> matchingSpecificIngredients = new HashSet<SpecificIngredient>();
	
	//constructor
	public Ingredient (String name) {
		ingredientName = name;
		addIngredientName(name);
	}
	
	public Ingredient (String codeSubstance, String name) {
		this.codeSubstance=codeSubstance;
		ingredientName = name;
		addIngredientName(name);
	}
	
	public Ingredient() {
	}
	
	public void setCodeSubstance(String codeSubstance){
		this.codeSubstance=codeSubstance;
	}
	
	public void addIngredientName(String ingredientName){
		otherNames.add(ingredientName);
	}

	public String getCodeSubstance(){
		return codeSubstance;
	}
	
	public String toString() {
		return ingredientName;
	}
	
}
