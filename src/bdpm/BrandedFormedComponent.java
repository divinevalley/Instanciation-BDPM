package bdpm;

import java.util.HashSet;
import java.util.Set;

public class BrandedFormedComponent implements IBrandName, IHasForm, IHasUndosedIngredients{
	Form form = new Form();
	Set<Ingredient> containedIngredients = new HashSet<Ingredient>();
	Set<BrandedFormedSpecificComponent> matchingBrandedFormedSpecificComponents = new HashSet<BrandedFormedSpecificComponent>();
	BrandName brandName = new BrandName();

	Set<BrandedDrug> matchingBrandedDrugs = new HashSet<BrandedDrug>();
	
	public String generateMapKey(){
		return Utils.hash(containedIngredients.toString() + form.toString() + brandName.toString());
	}
	
	//constructors
	BrandedFormedComponent(){
	}
	
	BrandedFormedComponent(Form form, Set<Ingredient> containedIngredients, BrandName brandName){
		this.form=form;
		this.containedIngredients=containedIngredients;
		this.brandName=brandName;
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
	public void addIngredient(Ingredient ingredient) {
		containedIngredients.add(ingredient);
	}
	
	@Override
	public HashSet<Ingredient> getIngredientsSet() {
		return (HashSet<Ingredient>) containedIngredients;
	}

	@Override
	public Form getForm() {
		return form;
	}

	@Override
	public void setForm(Form form) {
		this.form=form;
	}

	public void loopThroughIngredients(StringBuffer oneLine, String CSV_SEPARATOR){
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(containedIngredients.size()); //number of ingredients in this component 
		//go through ingredients 
		for (Ingredient ingredient : containedIngredients){ //print all ingredients in the hashset
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(ingredient.ingredientName.toString().length()==0? "" : ingredient.ingredientName.toString());
		}
	}

	
	public void writeMatchingFormedSpecificComponents(StringBuffer oneLine, String CSV_SEPARATOR){
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(matchingBrandedFormedSpecificComponents.size()); //nb matching FormedSpecificComponents
		//go through matching FormedSpecificComponents
		for (BrandedFormedSpecificComponent matchingBrandedFormedSpecificComponent : matchingBrandedFormedSpecificComponents){
			if (matchingBrandedFormedSpecificComponent.containedSpecificIngredients.size()>0){ // avoid putting "empty" objects
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(matchingBrandedFormedSpecificComponent.generateMapKey()); //print matching FormedSpecificComponent mapkey
			}
		}
	}


	@Override
	public String toString() {
		
		String ingredientsString = "";
		for (Ingredient ingred: containedIngredients) {
			ingredientsString += ingred + ", ";
		}
		
		return "\n\nIngredients: " + ingredientsString 
				+ "\nForm: " + form 
				+ "\nBrandName: "+ brandName.toString();
	}

	




}
