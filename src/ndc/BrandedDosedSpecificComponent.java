package ndc;

import java.util.HashMap;
import java.util.Map;

public class BrandedDosedSpecificComponent implements IAddDosedSpecificIngredient, IHasMapKey{
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
