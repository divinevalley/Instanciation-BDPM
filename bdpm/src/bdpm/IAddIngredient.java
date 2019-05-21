package bdpm;

import java.util.HashSet;

public interface IAddIngredient {
	//adders
	void addIngredient(Ingredient ingredient);
	
	HashSet<Ingredient> getIngredientsSet();
			
}
