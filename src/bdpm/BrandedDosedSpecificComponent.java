package bdpm;

import java.util.HashMap;
import java.util.Map;

public class BrandedDosedSpecificComponent implements IHasDosedSpecificIngredients, IHasMapKey{
	BrandName brandName = new BrandName();
	Map<SpecificIngredient, Dose> specificIngredsAndDoses = new HashMap<SpecificIngredient, Dose>();
	BrandedDosedComponent brandedDosedComponentBelongsTo = new BrandedDosedComponent();
	
	public BrandedDosedSpecificComponent(){
	}
	
	public BrandedDosedSpecificComponent(BrandName brandName, Map<SpecificIngredient, Dose> specificIngredsAndDoses) {
		super();
		this.brandName = brandName;
		this.specificIngredsAndDoses = specificIngredsAndDoses;
	}
	


	
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
		if (brandedDosedComponentBelongsTo.ingredientsAndDoses.size()>0){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(brandedDosedComponentBelongsTo.generateMapKey()); //matching overall branded dosed component 
		}
		
		
	}
	
	

	@Override
	public String generateMapKey() {
		return Utils.hash(specificIngredsAndDoses.toString() + brandName.toString());
	}

	@Override
	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose) {
		if (dose.doseLabel.length()>0){
			specificIngredsAndDoses.put(specIngredient, dose);
		}

		
	}

	@Override
	public HashMap<SpecificIngredient, Dose> getSpecificIngredientDoseMap() {
		return (HashMap<SpecificIngredient, Dose>) specificIngredsAndDoses;
	}

	



	
	

}
