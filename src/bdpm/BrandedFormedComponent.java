package bdpm;

import java.util.HashSet;
import java.util.Set;

public class BrandedFormedComponent implements IBrandName, IFormedComponent, IAddIngredient{
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
