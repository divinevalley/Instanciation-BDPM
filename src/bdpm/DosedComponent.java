package bdpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DosedComponent implements IHasMapKey, IAddDosedIngredient  {
	 
	Map<Ingredient,Dose> ingredientsAndDoses = new HashMap<Ingredient,Dose>();
	Set<DosedSpecificComponent> matchingDosedSpecificComponents = new HashSet<DosedSpecificComponent>();
	
	Set<NonbrandedDrug> matchingNonbrandedDrugs = new HashSet<NonbrandedDrug>();
	
	//constructors
	public DosedComponent(Map<Ingredient, Dose> ingredientsWithDoses) {
		this.ingredientsAndDoses = ingredientsWithDoses;
	}
	
	public DosedComponent() {
	}
	
	//generate mapkey for ontology listing
	public String generateMapKey(){
		return Utils.hash(ingredientsAndDoses.toString());
	}
	
	//adder   (add specific ingr with dose in super class)
	public void addIngredientWithDose(Ingredient ingredient, Dose dose){
		if (dose.doseLabel.length()>0){  //only add to Map if dose has something in it
			ingredientsAndDoses.put(ingredient, dose);
		}	
	}
	

	@Override
	public HashMap<Ingredient, Dose> getIngredientDoseMap() {
		return (HashMap<Ingredient, Dose>) ingredientsAndDoses;
	}
	
	
	@Override
	public String toString() {

		String ingredWithDose = "";
		for (Map.Entry<Ingredient, Dose> entry : ingredientsAndDoses.entrySet()) {
			ingredWithDose += entry.getKey().ingredientName + " at dose "+entry.getValue() + ", ";
		}
		return "\n\n" + ingredWithDose 
				+ "\n\tMatchingDosedSpecificComponents:" + matchingDosedSpecificComponents;
	}


	
	
	
}
