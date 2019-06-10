package bdpm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/* 
 * 
 * SELECT DISTINCT cis.CodeCIS, cis.Denomination, cis.FormePharma, cis_compo.DesignationElementPharma, cis_compo.DosageSubstance, cis_compo.DenomSubstance, cis_compo.NatureComposant, cis_compo.CodeSubstance, cis_gener.LibelleGrpGener 
 * FROM (`cis` LEFT OUTER JOIN cis_compo ON cis.CodeCIS=cis_compo.CodeCIS LEFT OUTER JOIN cis_gener ON cis_gener.CodeCIS=cis_compo.CodeCIS)
 * 
 * 
 */


public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		//create Branded Drugs
		try {
			String query = "SELECT DISTINCT cis.CodeCIS, cis.Denomination, cis.FormePharma, cis_compo.DesignationElementPharma, cis_compo.DosageSubstance, cis_compo.DenomSubstance, "
					+ "cis_compo.NatureComposant, cis_compo.CodeSubstance, cis_gener.LibelleGrpGener "
					+ "FROM (`cis` LEFT OUTER JOIN cis_compo ON cis.CodeCIS=cis_compo.CodeCIS LEFT OUTER JOIN cis_gener ON cis_gener.CodeCIS=cis_compo.CodeCIS)";
			ResultSet res = Ontology.sqlQuery(query);

			while(res.next()){ 
				//name the pertinent columns as Strings
				String codeSubstance=res.getString("CodeSubstance") == null ? "" : res.getString("CodeSubstance");
				String ingredientName=res.getString("DenomSubstance") == null ? "" : res.getString("DenomSubstance");
				String doseLabel=res.getString("DosageSubstance") == null ? "" : res.getString("DosageSubstance");
				String formLabel=res.getString("FormePharma") == null ? "" : res.getString("FormePharma");
				String brandedDrugId=res.getString("CodeCIS") == null ? "" : res.getString("CodeCIS");
				String drugLabel=res.getString("LibelleGrpGener") == null ? "" : res.getString("LibelleGrpGener");
				String brandLabel=res.getString("Denomination") == null ? "" : res.getString("Denomination");
				String natureComposant=res.getString("NatureComposant") == null ? "" : res.getString("NatureComposant");
				//create Dose from "dosagesubstance" column 
				Dose dose = new Dose(doseLabel);

				//create Form from "formepharma" column 
				Form form = Ontology.findOrCreateForm(formLabel);

				//create BrandName from each new brandLabel "denomination" column and add to this BrandedDrug
				BrandName brandName = new BrandName(brandLabel); 

				//we have enough information now to get or create Clinical Drug 
				BrandedDrug brandedDrug = Ontology.findOrCreateBrandedDrug(brandedDrugId, drugLabel, form, brandName);

				//if the column reads 'FT', add as Ingredient...
				if (natureComposant.equals("FT")){

					//get or create Ingredient from "denomsubstance" column 
					Ingredient ingredient = Ontology.findOrCreateIngredient(codeSubstance, ingredientName);

					//add Ingredient to clinicaldrug
					brandedDrug.addIngredientWithDose(ingredient, dose);


				}else if (natureComposant.equals("SA")){
					//it the column reads 'SA', add as SpecificIngredient...

					//get or create SpecificIngredient from "denomsubstance" column 
					SpecificIngredient specificIngredient = Ontology.findOrCreateSpecificIngredient(codeSubstance, ingredientName);

					//add specific ingredient 
					brandedDrug.addSpecIngredientWithDose(specificIngredient, dose);

				} else {
					System.out.println("ingredient not labeled SA nor FT");	
				}


			}

			//once all BrandedDrugs are made, use its attributes to create other entities
			Ontology.generateNonbrandedDrugMap();

			Ontology.generateFormedComponentMap();
			Ontology.generateDosedComponentMap();
			Ontology.generateBrandedDosedComponentMap();
			Ontology.generateBrandedFormedComponentMap();
			Ontology.generateDosedSpecificComponentMap();
			Ontology.generateFormedSpecificComponentMap();
			Ontology.generateBrandedDosedSpecificComponentMap();
			Ontology.generateBrandedFormedSpecificComponentMap();
			System.out.println("Maps generated");

			//organize specific ingredients under ingredients 
			Ontology.linkNonbrandedDrugsWithBrandedDrugs(); //must link branded and nonbranded first 
			Ontology.organizeSpecificIngredients();
			Ontology.organizeDosedSpecificComponents();
			Ontology.organizeFormedSpecificComponents();
			System.out.println("Links created between specific and general components");

//			Utils.writeIngredientsToCSV(Ontology.ingredientMap, "ingredients");
			Utils.writeSpecificIngredientsToCSV(Ontology.specificIngredientMap);
//			Utils.writeBrandedDrugsToCSV(Ontology.brandedDrugMap);
//			Utils.writeNonbrandedDrugsToCSV(Ontology.nonbrandedDrugMap);
//			Utils.writeDosedComponentsToCSV(Ontology.dosedComponentMap);
//			Utils.writeFormedComponentsToCSV(Ontology.formedComponentMap);
//			Utils.writeDosedSpecificComponentsToCSV(Ontology.dosedSpecificComponentMap);
//			Utils.writeFormedSpecificComponentsToCSV(Ontology.formedSpecificComponentMap);
//			Utils.writeBrandedDosedComponentsToCSV(Ontology.brandedDosedComponentMap);
//			Utils.writeBrandedFormedComponentsToCSV(Ontology.brandedFormedComponentMap);
//			Utils.writeBrandedDosedSpecificComponentsToCSV(Ontology.brandedDosedSpecificComponentMap);
//			Utils.writeBrandedFormedSpecificComponentsToCSV(Ontology.brandedFormedSpecificComponentMap);


			
		

		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		
		
	}



}