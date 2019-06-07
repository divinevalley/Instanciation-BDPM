package bdpm;

import java.util.HashMap;
import java.util.Map;

public class DosedSpecificComponent implements IAddDosedSpecificIngredient, IHasMapKey {
	
	Map<SpecificIngredient, Dose> specificIngredsAndDoses = new HashMap<SpecificIngredient, Dose>();
	DosedComponent dosedComponentBelongsTo = new DosedComponent();
	
	public DosedSpecificComponent(){
	}
	
	public DosedSpecificComponent(Map<SpecificIngredient, Dose> specificIngredDoseMap) {
		specificIngredsAndDoses = specificIngredDoseMap;
	}

	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose){
		if (dose.doseLabel.length()>0){
			specificIngredsAndDoses.put(specIngredient, dose);			
		}

	}
	
	@Override
	public HashMap<SpecificIngredient, Dose> getSpecificIngredientDoseMap() {
		return (HashMap<SpecificIngredient, Dose>) specificIngredsAndDoses;
	}

	@Override
	public String generateMapKey() {
		return Utils.hash(specificIngredsAndDoses.toString());
	}
	
	@Override
	public String toString() {

		String spIngredWithDose = "";
		for (Map.Entry<SpecificIngredient, Dose> entry : specificIngredsAndDoses.entrySet()) {
			spIngredWithDose += entry.getKey() + " at dose "+entry.getValue() + ", ";
		}
		return "\n"+ spIngredWithDose + "\n\n";
	}

	


	
}
