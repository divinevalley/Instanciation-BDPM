package ndc;

import java.util.HashSet;
import java.util.Set;

public class SpecificIngredient {
	String specificIngredientName;
	String codeSubstance;
	Set<String> otherNames = new HashSet<String>();
	Ingredient ingredientBelongsTo = new Ingredient();
	
	public SpecificIngredient (String codeSubstance, String name) {
		this.codeSubstance=codeSubstance;
		specificIngredientName = name;
		addSpecificIngredientName(name);
	}
	
	public SpecificIngredient() {
	}
	
	public void addSpecificIngredientName(String specificIngredientName){
		otherNames.add(specificIngredientName);
	}

	public String getCodeSubstance(){
		return codeSubstance;
	}
	
	public String toString() {
		return specificIngredientName;
	}
}
