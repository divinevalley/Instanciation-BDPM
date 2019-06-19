package bdpm;

import java.util.HashMap;
import java.util.Map;

public class DosedSpecificComponent implements IHasDosedSpecificIngredients, IHasMapKey {
	
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
	public void loopThroughDosedSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR) {
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(specificIngredsAndDoses.size()); //number of dosed specific ingredients
		for (Map.Entry<SpecificIngredient, Dose> dosedSpecificComponentEntry : specificIngredsAndDoses.entrySet()){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(dosedSpecificComponentEntry.getKey().specificIngredientName.toString().length()==0? "" : dosedSpecificComponentEntry.getKey().specificIngredientName.toString()); //specific ingredient name
			oneLine.append(CSV_DOSE_SEPARATOR);
			oneLine.append(dosedSpecificComponentEntry.getValue().toString().length()==0? "" : dosedSpecificComponentEntry.getValue().toString()); //dose
		}
		
	}
	
	@Override
	public void writeMatchingDosedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		if (dosedComponentBelongsTo.ingredientsAndDoses.size() > 0){  //avoid generating a hashid for an meaningless object (empty map)
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(dosedComponentBelongsTo.generateMapKey());
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
