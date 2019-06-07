package bdpm;

import java.util.HashSet;
import java.util.Set;

public class FormedSpecificComponent implements IAddSpecIngredient, IHasForm, IHasMapKey {
	
	Form form = new Form();
	Set<SpecificIngredient> containedSpecificIngredients = new HashSet<SpecificIngredient>();
	FormedComponent formedComponentBelongsTo = new FormedComponent();
	
	public FormedSpecificComponent() {
	}

	
	public FormedSpecificComponent(Form form, Set<SpecificIngredient> containedSpecIngredients) {
		containedSpecificIngredients = containedSpecIngredients;
		this.form=form;
	}

	@Override
	public void addSpecificIngredient(SpecificIngredient specificIngredient) {
		containedSpecificIngredients.add(specificIngredient); 
	}
	
	@Override
	public HashSet<SpecificIngredient> getSpecificIngredientSet() {
		return (HashSet<SpecificIngredient>) containedSpecificIngredients;
	}


	@Override
	public String generateMapKey() {
		return Utils.hash(containedSpecificIngredients.toString()+form.getFormLabel());
	}

	@Override
	public Form getForm() {
		return form;
	}

	@Override
	public void setForm(Form form) {
		this.form = form;
	}
	
	@Override
	public String toString() {
		
		
		String specIngredientsString = "";
		for (SpecificIngredient ingred: containedSpecificIngredients) {
			specIngredientsString += ingred + ", ";
		}
		
		return "\n\nIngredients: " + specIngredientsString 
				+ "\nForm: " + form;
	}


	
}
