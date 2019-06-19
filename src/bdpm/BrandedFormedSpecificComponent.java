package bdpm;

import java.util.HashSet;
import java.util.Set;

public class BrandedFormedSpecificComponent implements IHasUndosedSpecificIngredients, IHasForm, IHasMapKey {
	BrandName brandName = new BrandName();
	Form form = new Form();
	Set<SpecificIngredient> containedSpecificIngredients = new HashSet<SpecificIngredient>();
	BrandedFormedComponent brandedFormedComponentBelongsTo = new BrandedFormedComponent();
	
	BrandedFormedSpecificComponent(){
	}
	
	BrandedFormedSpecificComponent(Form form, Set<SpecificIngredient> containedSpecificIngredients, BrandName brandName){
		this.form=form;
		this.containedSpecificIngredients=containedSpecificIngredients;
		this.brandName=brandName;
	}

	@Override
	public String generateMapKey() {
		return Utils.hash(containedSpecificIngredients.toString()+form.getFormLabel()+brandName.toString());
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
	public void addSpecificIngredient(SpecificIngredient specificIngredient) {
		containedSpecificIngredients.add(specificIngredient); 
	}

	@Override
	public HashSet<SpecificIngredient> getSpecificIngredientSet() {
		return (HashSet<SpecificIngredient>) containedSpecificIngredients;
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
	public void writeMatchingFormedComponent(StringBuffer oneLine, String CSV_SEPARATOR) {
		if (brandedFormedComponentBelongsTo.containedIngredients.size()>0){ //avoid "empty" objects 
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(brandedFormedComponentBelongsTo.generateMapKey()); //matching branded formed component
		}
	}

}
