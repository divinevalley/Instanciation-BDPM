package bdpm;

import java.util.HashSet;
import java.util.Set;

public class FormedComponent implements IHasForm, IAddIngredient{
	Form form = new Form();
	Set<Ingredient> containedIngredients = new HashSet<Ingredient>();
	Set<FormedSpecificComponent> matchingFormedSpecificComponents = new HashSet<FormedSpecificComponent>();
	
	Set<NonbrandedDrug> matchingNonbrandedDrugs = new HashSet<NonbrandedDrug>();
	
	//constructors
	public FormedComponent(Form form, Set<Ingredient> containedIngredients) {
		this.form = form;
		this.containedIngredients = containedIngredients;
	}
	
	public FormedComponent(){
	}

	//Ontology Map key containedIngred map toString + containedSpecificIngredients map toString + formLabel no accents
	public String generateMapKey(){
		return Utils.hash(containedIngredients.toString() +form.getFormLabel());
	}
	
	//adders
	@Override
	public void addIngredient(Ingredient ingredient) {
		containedIngredients.add(ingredient);
	}
	
	@Override
	public HashSet<Ingredient> getIngredientsSet() {
		return (HashSet<Ingredient>) containedIngredients;
	}

	
	//getters and setters
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	@Override
	public String toString() {
		
		
		String ingredientsString = "";
		for (Ingredient ingred: containedIngredients) {
			ingredientsString += ingred + ", ";
		}
		
		return "\n\nIngredients: " + ingredientsString 
				+ "\nForm: " + form
				+ "\n\tMatchingFormedSpecificComponents: " + matchingFormedSpecificComponents.toString();
	}


	
	
	
	
	
	
	
	
}
