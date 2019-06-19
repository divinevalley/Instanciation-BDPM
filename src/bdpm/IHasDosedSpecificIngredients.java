package bdpm;

import java.util.HashMap;

public interface IHasDosedSpecificIngredients {
	
	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose);
	
	HashMap<SpecificIngredient,Dose> getSpecificIngredientDoseMap();
	
	public void loopThroughDosedSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR);
	
	public void writeMatchingDosedComponent(StringBuffer oneLine, String CSV_SEPARATOR);

	
}
