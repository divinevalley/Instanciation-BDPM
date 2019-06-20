package bdpm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Ontology {
	//building blocks of drugs 
	static Map<String, Form> formMap = new HashMap<String,Form>(); //form label  as key
	static Map<String, Dose> doseMap = new HashMap<String,Dose>(); //dose label  as key
	static Map<String, Ingredient> ingredientMap = new HashMap<String,Ingredient>(); //CodeSubstance as key
	static Map<String, SpecificIngredient> specificIngredientMap = new HashMap<String,SpecificIngredient>(); //CodeSubstance as key
	static Map<String, BrandName> brandNameMap = new HashMap<String,BrandName>(); //branad name as key

	//Drugs - branded and non branded
	static Map<String, ClinicalDrug> clinicalDrugMap = new HashMap<String, ClinicalDrug>(); //form+DosedIngredMap+DosedSpecIngredMap toString & hasehd as key
	static Map<String, BrandedDrug> brandedDrugMap = new HashMap<String, BrandedDrug>(); //brandedDrugId as key

	//Clinical Drug components (more general types of drugs)
	static Map<String, DosedComponent> dosedComponentMap = new HashMap<String, DosedComponent>(); //ingredients toString +doseLabel hashed as key
	static Map<String, FormedComponent> formedComponentMap = new HashMap<String, FormedComponent>(); //ingredients toString +formLabel & hashed as key
	
	static Map<String, FormedSpecificComponent> formedSpecificComponentMap = new HashMap<String, FormedSpecificComponent>(); //specificingredients toString +formLabel & hashed as key
	static Map<String, DosedSpecificComponent> dosedSpecificComponentMap = new HashMap<String, DosedSpecificComponent>(); //specificingredients toString +doseLabel & hashed as key
	
	//Branded Drug Components 
	static Map <String, BrandedDosedComponent> brandedDosedComponentMap = new HashMap <String, BrandedDosedComponent>(); // ingreds + brandname tostring hashed as key
	static Map <String, BrandedFormedComponent> brandedFormedComponentMap = new HashMap <String, BrandedFormedComponent>(); //form + ingreds + brandname tostring hashed as key 

	static Map <String, BrandedFormedSpecificComponent> brandedFormedSpecificComponentMap = new HashMap <String, BrandedFormedSpecificComponent>(); //form + specingreds + brandname tostring & hashed as key
	static Map <String, BrandedDosedSpecificComponent> brandedDosedSpecificComponentMap = new HashMap <String, BrandedDosedSpecificComponent>(); 
	
	private static String url = "jdbc:mysql://localhost/bdpm";
	private static String user = "root";
	private static String password = "";


	//-----------------find or create functions of all types of drug entities--------------------------------------
	
	static Form findOrCreateForm(String formName) {
		String key = formName; //create map key
		Form matchingForm = formMap.get(key); //find matching Form
		if (matchingForm == null) { //if doesn't exist, create it, then find it
			matchingForm = new Form(formName);
			formMap.put(key, matchingForm);
		}
		return matchingForm;
	}
	
	static Dose findOrCreateDose(String doseLabel) {
		String key = doseLabel; //create map key
		Dose matchingDose = doseMap.get(key); //find matching Dose
		if (matchingDose == null) { //if doesn't exist, create it, then find it
			matchingDose = new Dose(doseLabel);
			doseMap.put(key, matchingDose);
		}
		return matchingDose;
	}
	
	static Dose findOrCreateDose(String doseNumber, String unitLabel) {
		String key = doseNumber + unitLabel; //create map key
		Dose matchingDose = doseMap.get(key); //find matching Dose
		if (matchingDose == null) { //if doesn't exist, create it, then find it
			matchingDose = new Dose(doseNumber, unitLabel);
			doseMap.put(key, matchingDose);
		}
		return matchingDose;
	}

	static Ingredient findOrCreateIngredient(String codeSubstance, String ingredientName){
		Ingredient matchingIngredient = ingredientMap.get(codeSubstance); //find matching Ingredient, codeSubstance is map key
		if (matchingIngredient==null) { //if not found, create Ingredient and put in map
			matchingIngredient = new Ingredient(codeSubstance, ingredientName);
			ingredientMap.put(codeSubstance, matchingIngredient); 
		} else { //but if found, add name to Set of names
			matchingIngredient.otherNames.add(ingredientName);
		}
		return matchingIngredient;
	}
	

	static SpecificIngredient findOrCreateSpecificIngredient(String codeSubstance, String ingredientName){
		SpecificIngredient matchingIngredient = specificIngredientMap.get(codeSubstance); //find matching SpecificIngredient, codeSubstance is map key
		if (matchingIngredient==null) { //if not found, create Ingredient and put in map
			matchingIngredient = new SpecificIngredient(codeSubstance, ingredientName);
			specificIngredientMap.put(codeSubstance, matchingIngredient); 
		} else { //but if found, add name to Set of names
			matchingIngredient.otherNames.add(ingredientName);
		}
		return matchingIngredient;
	}
	
	static BrandName findOrCreateBrandName(String brandNameLabel) {
		String key = brandNameLabel; //create map key
		BrandName matchingBrandName = brandNameMap.get(key); //find matching BrandName
		if (matchingBrandName == null) { //if doesn't exist, create it, then find it
			matchingBrandName = new BrandName(brandNameLabel);
			brandNameMap.put(key, matchingBrandName);
		}
		return matchingBrandName;
	}

	static DosedComponent findOrCreateDosedComponent(HashMap<Ingredient,Dose> ingredientDoseMap) {
		String mapKey = Utils.hash(ingredientDoseMap.toString()); //create key
		DosedComponent matchingDosedComponent = dosedComponentMap.get(mapKey); // see if DosedComponent exists already
		if (matchingDosedComponent == null) { //if doesn't exist, create it and put it in Map
			matchingDosedComponent = new DosedComponent(ingredientDoseMap);
			dosedComponentMap.put(mapKey, matchingDosedComponent); 
		}
		return matchingDosedComponent;
	}
	
	static DosedSpecificComponent findOrCreateDosedSpecificComponent(HashMap<SpecificIngredient, Dose> specificIngredDoseMap) {
		String mapKey = Utils.hash(specificIngredDoseMap.toString()); //create key
		DosedSpecificComponent matchingDosedIngred = dosedSpecificComponentMap.get(mapKey); 
		if (matchingDosedIngred == null) { //if doesn't exist, create it and put it in Map
			matchingDosedIngred = new DosedSpecificComponent(specificIngredDoseMap);
			dosedSpecificComponentMap.put(mapKey, matchingDosedIngred); 
		}
		return matchingDosedIngred;
	}

	static FormedComponent findOrCreateFormedComponent(Form form, Set<Ingredient> containedIngredients){
		String mapKey = Utils.hash(containedIngredients.toString() + form.getFormLabel());//create a key 
		FormedComponent matchingFormedComponent = formedComponentMap.get(mapKey); //use that key to find matching FormedComponent
		if(matchingFormedComponent==null) { //if doesn't exist, create it and put in Map
			matchingFormedComponent=new FormedComponent(form, containedIngredients);
			formedComponentMap.put(mapKey, matchingFormedComponent);
		}
		return matchingFormedComponent;
	}
	
	static FormedSpecificComponent findOrCreateFormedSpecificComponent(BrandedDrug brandedDrug){
		String mapKey = Utils.hash(brandedDrug.getSpecificIngredientSet().toString() + brandedDrug.getForm().getFormLabel());//create a key 
		FormedSpecificComponent matchingComponent = formedSpecificComponentMap.get(mapKey); //use that key to find matching FormedSpecificComponent
		if(matchingComponent==null) { //if doesn't exist, create it and put in Map
			matchingComponent=new FormedSpecificComponent(brandedDrug.getForm(), brandedDrug.getSpecificIngredientSet());
			formedSpecificComponentMap.put(mapKey, matchingComponent);
		}
		return matchingComponent;
	}
	
	
	static BrandedDosedComponent findOrCreateBrandedDosedComponent(BrandedDrug brandedDrug){
		String mapKey = brandedDrug.brandedDosedComponent.generateMapKey();
		BrandedDosedComponent matchingBrandedComponent = brandedDosedComponentMap.get(mapKey);
		if (matchingBrandedComponent == null) {
			matchingBrandedComponent = new BrandedDosedComponent(brandedDrug.brandedDosedComponent.ingredientsAndDoses, 
					brandedDrug.brandedDosedSpecificComponent.specificIngredsAndDoses, 
					brandedDrug.brandName);
			brandedDosedComponentMap.put(mapKey, matchingBrandedComponent);
		}
		return matchingBrandedComponent;
	}

	static BrandedFormedComponent findOrCreateBrandedFormedComponent(BrandedDrug brandedDrug){
		String mapKey = brandedDrug.brandedFormedComponent.generateMapKey();
		BrandedFormedComponent matchingFormedComponent = brandedFormedComponentMap.get(mapKey);
		if (matchingFormedComponent == null) {
			matchingFormedComponent = new BrandedFormedComponent(brandedDrug.getForm(), 
					brandedDrug.brandedFormedComponent.containedIngredients, 
					brandedDrug.brandName);
			brandedFormedComponentMap.put(mapKey, matchingFormedComponent);
		}
		return matchingFormedComponent;
	}
	
	static BrandedFormedSpecificComponent findOrCreateBrandedFormedSpecificComponent(BrandedDrug brandedDrug){
		String mapKey = brandedDrug.brandedFormedSpecificComponent.generateMapKey();
		BrandedFormedSpecificComponent matchingComponent = brandedFormedSpecificComponentMap.get(mapKey);
		if (matchingComponent  == null) {
			matchingComponent  = new BrandedFormedSpecificComponent(brandedDrug.getForm(), 
					brandedDrug.brandedFormedSpecificComponent.containedSpecificIngredients, 
					brandedDrug.brandName);
			brandedFormedSpecificComponentMap.put(mapKey, matchingComponent);
		}
		return matchingComponent;
	}
	
	static BrandedDosedSpecificComponent findOrCreateBrandedDosedSpecificComponent(BrandedDrug brandedDrug){
		String mapKey = brandedDrug.brandedDosedSpecificComponent.generateMapKey();
		BrandedDosedSpecificComponent matchingComponent = brandedDosedSpecificComponentMap.get(mapKey);
		if (matchingComponent == null) {
			matchingComponent = new BrandedDosedSpecificComponent(brandedDrug.brandName,
					brandedDrug.brandedDosedSpecificComponent.specificIngredsAndDoses);
			brandedDosedSpecificComponentMap.put(mapKey, matchingComponent);
		}
		return matchingComponent;
	}



	static BrandedDrug findOrCreateBrandedDrug (String drugId, String label, Form form, BrandName brandName){
		BrandedDrug matchingBrandedDrug = brandedDrugMap.get(drugId); //find matching BrandedDrug, CodeCis is the map key 
		if(matchingBrandedDrug==null){ //if doesn't exist, create it and put in BrandedDrug map
			matchingBrandedDrug=new BrandedDrug(drugId, label, form, brandName);
			brandedDrugMap.put(drugId, matchingBrandedDrug);
		} 
		return matchingBrandedDrug;
	}


	static ClinicalDrug findOrCreateClinicalDrug(BrandedDrug brandedDrug){
		String mapKey = Utils.hash(brandedDrug.getForm().toString() + brandedDrug.getIngredientDoseMap().toString() + brandedDrug.getSpecificIngredientDoseMap().toString()); 
		ClinicalDrug matchingClinicalDrug = clinicalDrugMap.get(mapKey); //find potentially matching ClinicalDrug
		if (matchingClinicalDrug==null){ //if doesn't exist, create it and put it in map
			matchingClinicalDrug = new ClinicalDrug(brandedDrug.getForm(),  //add maps and sets that we have already from clinicaldrug 
					brandedDrug.getIngredientDoseMap(), 
					brandedDrug.getSpecificIngredientDoseMap(), 
					brandedDrug.getIngredientsSet(), 
					brandedDrug.getSpecificIngredientSet()); 
			clinicalDrugMap.put(mapKey, matchingClinicalDrug);
		}
		return matchingClinicalDrug; 
	}
	

	//________________________________________________________________________________________________________________________________________________________________________
	
	
	//once ALL BrandedDrugs are made, use the following to generate the more general drug entities 

	static void generateClinicalDrugMap(){
		//iterate over all BrandedDrugs in Map and fill up the ClinicalDrugMap 
		for (BrandedDrug value : brandedDrugMap.values()) {
			ClinicalDrug clinicalDrug = findOrCreateClinicalDrug(value);
			value.clinicalDrugBelongsTo = clinicalDrug; //and put the branded drug as under the clinical drug
		}
	}

	static void generateDosedComponentMap(){ //TODO 
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateDosedComponent(value.getIngredientDoseMap());
		}
		for (ClinicalDrug value : clinicalDrugMap.values()){
			findOrCreateDosedComponent(value.getIngredientDoseMap());
		}
	}

	static void generateFormedComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateFormedComponent(value.getForm(), value.getIngredientsSet());
			}
	}
	
	static void generateFormedSpecificComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateFormedSpecificComponent(value);
			}
	}
	
	static void generateDosedSpecificComponentMap(){ //do this for branded and clinical sides because their dosedcomponents are not equivalent ??
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateDosedSpecificComponent(value.getSpecificIngredientDoseMap());
			}
		for (ClinicalDrug value : clinicalDrugMap.values()) {
			findOrCreateDosedSpecificComponent(value.getSpecificIngredientDoseMap());
			}
	}
	
	static void generateBrandedDosedComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateBrandedDosedComponent(value);
		}
	}
	
	
	static void generateBrandedFormedComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateBrandedFormedComponent(value);
		}
	}
	
	static void generateBrandedFormedSpecificComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateBrandedFormedSpecificComponent(value);
		}
	}
	
	static void generateBrandedDosedSpecificComponentMap(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			findOrCreateBrandedDosedSpecificComponent(value);
		}
	}
	
	//_______________________________________________________________________________________________________________________________________________
	
	//once all clinical drugs are made, link BrandedDrugs to ClinicalDrug
	static void linkClinicalDrugsWithBrandedDrugs(){
		for (ClinicalDrug clinicalDrug : clinicalDrugMap.values()) { //for each ClinicalDrug, go through all BrandedDrugs and find matching ones  	 
			for(BrandedDrug brandedDrug : brandedDrugMap.values()){
				//for them to match, we need the same form, ingredients and doses, and specificingredients and doses 
				if (clinicalDrug.getForm().toString().equals(brandedDrug.getForm().toString()) 
						&& clinicalDrug.getIngredientDoseMap().toString().equals(brandedDrug.getIngredientDoseMap().toString()) 
						&& clinicalDrug.getSpecificIngredientDoseMap().toString().equals(brandedDrug.getSpecificIngredientDoseMap().toString())){
					//if these three are all equal, then make the link
					brandedDrug.clinicalDrugBelongsTo=clinicalDrug;
					clinicalDrug.matchingBrandedDrugs.add(brandedDrug);	
				}
			}
		}
	}
	
	
	//go through branded drugs and put specific ingredients as under their overarching ingredient 
	static void organizeSpecificIngredients(){
		//go through all Branded Drugs and add its specific ingredient to the ingredient's set of matching specific ingredients 
		for (BrandedDrug value : brandedDrugMap.values()) {
			if (value.getSpecificIngredientSet().size()==1 & value.getIngredientsSet().size()==1){ //do this only if the drug has 1 ingredient and 1 specific ingredient, otherwise it will mix up ingredients  
				SpecificIngredient specificIngredient = value.brandedFormedSpecificComponent.containedSpecificIngredients.iterator().next();
				Ingredient ingredient = value.brandedFormedComponent.containedIngredients.iterator().next();
				ingredient.matchingSpecificIngredients.add(specificIngredient); //add the specific ingredient to the ingredient's set
				specificIngredient.ingredientBelongsTo = ingredient;
			}
		}
	}
	
	//TODO not working!!
	//put DosedSpecificComponents as under their matching DosedComponent (for both Branded and clinical drugs!) 
	static void organizeDosedSpecificComponents(){
		for (BrandedDrug value : brandedDrugMap.values()) { //go through all BrandedDrugs
			if(value.getIngredientDoseMap().size()==1 && value.getSpecificIngredientDoseMap().size()==1){ 
				//do this for clinica drug side: get specific components and put under general component
				DosedComponent dosedComponent = findOrCreateDosedComponent(value.getIngredientDoseMap());
				DosedSpecificComponent dosedSpecificComponent = findOrCreateDosedSpecificComponent(value.getSpecificIngredientDoseMap());
				dosedComponent.matchingDosedSpecificComponents.add(dosedSpecificComponent);
				dosedSpecificComponent.dosedComponentBelongsTo = dosedComponent;  //specific component points to a general component 
				
				//then do this for branded side
				BrandedDosedComponent brandedDosedComponent = findOrCreateBrandedDosedComponent(value);
				BrandedDosedSpecificComponent brandedDosedSpecificComponent = findOrCreateBrandedDosedSpecificComponent(value);
				brandedDosedComponent.matchingBrandedDosedSpecificComponents.add(brandedDosedSpecificComponent);
				brandedDosedSpecificComponent.brandedDosedComponentBelongsTo = brandedDosedComponent;
			}
		}
	}
	

	//put FormedSpecificComponents as under their matching FormedComponent, for both branded and clinical sides
	static void organizeFormedSpecificComponents(){
		for (BrandedDrug value : brandedDrugMap.values()) {
			if(value.getIngredientsSet().size()==1 && value.getSpecificIngredientSet().size()==1){ 
				//clinical side: get the components and put the specific one under the general one 
				FormedSpecificComponent formedSpecificComponent = findOrCreateFormedSpecificComponent(value);
				FormedComponent formedComponent = findOrCreateFormedComponent(value.getForm(), value.getIngredientsSet());
				formedComponent.matchingFormedSpecificComponents.add(formedSpecificComponent);
				formedSpecificComponent.formedComponentBelongsTo = formedComponent;
				//then do this for the branded side
				BrandedFormedSpecificComponent brandedFormedSpecificComponent = findOrCreateBrandedFormedSpecificComponent(value);
				BrandedFormedComponent brandedFormedComponent = findOrCreateBrandedFormedComponent(value);
				brandedFormedComponent.matchingBrandedFormedSpecificComponents.add(brandedFormedSpecificComponent);
				brandedFormedSpecificComponent.brandedFormedComponentBelongsTo = brandedFormedComponent;
			}
		}
	}
	
	
	//____________________________________________________________________________________________________________________________________________
	//_______________________Other Functions____________________________________________________________________________________________________________


	//connect and create statement and execute sql query
	static ResultSet sqlQuery(String query){
		ResultSet res = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection con = DriverManager.getConnection(url, user, password);
			java.sql.Statement stt = con.createStatement();
			res = stt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;

	}




}
