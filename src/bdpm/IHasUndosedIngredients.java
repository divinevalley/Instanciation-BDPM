package bdpm;

import java.util.HashSet;

public interface IHasUndosedIngredients {
	//adders
	void addIngredient(Ingredient ingredient);
	
	HashSet<Ingredient> getIngredientsSet();
	
	public void loopThroughIngredients(StringBuffer oneLine, String CSV_SEPARATOR);
	
	
	public void writeMatchingFormedSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR);
	
	
}
