package bdpm;

import java.util.HashSet;
import java.util.Set;

public class BrandedFormedSpecificComponent implements IAddSpecIngredient, IHasForm, IHasMapKey {
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

}
