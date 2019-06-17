package bdpm;

import java.io.BufferedWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import java.io.OutputStreamWriter;

public class Utils {
	private static final String CSV_SEPARATOR = ";";
	private static final String CSV_DOSE_SEPARATOR = "|";
	
	//generic regex function
	static String matchRegex(String regexPattern, String inputString){
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(inputString);
		if (matcher.find()){
			return matcher.group();
		} else {
			return "";
		}
	}
	
	//regex to extract brand name
	static BrandName extractBrandNameAndCreateBrandName(String brandLabel){
		String foundString = matchRegex("\\b[A-Z0-9Ï'/.-]+[A-ZÏ'/\\s.-]+\\b", brandLabel);
		String unchanged = brandLabel + " *need to change!";    
		BrandName brandName;
	    if(foundString.equals(" ")||foundString.length()<3){ //regex couldn't find brandname as desired. don't change brandLabel
	    	brandLabel = unchanged;
	    	brandName = Ontology.findOrCreateBrandName(brandLabel);
	    	brandName.labelNeedsChecking = true;//tag for check
	    } else {
		    brandLabel = foundString.trim();
		    brandName = Ontology.findOrCreateBrandName(brandLabel); 
	    }
		return brandName;
	}
	
	static Dose splitDoseAndUnitAndCreateDose(String doseLabel){
		String[] doseAndUnitSegments = doseLabel.split("(?=\\s[mglicGLIUCµ])", 2); //split and keep the delimiter to the right 
		Unit unit = doseAndUnitSegments.length<2 ? new Unit("") : new Unit(doseAndUnitSegments[1].trim());
		Dose dose = Ontology.findOrCreateDose(doseAndUnitSegments[0], unit.unitLabel);
		//if split was impossible OR if any part is too long OR dose part has letters
		if (doseAndUnitSegments.length==1 || doseAndUnitSegments[doseAndUnitSegments.length-1].trim().length()>12 || !doseAndUnitSegments[0].matches("[^a-zA-Z]+")){ 
			dose.needsChecking=true;
		}
		return dose;
	}
	
	static void addSpecificIngredientWithDose(String doseLabel, Unit unit, String ingredientLabel, BrandedDrug brandedDrug){
		//create Dose object
		Dose dose = Ontology.findOrCreateDose(doseLabel, unit.unitLabel);
		//create  specific ingredient object
		SpecificIngredient specificIngredient = Ontology.findOrCreateSpecificIngredient(ingredientLabel, ingredientLabel);
		brandedDrug.addSpecIngredientWithDose(specificIngredient, dose);
	}
	
	static void addIngredientWithDose(String doseLabel, Unit unit, String ingredientLabel, BrandedDrug brandedDrug){
		//create Dose object
		Dose dose = Ontology.findOrCreateDose(doseLabel, unit.unitLabel);
		//create  ingredient object
		Ingredient ingredient = Ontology.findOrCreateIngredient(ingredientLabel, ingredientLabel);
		brandedDrug.addIngredientWithDose(ingredient, dose);
	}
	
	

	static void writeIngredientsToCSV(Map<String,Ingredient> ingredientMap, String csvFileName){
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFileName + ".csv"), "UTF-8"));

			for (Ingredient ingredient : ingredientMap.values()) { //codeSubstance; main ingredient name; number of alternative names; alternative names; number of matching specific ingreds; specific ingreds 
				StringBuffer oneLine = new StringBuffer();
				oneLine.append(ingredient.codeSubstance);
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(ingredient.ingredientName.length() == 0? "" : ingredient.ingredientName);
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(ingredient.otherNames.size());  //number of ingredient name labels in set
			
				for (String name : ingredient.otherNames){  //append ingredient names 
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(name);
				}
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(ingredient.matchingSpecificIngredients.size()); //number of matching specific ingredients 
				
				for (SpecificIngredient specIngredient : ingredient.matchingSpecificIngredients){ //append specific ingredients
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(specIngredient.specificIngredientName);
				}			
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}

	static void writeSpecificIngredientsToCSV(Map<String,SpecificIngredient> specIngredientMap){ //codesubstance; name; number of alternative names; alternative names; matching ingredient 
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("specificIngredients.csv"), "UTF-8"));

			for (SpecificIngredient specIngredient : specIngredientMap.values()) {
				StringBuffer oneLine = new StringBuffer();
				oneLine.append(specIngredient.codeSubstance);
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(specIngredient.specificIngredientName.length() == 0? "" : specIngredient.specificIngredientName);
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(specIngredient.otherNames.size());
			
				for (String name : specIngredient.otherNames){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(name);
				}
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(specIngredient.ingredientBelongsTo.codeSubstance);
			
				bw.write(oneLine.toString());
				bw.newLine();
			}

			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}


	static void writeBrandedDrugsToCSV(Map<String,BrandedDrug> clinicalDrugMap){  //brandedDrugId;brandName;form;number of ingredients; each dosed ingredient|dose; number of specific ingredients ; each specific ingred|dose; matching nonbranded drug ; original label
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDrugs.csv"), "UTF-8"));
			BufferedWriter bwDoseNeedsChecking = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDrugsDoseNeedsChecking.csv"), "UTF-8"));
			BufferedWriter bwBrandNameNeedsChecking = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDrugsBrandNamesNeedChecking.csv"), "UTF-8"));
			
			//variables for counters
//			int nbDrugsNoIngredients = 0;
//			int nbDrugsWithADoseMissing=0;
//			Set<Integer> nbIngredients = new HashSet<Integer>();
//			Set<Integer> nbSpecificIngredients = new HashSet<Integer>();


			for (BrandedDrug drug : clinicalDrugMap.values()) {
//				boolean ingredDoseMissing = false; // for counters. by default, assume dose is not missing
//				boolean specificIngredDoseMissing = false;
				
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(drug.brandedDrugId);
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.brandName.toString().length() == 0? "" : drug.brandName.toString());
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.getForm().toString().length() == 0? "" : drug.getForm().toString());

				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.brandedDosedComponent.ingredientsAndDoses.size()); //number of dosed ingredients
				
//				//counters for missing ingredients
//				if (drug.brandedDosedComponent.ingredientsAndDoses.size()==0){
//					nbDrugsNoIngredients++;
//				}
//				//counter hashsets for number of ingredients 
//				nbIngredients.add(drug.brandedDosedComponent.ingredientsAndDoses.size());
//				nbSpecificIngredients.add(drug.brandedDosedSpecificComponent.specificIngredsAndDoses.size());
				
				//go through ingredients and doses
				for (Map.Entry<Ingredient, Dose> dosedComponentEntry : drug.brandedDosedComponent.ingredientsAndDoses.entrySet()){
					
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedComponentEntry.getKey().ingredientName.toString().length()==0? "" : dosedComponentEntry.getKey().ingredientName.toString()); //ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedComponentEntry.getValue().doseNumber.length()==0? "" : dosedComponentEntry.getValue().doseNumber.toString()); //dose
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedComponentEntry.getValue().unit.unitLabel.length()==0? "" : dosedComponentEntry.getValue().unit.unitLabel); //unit
//					//counter for missing dose Part 1 
//					if(dosedComponentEntry.getValue().toString().length()==0){
//						ingredDoseMissing=true;
//					}				
				}

				oneLine.append(CSV_SEPARATOR);
				//go through specific ingredients and doses
				oneLine.append(drug.brandedDosedSpecificComponent.specificIngredsAndDoses.size()); //number of dosed specific ingredients

				for (Map.Entry<SpecificIngredient, Dose> dosedSpecificComponentEntry : drug.brandedDosedSpecificComponent.specificIngredsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getKey().specificIngredientName.toString().length()==0? "" : dosedSpecificComponentEntry.getKey().specificIngredientName.toString()); //specific ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getValue().doseNumber.length()==0? "" : dosedSpecificComponentEntry.getValue().doseNumber.toString()); //dose
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getValue().unit.unitLabel.length()==0? "" : dosedSpecificComponentEntry.getValue().unit.unitLabel.toString()); //unit
//					//counter for specific ingredient dose missing Part 2 
//					if(dosedSpecificComponentEntry.getValue().toString().length()==0){
//						specificIngredDoseMissing=true;
//					}
				}

				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.nonbrandedDrugBelongsTo.generateMapKey());  //hash id for nonbrandeddrug
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.label.length() == 0? "" : drug.label);

				//print to different csv's depending on checking status
				if(drug.doseNeedsChecking==true){
					bwDoseNeedsChecking.write(oneLine.toString());
					bwDoseNeedsChecking.newLine();
				} else if (drug.brandNameNeedsChecking==true){
					bwBrandNameNeedsChecking.write(oneLine.toString());
					bwBrandNameNeedsChecking.newLine();
				} else {
					bw.write(oneLine.toString());
					bw.newLine();
				}
				
				
				
				
//				if (specificIngredDoseMissing==true||ingredDoseMissing==true){ // Part 3 final counter for if dose is missing from either ingredient or specific ingredient side
//					nbDrugsWithADoseMissing++;
//				}
			}
//			System.out.println("Number of drugs with no ingredients: "+nbDrugsNoIngredients 
//					+ "\nNumber of Drugs with a dose missing: " + nbDrugsWithADoseMissing
//					+ "\nNumber of Ingredients: " + nbIngredients
//					+ "\nNumber of Specific Ingredients: " + nbSpecificIngredients);

			bw.flush();
			bw.close();
			bwDoseNeedsChecking.flush();
			bwDoseNeedsChecking.close();
			bwBrandNameNeedsChecking.flush();
			bwBrandNameNeedsChecking.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}


	static void writeNonbrandedDrugsToCSV(Map<String, NonbrandedDrug> nonbrandedDrugMap){ //hash id; form;number of ingredients; eachdosed ingredient|dose; number of specific ingredients ; each specific ingred|dose ; number of matching BrandedDrugs ; each matching BrandedDrug's id
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("nonbrandeddrugs.csv"), "UTF-8"));

			for (Map.Entry<String, NonbrandedDrug> drugEntry : nonbrandedDrugMap.entrySet()) {

				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(drugEntry.getKey()); //hashid
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drugEntry.getValue().formedComponent.form.toString().length() == 0? "" : drugEntry.getValue().formedComponent.form.toString());

				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drugEntry.getValue().dosedComponent.ingredientsAndDoses.size()); //number of dosed ingredients
				
				//go through ingredients and doses
				for (Map.Entry<Ingredient, Dose> dosedComponentEntry : drugEntry.getValue().dosedComponent.ingredientsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedComponentEntry.getKey().ingredientName.toString().length()==0? "" : dosedComponentEntry.getKey().ingredientName.toString()); //ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedComponentEntry.getValue().toString().length()==0? "" : dosedComponentEntry.getValue().toString()); //dose
				}

				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drugEntry.getValue().dosedSpecificComponent.specificIngredsAndDoses.size()); //number of dosed specific ingredients

				//go through specific ingredients and doses
				for (Map.Entry<SpecificIngredient, Dose> dosedSpecificComponentEntry : drugEntry.getValue().dosedSpecificComponent.specificIngredsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getKey().specificIngredientName.toString().length()==0? "" : dosedSpecificComponentEntry.getKey().specificIngredientName.toString()); //specific ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getValue().toString().length()==0? "" : dosedSpecificComponentEntry.getValue().toString()); //dose
				}
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drugEntry.getValue().matchingBrandedDrugs.size()); //number of matching BrandedDrugs
				
				//go through matching BrandedDrugs 
				for (BrandedDrug matchingDrug : drugEntry.getValue().matchingBrandedDrugs){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(matchingDrug.brandedDrugId); //matching BrandedDrug id
				}
				
				bw.write(oneLine.toString());
				bw.newLine();
			}

			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}

	
	static void writeDosedComponentsToCSV(Map<String, DosedComponent> dosedComponentMap){ //hashid; number dosedingredients; ingredient|dose; nb matching DosedSpecificComponents ; each matching DosedSpecificComponent
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dosedcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, DosedComponent> entry : dosedComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(entry.getKey()); //hashid
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(entry.getValue().ingredientsAndDoses.size()); //number of dosed ingredients 

				//go through ingredients and doses
				for (Map.Entry<Ingredient, Dose> ingredDoseEntry : entry.getValue().ingredientsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(ingredDoseEntry.getKey().ingredientName.toString().length()==0? "" : ingredDoseEntry.getKey().ingredientName.toString()); //ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(ingredDoseEntry.getValue().toString().length()==0? "" : ingredDoseEntry.getValue().toString()); //dose
				}
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(entry.getValue().matchingDosedSpecificComponents.size());  //nb matching DosedSpecificComponents
				
				//go through matching DosedSpecificComponents
				for (DosedSpecificComponent matchingDosedSpecificComponent : entry.getValue().matchingDosedSpecificComponents){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(matchingDosedSpecificComponent.generateMapKey());  //print matching DosedSpecificComponent's mapkey
				}
				
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	static void writeFormedComponentsToCSV(Map<String, FormedComponent> formedComponentMap){ // hash id; form;number ingredients;ingredient; nb matching FormedSpecific components ; each matching FormedSpecificComponent
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("formedcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, FormedComponent>formedComponentEntry : formedComponentMap.entrySet()) {

				StringBuffer oneLine = new StringBuffer();
				oneLine.append(formedComponentEntry.getKey()); 
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().form); 
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().containedIngredients.size()); //number of ingredients in this component 

				//go through ingredients 
				for (Ingredient ingredient : formedComponentEntry.getValue().containedIngredients){ //print all ingredients in the hashset
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(ingredient.ingredientName.toString().length()==0? "" : ingredient.ingredientName.toString());
				}
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().matchingFormedSpecificComponents.size()); //nb matching FormedSpecificComponents
				
				//go through matching FormedSpecificComponents
				for (FormedSpecificComponent matchingFormedSpecificComponent : formedComponentEntry.getValue().matchingFormedSpecificComponents){
					if (matchingFormedSpecificComponent.containedSpecificIngredients.size()>0){ // avoid putting "empty" objects
						oneLine.append(CSV_SEPARATOR);
						oneLine.append(matchingFormedSpecificComponent.generateMapKey()); //print matching FormedSpecificComponent mapkey
					}
					
				}
				
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	
	static void writeDosedSpecificComponentsToCSV(Map<String, DosedSpecificComponent> dosedSpecificComponentMap){ //hashid; number of dosed spec ingredients; specificingredient|dose
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dosedspecificcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, DosedSpecificComponent> dosedSpecificComponentEntry : dosedSpecificComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(dosedSpecificComponentEntry.getKey()); //hashid
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(dosedSpecificComponentEntry.getValue().specificIngredsAndDoses.size()); //number of dosed specific ingredients
				
				//go through specific ingredients and doses 
				for (Map.Entry<SpecificIngredient, Dose> ingredDoseMapEntry : dosedSpecificComponentEntry.getValue().specificIngredsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(ingredDoseMapEntry.getKey().specificIngredientName.toString().length()==0? "" : ingredDoseMapEntry.getKey().specificIngredientName.toString()); //ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(ingredDoseMapEntry.getValue().toString().length()==0? "" : ingredDoseMapEntry.getValue().toString()); //dose
				}
						
				if (dosedSpecificComponentEntry.getValue().dosedComponentBelongsTo.ingredientsAndDoses.size() > 0){  //avoid generating a hashid for an meaningless object (empty map)
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getValue().dosedComponentBelongsTo.generateMapKey());
					
				}
				
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	static void writeFormedSpecificComponentsToCSV(Map<String, FormedSpecificComponent> formedSpecificComponentMap){ // hash id; form;number of specificingredients;specificingredient; matching formed component
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("formedspecificcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, FormedSpecificComponent> formedSpecificComponentEntry : formedSpecificComponentMap.entrySet()) {

				StringBuffer oneLine = new StringBuffer();
				oneLine.append(formedSpecificComponentEntry.getKey()); //hash id
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecificComponentEntry.getValue().form); 
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecificComponentEntry.getValue().containedSpecificIngredients.size()); //number of specificingredients in this component 

				for (SpecificIngredient specificIngredient : formedSpecificComponentEntry.getValue().containedSpecificIngredients){ //print all ingredients in the hashset
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(specificIngredient.specificIngredientName.toString().length()==0? "" : specificIngredient.specificIngredientName.toString());
				} 
				if (formedSpecificComponentEntry.getValue().formedComponentBelongsTo.containedIngredients.size()>0){ //avoid generating mapkey for a meaningless object
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(formedSpecificComponentEntry.getValue().formedComponentBelongsTo.generateMapKey()); 
				}
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	static void writeBrandedDosedComponentsToCSV(Map<String, BrandedDosedComponent> brandedDosedComponentMap){ // hash id; number of dosed ingredients; ingredient|dose; brandname
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandeddosedcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, BrandedDosedComponent> brandedDosedComponentEntry : brandedDosedComponentMap.entrySet()) {

				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(brandedDosedComponentEntry.getKey()); //hashid
				oneLine.append(CSV_SEPARATOR);
				
				oneLine.append(brandedDosedComponentEntry.getValue().ingredientsAndDoses.size()); //number of dosed specific ingredients

				for (Map.Entry<Ingredient, Dose> dosedComponentEntry : brandedDosedComponentEntry.getValue().ingredientsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedComponentEntry.getKey().ingredientName.toString().length()==0? "" : dosedComponentEntry.getKey().ingredientName.toString()); //ingredient
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedComponentEntry.getValue().toString().length()==0? "" : dosedComponentEntry.getValue().toString()); //dose
				}
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(brandedDosedComponentEntry.getValue().brandName.brandNameLabel);
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(brandedDosedComponentEntry.getValue().matchingBrandedDosedSpecificComponents.size());  //number of Branded DosedSpecificComponents
				
				for (BrandedDosedSpecificComponent matchingComponent : brandedDosedComponentEntry.getValue().matchingBrandedDosedSpecificComponents){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(matchingComponent.generateMapKey());
				}
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	static void writeBrandedFormedComponentsToCSV(Map<String, BrandedFormedComponent> brandedFormedComponentMap){ // hashid; form; number of ingredients; ingredient|dose; brandname; nb matching branded formed specific components; matching 
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedformedcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, BrandedFormedComponent> formedComponentEntry : brandedFormedComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(formedComponentEntry.getKey()); //hash id
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().form); 
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().containedIngredients.size()); //number of ingredients in this component 

				for (Ingredient ingredient : formedComponentEntry.getValue().containedIngredients){ //go through all ingredients in the hashset
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(ingredient.ingredientName.toString().length()==0? "" : ingredient.ingredientName.toString());
				}
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().brandName.brandNameLabel);
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedComponentEntry.getValue().matchingBrandedFormedSpecificComponents.size()); // # of matching
				
				for (BrandedFormedSpecificComponent matchingBFSComponent : formedComponentEntry.getValue().matchingBrandedFormedSpecificComponents){ // matching branded formed specific components 
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(matchingBFSComponent.generateMapKey());
				}
		
				bw.write(oneLine.toString());
				bw.newLine();
				
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	
	static void writeBrandedFormedSpecificComponentsToCSV(Map<String, BrandedFormedSpecificComponent> brandedFormedSpecificComponentMap){ //  hash id; form; number of spec ingredients; specific ingredient|dose; brandname; matching formed component
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedFormedSpecificComponents.csv"), "UTF-8"));

			for (Map.Entry<String, BrandedFormedSpecificComponent> formedSpecComponentEntry : brandedFormedSpecificComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				oneLine.append(formedSpecComponentEntry.getKey()); // hash id
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecComponentEntry.getValue().form); 
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecComponentEntry.getValue().containedSpecificIngredients.size()); //number of ingredients in this component 

				for (SpecificIngredient specificIngredient : formedSpecComponentEntry.getValue().containedSpecificIngredients){ //print all specific ingredients in the hashset
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(specificIngredient.specificIngredientName.toString().length()==0? "" : specificIngredient.specificIngredientName.toString());
				}
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecComponentEntry.getValue().brandName.brandNameLabel);
				
				if (formedSpecComponentEntry.getValue().brandedFormedComponentBelongsTo.containedIngredients.size()>0){ //avoid "empty" objects 
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(formedSpecComponentEntry.getValue().brandedFormedComponentBelongsTo.generateMapKey()); //matching branded formed component
				}
				
				bw.write(oneLine.toString());
				bw.newLine();
				
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}

	static void writeBrandedDosedSpecificComponentsToCSV(Map<String, BrandedDosedSpecificComponent> brandedDosedSpecificComponentMap){ //  hash id; number of dosed specific ingredients; specific ingredient|dose; brandname/ matching dosed component 
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDosedSpecificComponents.csv"), "UTF-8"));
					
			for (Map.Entry<String, BrandedDosedSpecificComponent> brandedDosedSpecificComponentEntry : brandedDosedSpecificComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(brandedDosedSpecificComponentEntry.getKey()); //hash id
				oneLine.append(CSV_SEPARATOR);
				
				oneLine.append(brandedDosedSpecificComponentEntry.getValue().specificIngredsAndDoses.size()); //number of dosed specific ingredients

				for (Map.Entry<SpecificIngredient, Dose> dosedSpecificComponentEntry : brandedDosedSpecificComponentEntry.getValue().specificIngredsAndDoses.entrySet()){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getKey().specificIngredientName.toString().length()==0? "" : dosedSpecificComponentEntry.getKey().specificIngredientName.toString()); //specific ingredient name
					oneLine.append(CSV_DOSE_SEPARATOR);
					oneLine.append(dosedSpecificComponentEntry.getValue().toString().length()==0? "" : dosedSpecificComponentEntry.getValue().toString()); //dose
				}
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(brandedDosedSpecificComponentEntry.getValue().brandName.brandNameLabel);
				
				if (brandedDosedSpecificComponentEntry.getValue().brandedDosedComponentBelongsTo.ingredientsAndDoses.size()>0){
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(brandedDosedSpecificComponentEntry.getValue().brandedDosedComponentBelongsTo.generateMapKey()); //matching overall branded dosed component 
				}
				bw.write(oneLine.toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		}
		catch (UnsupportedEncodingException e) {e.printStackTrace();}
		catch (FileNotFoundException e){e.printStackTrace();}
		catch (IOException e){e.printStackTrace();}
	}
	
	
	public static String hash(String stringToEncrypt) {
		String encryptedString = "";
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(stringToEncrypt.getBytes());
			encryptedString = DatatypeConverter.printHexBinary(messageDigest.digest());
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encryptedString;

	}
}
