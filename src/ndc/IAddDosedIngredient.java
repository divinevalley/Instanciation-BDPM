package ndc;

import java.util.HashMap;

public interface IAddDosedIngredient {

	public void addIngredientWithDose(Ingredient ingredient, Dose dose);
	
	HashMap<Ingredient,Dose> getIngredientDoseMap();


}
