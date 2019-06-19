package bdpm;

import java.util.HashMap;

public interface IHasDosedIngredients {

	public void addIngredientWithDose(Ingredient ingredient, Dose dose);
	
	HashMap<Ingredient,Dose> getIngredientDoseMap();
	
	public void loopThroughDosedIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR);
	
	public void writeMatchingSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR);

}
