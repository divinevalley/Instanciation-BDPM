package bdpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BrandedDrug implements IHasMapKey, IHasForm, IHasUndosedSpecificIngredients, IHasUndosedIngredients, IHasDosedSpecificIngredients, IHasDosedIngredients { 
	String brandedDrugId;
	String label;
	BrandName brandName = new BrandName(); 
	BrandedDosedComponent brandedDosedComponent = new BrandedDosedComponent();
	BrandedFormedComponent brandedFormedComponent = new BrandedFormedComponent();
	BrandedDosedSpecificComponent brandedDosedSpecificComponent = new BrandedDosedSpecificComponent();
	BrandedFormedSpecificComponent brandedFormedSpecificComponent = new BrandedFormedSpecificComponent();
	Boolean doseNeedsChecking = false;
	Boolean brandNameNeedsChecking = false;
	
	// link with NonbrandedDrug
	NonbrandedDrug nonbrandedDrugBelongsTo = new NonbrandedDrug();

	//constructors

//	BrandedDrug(){
//	}

	BrandedDrug(String brandedDrugId, String label, Form form, BrandName brandName) {
		this.brandedDrugId=brandedDrugId;
		this.label=label;
		setForm(form);
		setBrandName(brandName);
	}
	

	@Override
	public Form getForm() {
		return nonbrandedDrugBelongsTo.getForm();
	}

	@Override
	public String generateMapKey() {
		return brandedDrugId;
	}

	//adders addIngredient and addSpecificIngredient
	@Override
	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose) {
		//build the dosed components. add to both nonbranded and branded components!
		nonbrandedDrugBelongsTo.dosedSpecificComponent.specificIngredsAndDoses.put(specIngredient, dose);
		brandedDosedSpecificComponent.specificIngredsAndDoses.put(specIngredient, dose); 

		//build the formed components 
		nonbrandedDrugBelongsTo.formedSpecificComponent.containedSpecificIngredients.add(specIngredient);
		brandedFormedSpecificComponent.containedSpecificIngredients.add(specIngredient);
	}


	@Override
	public HashMap<SpecificIngredient, Dose> getSpecificIngredientDoseMap() {
		return brandedDosedSpecificComponent.getSpecificIngredientDoseMap();
	}
	
	@Override
	public void addIngredientWithDose(Ingredient ingredient, Dose dose) {
		//add new ingredient to both Map and Set
		nonbrandedDrugBelongsTo.dosedComponent.ingredientsAndDoses.put(ingredient, dose); //dosed stuff
		nonbrandedDrugBelongsTo.formedComponent.containedIngredients.add(ingredient); //formed stuff

		brandedDosedComponent.ingredientsAndDoses.put(ingredient, dose); //build branded components too 
		brandedFormedComponent.addIngredient(ingredient);
	}
	

	@Override
	public HashMap<Ingredient, Dose> getIngredientDoseMap() {
		return brandedDosedComponent.getIngredientDoseMap();
	}
	

	@Override
	public void addIngredient(Ingredient ingredient) {
		nonbrandedDrugBelongsTo.formedComponent.addIngredient(ingredient);
		brandedFormedComponent.addIngredient(ingredient);
	}
	
	@Override
	public HashSet<Ingredient> getIngredientsSet() {
		return (HashSet<Ingredient>) brandedFormedComponent.containedIngredients;
	}

	
	
	@Override
	public void addSpecificIngredient(SpecificIngredient specificIngredient) {
		nonbrandedDrugBelongsTo.formedSpecificComponent.addSpecificIngredient(specificIngredient);	
		brandedFormedSpecificComponent.addSpecificIngredient(specificIngredient);
	}
	

	@Override
	public HashSet<SpecificIngredient> getSpecificIngredientSet() {
		return brandedFormedSpecificComponent.getSpecificIngredientSet();
	}

	//setters
	public void setBrandName(BrandName brandName){
		this.brandName=brandName;
		brandedDosedComponent.brandName=brandName;
		brandedFormedComponent.brandName=brandName;
		brandedDosedSpecificComponent.brandName=brandName;
		brandedFormedSpecificComponent.brandName=brandName;
	}

	public void setDrugId(String brandedDrugId) {
		this.brandedDrugId=brandedDrugId;
	}

	public void setLabel(String label){
		this.label=label;
	}

	public void setForm(Form form){
		nonbrandedDrugBelongsTo.formedComponent.form=form;
		nonbrandedDrugBelongsTo.formedSpecificComponent.form=form;
		brandedFormedComponent.setForm(form);
		brandedFormedSpecificComponent.form=form;
	}

	


	@Override
	public void loopThroughDosedIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR) {
		brandedDosedComponent.loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
	}

	
	public void writeMatchingFormedSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR){
		brandedFormedComponent.writeMatchingFormedSpecificComponents(oneLine, CSV_SEPARATOR);
	}
	
	@Override
	public void loopThroughIngredients(StringBuffer oneLine, String CSV_SEPARATOR) {
		brandedFormedComponent.loopThroughIngredients(oneLine, CSV_SEPARATOR);
	}
	
	@Override
	public void loopThroughDosedSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR) {
		brandedDosedSpecificComponent.loopThroughDosedSpecificIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
	}
	
	@Override
	public void loopThroughSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR) {
		brandedFormedSpecificComponent.loopThroughSpecificIngredients(oneLine, CSV_SEPARATOR);
	}
	
	@Override
	public void writeMatchingSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR) {
		brandedDosedComponent.writeMatchingSpecificComponents(oneLine, CSV_SEPARATOR);
	}


	@Override
	public void writeMatchingDosedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		brandedDosedSpecificComponent.writeMatchingDosedComponent(oneLine, CSV_SEPARATOR);
	}


	@Override
	public void writeMatchingFormedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		brandedFormedSpecificComponent.writeMatchingFormedComponent(oneLine, CSV_SEPARATOR);
	}


	



	@Override
	public String toString() {
		String ingredientsString = "";
		for (Map.Entry<Ingredient, Dose> entry : nonbrandedDrugBelongsTo.dosedComponent.ingredientsAndDoses.entrySet()) {
			ingredientsString += entry.getKey() + " at dose "+entry.getValue() + ", ";
		}
		String specificIngredientsString ="";
		for (Map.Entry<SpecificIngredient, Dose> entry : nonbrandedDrugBelongsTo.dosedSpecificComponent.specificIngredsAndDoses.entrySet()) {
			specificIngredientsString += entry.getKey() + " at dose " + entry.getValue() + ", ";
		}

		String formedSpecIngredientsString = "";
		for (SpecificIngredient specIngred: nonbrandedDrugBelongsTo.formedSpecificComponent.containedSpecificIngredients) {
			formedSpecIngredientsString += specIngred + ", ";
		}

		String brandedForm = "";
		for (Ingredient ingred: brandedFormedComponent.containedIngredients) {
			brandedForm += ingred + ", ";
		}

		String formedIngredientsString = "";
		for (Ingredient ingred: nonbrandedDrugBelongsTo.formedComponent.containedIngredients) {
			formedIngredientsString += ingred + ", ";
		}
		return "\nIngredients & doses:\t " + ingredientsString 
				+ "\nSpecificIngredients & doses:\t" + specificIngredientsString
				+ "\n*FormedSpecificComponent: SpecificIngredients:\t" + formedSpecIngredientsString
				+ "\n\t\tForm: \t" + nonbrandedDrugBelongsTo.formedSpecificComponent.form.toString()
				+ "\n*FormedComponent: Ingredients: \t" + formedIngredientsString
				+ "\n\t\tForm: \t" + nonbrandedDrugBelongsTo.formedComponent.form.toString()
				+ "\n---\nBrandedFormedComponent: Ingredients: " + brandedForm
				+ "\nBrandedFormedComponent Brand: " + brandedFormedComponent.brandName
				+ "\nForm:\t" + nonbrandedDrugBelongsTo.formedComponent.form.toString() 
				+ "\nBrandName:\t" + brandName 
				+ "\nORIGINAL LABEL: " + label+"\n\n";
	}







	



}
