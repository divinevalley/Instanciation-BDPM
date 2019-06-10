package ndc;

import java.util.HashSet;

public interface IAddSpecIngredient {
		//adders
		void addSpecificIngredient(SpecificIngredient specificIngredient);
		
		HashSet<SpecificIngredient> getSpecificIngredientSet();
		
}
