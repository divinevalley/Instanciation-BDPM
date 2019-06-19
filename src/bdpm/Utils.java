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
		if (doseAndUnitSegments.length==1 || doseAndUnitSegments[doseAndUnitSegments.length-1].trim().length()>12 || doseAndUnitSegments[0].matches("[a-zA-Z,]+")){ 
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
			BufferedWriter bwCheckDose = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDrugsCHECKDOSE.csv"), "UTF-8"));
			BufferedWriter bwCheckBrandName = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("brandedDrugsCHECKBRANDNAMES.csv"), "UTF-8"));
			
			for (BrandedDrug drug : clinicalDrugMap.values()) {
				
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(drug.brandedDrugId);
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.brandName.toString().length() == 0? "" : drug.brandName.toString());
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.getForm().toString().length() == 0? "" : drug.getForm().toString());

				//go through ingredients and doses	
				drug.brandedDosedComponent.loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
		
				//go through specific ingredients and doses
				drug.brandedDosedSpecificComponent.loopThroughDosedComponents(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);

				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.nonbrandedDrugBelongsTo.generateMapKey());  //hash id for nonbrandeddrug
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(drug.label.length() == 0? "" : drug.label);

				//print to different csv's depending on checking status
				if(drug.doseNeedsChecking==true){
					bwCheckDose.write(oneLine.toString());
					bwCheckDose.newLine();
				} else if (drug.brandNameNeedsChecking==true){
					bwCheckBrandName.write(oneLine.toString());
					bwCheckBrandName.newLine();
				} else {
					bw.write(oneLine.toString());
					bw.newLine();
				}
			}

			bw.flush();
			bw.close();
			bwCheckDose.flush();
			bwCheckDose.close();
			bwCheckBrandName.flush();
			bwCheckBrandName.close();
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
				
				//go through ingredients and doses
				drugEntry.getValue().dosedComponent.loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
	
				//go through specific ingredients and doses
				drugEntry.getValue().dosedSpecificComponent.loopThroughDosedComponents(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR); 
				
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

				//go through ingredients and doses
				entry.getValue().loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);		

				//go through matching DosedSpecificComponents
				entry.getValue().writeMatchingSpecificComponents(oneLine, CSV_SEPARATOR);
				
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

				formedComponentEntry.getValue().loopThroughIngredients(oneLine, CSV_SEPARATOR);
				
				formedComponentEntry.getValue().writeMatchingFormedSpecificComponents(oneLine, CSV_SEPARATOR);
				
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
	
	
	static void writeDosedSpecificComponentsToCSV(Map<String, DosedSpecificComponent> dosedSpecificComponentMap){ //hashid; number of dosed spec ingredients; specificingredient|dose; matching DosedComponent
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dosedspecificcomponents.csv"), "UTF-8"));

			for (Map.Entry<String, DosedSpecificComponent> dosedSpecificComponentEntry : dosedSpecificComponentMap.entrySet()) {
				StringBuffer oneLine = new StringBuffer();
				
				oneLine.append(dosedSpecificComponentEntry.getKey()); //hashid
							
				//go through specific ingredients and doses 
				dosedSpecificComponentEntry.getValue().loopThroughDosedSpecificIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
				
				dosedSpecificComponentEntry.getValue().writeMatchingDosedComponent(oneLine, CSV_SEPARATOR);

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
				
				formedSpecificComponentEntry.getValue().loopThroughSpecificIngredients(oneLine, CSV_SEPARATOR);
				
				formedSpecificComponentEntry.getValue().writeMatchingFormedComponent(oneLine, CSV_SEPARATOR);
				
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
				
				brandedDosedComponentEntry.getValue().loopThroughDosedIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(brandedDosedComponentEntry.getValue().brandName.brandNameLabel);
				
				brandedDosedComponentEntry.getValue().writeMatchingSpecificComponents(oneLine, CSV_SEPARATOR);
				
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
				
				formedComponentEntry.getValue().loopThroughIngredients(oneLine, CSV_SEPARATOR);
				
				formedComponentEntry.getValue().writeMatchingFormedSpecificComponents(oneLine, CSV_SEPARATOR);
				
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
				
						
				formedSpecComponentEntry.getValue().loopThroughSpecificIngredients(oneLine, CSV_SEPARATOR);
				
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(formedSpecComponentEntry.getValue().brandName.brandNameLabel);
				
				formedSpecComponentEntry.getValue().writeMatchingFormedComponent(oneLine, CSV_SEPARATOR);
				
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
		
				brandedDosedSpecificComponentEntry.getValue().loopThroughDosedSpecificIngredients(oneLine, CSV_SEPARATOR, CSV_DOSE_SEPARATOR);
				
				oneLine.append(CSV_SEPARATOR);
				oneLine.append(brandedDosedSpecificComponentEntry.getValue().brandName.brandNameLabel);
				
				
				brandedDosedSpecificComponentEntry.getValue().writeMatchingDosedComponent(oneLine, CSV_SEPARATOR);
				
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
