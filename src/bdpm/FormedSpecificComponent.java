package bdpm;

import java.util.HashSet;
import java.util.Set;

public class FormedSpecificComponent implements IHasUndosedSpecificIngredients, IHasForm, IHasMapKey {
	
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
	public void writeMatchingFormedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		if (formedComponentBelongsTo.containedIngredients.size()>0){ //avoid generating mapkey for a meaningless object
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(formedComponentBelongsTo.generateMapKey()); 
		}
	}
	
	@Override
	public void loopThroughSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR) {
		oneLine.append(CSV_SEPARATOR);
		oneLine.append(containedSpecificIngredients.size()); //number of ingredients in this component 

		for (SpecificIngredient specificIngredient : containedSpecificIngredients){ //print all specific ingredients in the hashset
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(specificIngredient.specificIngredientName.toString().length()==0? "" : specificIngredient.specificIngredientName.toString());
		}
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
