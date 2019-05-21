package bdpm;

import java.util.HashMap;

public interface IAddDosedSpecificIngredient {
	
	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose);
	HashMap<SpecificIngredient,Dose> getSpecificIngredientDoseMap();
	
}
