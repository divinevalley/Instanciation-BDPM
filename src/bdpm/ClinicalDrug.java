package bdpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClinicalDrug implements IHasMapKey, IHasForm, IHasUndosedSpecificIngredients, IHasUndosedIngredients, IHasDosedSpecificIngredients, IHasDosedIngredients { 
	FormedComponent formedComponent = new FormedComponent(); //for multiple inheritance, FormedComponent is like another superclass of NonBrandedDrug 
	DosedComponent dosedComponent = new DosedComponent(); //this object will contain the map of DosedIngredients
	DosedSpecificComponent dosedSpecificComponent = new DosedSpecificComponent();
	FormedSpecificComponent formedSpecificComponent = new FormedSpecificComponent();
	
	//connection to BrandedDrug
	Set<BrandedDrug> matchingBrandedDrugs = new HashSet<BrandedDrug>();

	//constructors
	public ClinicalDrug(){
	}

	public ClinicalDrug(Form form, Map<Ingredient, Dose> ingredientsDoses, 
			Map<SpecificIngredient, Dose> specificIngredientsDoses, 
			Set<Ingredient> ingredientsSet, 
			Set<SpecificIngredient> specificIngredientsSet) {
		setForm(form);
		formedComponent.containedIngredients=ingredientsSet;
		formedSpecificComponent.containedSpecificIngredients=specificIngredientsSet;
		dosedComponent.ingredientsAndDoses=ingredientsDoses;
		dosedSpecificComponent.specificIngredsAndDoses = specificIngredientsDoses;
	}

	public String generateMapKey(){  //form + IngredientsAndDoses + specificIngredientsAndDoses
		return Utils.hash(getForm().toString()+dosedComponent.ingredientsAndDoses.toString()+dosedSpecificComponent.specificIngredsAndDoses.toString());
	}

	
	@Override
	public Form getForm() {
		return formedComponent.form;
	}

	@Override
	public void setForm(Form form) {
		formedComponent.form=form;
		formedSpecificComponent.form=form;
	}


	//adders
	@Override
	public void addSpecIngredientWithDose(SpecificIngredient specIngredient, Dose dose) {
		dosedSpecificComponent.specificIngredsAndDoses.put(specIngredient, dose);
		formedSpecificComponent.containedSpecificIngredients.add(specIngredient);
	}
	
	@Override
	public HashMap<SpecificIngredient, Dose> getSpecificIngredientDoseMap() {
		return dosedSpecificComponent.getSpecificIngredientDoseMap();
	}

	@Override
	public void addIngredientWithDose(Ingredient ingredient, Dose dose) {
		//add new ingredient to both Map and Set
		dosedComponent.ingredientsAndDoses.put(ingredient, dose);
		formedComponent.containedIngredients.add(ingredient);
	}
	
	@Override
	public void addSpecificIngredient(SpecificIngredient specificIngredient) {
		formedSpecificComponent.addSpecificIngredient(specificIngredient);
	}
	

	@Override
	public void addIngredient(Ingredient ingredient) {
		formedComponent.addIngredient(ingredient);
	}
	
	@Override
	public HashSet<Ingredient> getIngredientsSet() {
		return formedComponent.getIngredientsSet();
	}

	@Override
	public HashSet<SpecificIngredient> getSpecificIngredientSet() {
		return formedSpecificComponent.getSpecificIngredientSet();
	}
	
	@Override
	public HashMap<Ingredient, Dose> getIngredientDoseMap() {
		return (HashMap<Ingredient, Dose>) dosedComponent.ingredientsAndDoses;
	}

	

	

	@Override
	public void loopThroughDosedIngredients(StringBuffer oneLine, String CSV_SEPARATOR, String CSV_DOSE_SEPARATOR) {
		dosedComponent.loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
	}
	

	@Override
	public void loopThroughIngredients(StringBuffer oneLine, String CSV_SEPARATOR) {
		formedComponent.loopThroughIngredients(oneLine, CSV_SEPARATOR);
	}
	
	
	@Override
	public void loopThroughDosedSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR,
			String CSV_DOSE_SEPARATOR) {
		dosedSpecificComponent.loopThroughDosedSpecificIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
	}
	
	
	@Override
	public void loopThroughSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR) {
		formedSpecificComponent.loopThroughSpecificIngredients(oneLine, CSV_SEPARATOR);
	}
	


	@Override
	public void writeMatchingFormedSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR) {
		formedComponent.writeMatchingFormedSpecificComponents(oneLine, CSV_SEPARATOR);	
	}
	
	@Override
	public void writeMatchingSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR) {
		dosedComponent.writeMatchingSpecificComponents(oneLine, CSV_SEPARATOR);
	}
	
	@Override
	public void writeMatchingDosedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		dosedSpecificComponent.writeMatchingDosedComponent(oneLine, CSV_SEPARATOR);
	}

	@Override
	public void writeMatchingFormedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		formedSpecificComponent.writeMatchingFormedComponent(oneLine, CSV_SEPARATOR);
	}

		
	
	public String toString() {
		String ingredientsString = "";
		for (Map.Entry<Ingredient, Dose> entry : dosedComponent.ingredientsAndDoses.entrySet()) {
			ingredientsString += entry.getKey() + " at dose "+entry.getValue() + ", ";
		}
		String specificIngredientsString ="";
		for (Map.Entry<SpecificIngredient, Dose> entry : dosedSpecificComponent.specificIngredsAndDoses.entrySet()) {
			specificIngredientsString += entry.getKey() + " at dose " + entry.getValue() + ", ";
		}
		
		String formedSpecIngredientsString = "";
		for (SpecificIngredient specIngred: formedSpecificComponent.containedSpecificIngredients) {
			formedSpecIngredientsString += specIngred + ", ";
		}

		String formedIngredientsString = "";
		for (Ingredient ingred: formedComponent.containedIngredients) {
			formedIngredientsString += ingred + ", ";
		}


		return "\nIngredients & doses:\t " + ingredientsString 
				+ "\nSpecificIngredients & doses:\t" + specificIngredientsString
				+ "\n*Formed SpecificComponent: SpecificIngredients:\t" + formedSpecIngredientsString
				+ "\n\t\tForm: \t" + formedSpecificComponent.form.toString()
				+ "\n*FormedComponent: Ingredients: \t" + formedIngredientsString
				+ "\n\t\tForm: \t" + formedComponent.form.toString()
				+ "\nForm:\t" + formedComponent.form.toString() + "\n\n";

	}

	
	

	

	

}

	

