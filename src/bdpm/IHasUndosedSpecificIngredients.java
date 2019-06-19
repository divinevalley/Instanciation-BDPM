package bdpm;

import java.util.HashSet;

public interface IHasUndosedSpecificIngredients {
		//adders
		void addSpecificIngredient(SpecificIngredient specificIngredient);
		
		HashSet<SpecificIngredient> getSpecificIngredientSet();
		
		public void loopThroughSpecificIngredients(StringBuffer oneLine, String CSV_SEPARATOR);


		public void writeMatchingFormedComponent(StringBuffer oneLine, String CSV_SEPARATOR);
		
		
		
		
		
}
