package bdpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BrandedDosedComponent implements IBrandName, IHasMapKey, IHasDosedIngredients {

	BrandName brandName = new BrandName();
	Map<Ingredient,Dose> ingredientsAndDoses = new HashMap<Ingredient,Dose>();
	Set<BrandedDosedSpecificComponent> matchingBrandedDosedSpecificComponents = new HashSet<BrandedDosedSpecificComponent>();

	Set<BrandedDrug> matchingBrandedDrugs = new HashSet<BrandedDrug>();
	
	public String generateMapKey() {
		return Utils.hash(ingredientsAndDoses.toString() + brandName.toString());
	}
	
	//constructors 
	BrandedDosedComponent(){	
	}

	public BrandedDosedComponent(Map<Ingredient, Dose> ingredientsAndDoses, 
			Map<SpecificIngredient, Dose> specificIngredsAndDoses, BrandName brandName) {
		super();
		this.ingredientsAndDoses = ingredientsAndDoses;
		this.brandName=brandName;
	}


	//adder
	public void addIngredientWithDose(Ingredient ingredient, Dose dose){
		if (dose.doseLabel.length()>0){
			this.ingredientsAndDoses.put(ingredient, dose);			
		}

	}
	

	public void loopThroughDosedIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR){
		for (Map.Entry<Ingredient, Dose> dosedComponentEntry : ingredientsAndDoses.entrySet()){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(ingredientsAndDoses.size()); //number of dosed ingredients
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(dosedComponentEntry.getKey().ingredientName.toString().length()==0? "" : dosedComponentEntry.getKey().ingredientName.toString()); //ingredient
			oneLine.append(CSV_DOSE_SEPARATOR);
			oneLine.append(dosedComponentEntry.getValue().doseNumber.length()==0? "" : dosedComponentEntry.getValue().doseNumber); //dose
			oneLine.append(CSV_DOSE_SEPARATOR);
			oneLine.append(dosedComponentEntry.getValue().unit.unitLabel.length()==0? "" : dosedComponentEntry.getValue().unit.unitLabel); //unit
		}
	}
	
	@Override
	public void writeMatchingSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR) {
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(matchingBrandedDosedSpecificComponents.size());  //number of Branded DosedSpecificComponents
		
		for (BrandedDosedSpecificComponent matchingComponent : matchingBrandedDosedSpecificComponents){
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(matchingComponent.generateMapKey());
		}
	}

	
	@Override
	public HashMap<Ingredient, Dose> getIngredientDoseMap() {
		return (HashMap<Ingredient, Dose>) ingredientsAndDoses;
	}


	@Override
	public BrandName getBrandName() {
		return brandName;
	}

	@Override
	public void setBrandName(BrandName brandName) {
		this.brandName=brandName;
	}


	@Override
	public String toString() {

		
		String ingredWithDose = "";
		for (Map.Entry<Ingredient, Dose> entry : ingredientsAndDoses.entrySet()) {
			ingredWithDose += entry.getKey() + " at dose "+entry.getValue() + ", ";
		}
		
		return "\n\nIngredients with Doses: " + ingredWithDose 
				+ "\n BrandName: " + brandName.toString();
	}

	
	

}
