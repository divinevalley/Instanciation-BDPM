package bdpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DosedComponent implements IHasMapKey, IHasDosedIngredients  {
	 
	Map<Ingredient,Dose> ingredientsAndDoses = new HashMap<Ingredient,Dose>();
	Set<DosedSpecificComponent> matchingDosedSpecificComponents = new HashSet<DosedSpecificComponent>();
	
	Set<ClinicalDrug> matchingNonbrandedDrugs = new HashSet<ClinicalDrug>();
	
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
	
	public void loopThroughDosedIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR){
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(ingredientsAndDoses.size()); //number of dosed ingredients
		for (Map.Entry<Ingredient, Dose> dosedComponentEntry : ingredientsAndDoses.entrySet()){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(dosedComponentEntry.getKey().ingredientName.toString().length()==0? "" : dosedComponentEntry.getKey().ingredientName.toString()); //ingredient
			oneLine.append(CSV_DOSE_SEPARATOR);
			oneLine.append(dosedComponentEntry.getValue().doseNumber.length()==0? "" : dosedComponentEntry.getValue().doseNumber); //dose
			oneLine.append(CSV_DOSE_SEPARATOR);
			oneLine.append(dosedComponentEntry.getValue().unit.unitLabel.length()==0? "" : dosedComponentEntry.getValue().unit.unitLabel); //unit
		}
	}
	

	
	public void writeMatchingSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR){
		for (DosedSpecificComponent matchingDosedSpecificComponent : matchingDosedSpecificComponents){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(matchingDosedSpecificComponent.generateMapKey());  //print matching DosedSpecificComponent's mapkey
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
