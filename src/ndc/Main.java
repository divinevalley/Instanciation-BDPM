package ndc;  // ndc! 

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import com.mysql.jdbc.StringUtils;

/* 
 * SELECT product.PRODUCTID, product.PRODUCTNDC, product.PRODUCTTYPENAME, product.PROPRIETARYNAME, product.PROPRIETARYNAMESUFFIX, product.NONPROPRIETARYNAME, product.LABELERNAME, product.SUBSTANCENAME, product.ACTIVE_NUMERATOR_STRENGTH, product.ACTIVE_INGRED_UNIT, product.DEASCHEDULE, package.PRODUCTNDC, package.PACKAGEDESCRIPTION 
FROM `product` LEFT OUTER JOIN package ON product.PRODUCTID = package.PRODUCTID
 */


public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		//create Branded Drugs
		try {
			String query = "SELECT DISTINCT PRODUCTNDC, PROPRIETARYNAME, PROPRIETARYNAMESUFFIX, NONPROPRIETARYNAME, DOSAGEFORMNAME, ROUTENAME, "
					+ "LABELERNAME, SUBSTANCENAME, ACTIVE_NUMERATOR_STRENGTH, ACTIVE_INGRED_UNIT, PHARM_CLASSES, DEASCHEDULE FROM `product`";
			ResultSet res = Ontology.sqlQuery(query);
			System.out.println("query went through");

			while(res.next()){ 
				//name the pertinent columns as Strings
//				String codeSubstance=res.getString("SUBSTANCENAME") == null ? "" : res.getString("SUBSTANCENAME");
				String ingredientName=res.getString("SUBSTANCENAME") == null ? "" : res.getString("SUBSTANCENAME").trim();
				String doseLabel=res.getString("ACTIVE_NUMERATOR_STRENGTH") == null ? "" : res.getString("ACTIVE_NUMERATOR_STRENGTH").trim();
				String formLabel=res.getString("DOSAGEFORMNAME") == null ? "" : res.getString("DOSAGEFORMNAME");
				String brandedDrugId=res.getString("PRODUCTNDC") == null ? "" : res.getString("PRODUCTNDC");
				String drugLabel=res.getString("PROPRIETARYNAME") == null ? "" : res.getString("PROPRIETARYNAME");
				String brandLabel=res.getString("PROPRIETARYNAME") == null ? "" : res.getString("PROPRIETARYNAME").concat(" "+res.getString("PROPRIETARYNAMESUFFIX"));
				//String natureComposant=res.getString("NatureComposant") == null ? "" : res.getString("NatureComposant");
				

				//create Form from "formepharma" column 
				Form form = Ontology.findOrCreateForm(formLabel);

				//create BrandName from each new brandLabel "denomination" column and add to this BrandedDrug
				BrandName brandName = Ontology.findOrCreateBrandName(brandLabel); 

				//we have enough information now to get or create Clinical Drug 
				BrandedDrug brandedDrug = Ontology.findOrCreateBrandedDrug(brandedDrugId, drugLabel, form, brandName);
				
				//if multiple ingredients, 
				if (ingredientName.contains(";")){
					ingredientName=Utils.removeEndSemicolon(ingredientName);
					
					if (doseLabel.contains(";")){
						doseLabel=Utils.removeEndSemicolon(doseLabel);	
					} //remove unnecessary semicolons from doseLabel 
					
					
					if(Utils.countOccurences(ingredientName, ";")==Utils.countOccurences(doseLabel, ";")){ //after ";" are removed, if both have same 
						//split ingredient string by ";" 
						String ingredientLabelSegments[] = ingredientName.split(";\\s*");
						//split dose part
						String doseLabelSegments[] = doseLabel.split(";\\s*");
						
						//for each individual ingredient, create object
						for (int i=0;i<ingredientLabelSegments.length;i++){
							
							Utils.sortIngredientOrSpecificAndAddWithDose(doseLabelSegments[i], ingredientLabelSegments[i], brandedDrug);
							
						}
					}
					
					
				} else { //if only one ingredient (no semicolons), proceed normally
					
					Utils.sortIngredientOrSpecificAndAddWithDose(doseLabel, ingredientName, brandedDrug);
					
				}
					
			}

			System.out.println("objects created");
			//once all BrandedDrugs are made, use its attributes to create other entities
			Ontology.generateNonbrandedDrugMap();
			System.out.println("first map done");
			Ontology.generateFormedComponentMap();
			Ontology.generateDosedComponentMap();
			Ontology.generateBrandedDosedComponentMap();
			Ontology.generateBrandedFormedComponentMap();
			Ontology.generateDosedSpecificComponentMap();
			Ontology.generateFormedSpecificComponentMap();
			Ontology.generateBrandedDosedSpecificComponentMap();
			Ontology.generateBrandedFormedSpecificComponentMap();
			System.out.println("All maps generated");

			//organize specific ingredients under ingredients 
			Ontology.linkNonbrandedDrugsWithBrandedDrugs(); //must link branded and nonbranded first 
//			Ontology.organizeSpecificIngredients();
//			Ontology.organizeDosedSpecificComponents();
//			Ontology.organizeFormedSpecificComponents();
			System.out.println("Links created");

		
			//write to CSV 
			Utils.writeIngredientsToCSV(Ontology.ingredientMap, "ingredients");
			Utils.writeSpecificIngredientsToCSV(Ontology.specificIngredientMap);
			Utils.writeBrandedDrugsToCSV(Ontology.brandedDrugMap);
			Utils.writeNonbrandedDrugsToCSV(Ontology.nonbrandedDrugMap);
			Utils.writeDosedComponentsToCSV(Ontology.dosedComponentMap);
			Utils.writeFormedComponentsToCSV(Ontology.formedComponentMap);
			Utils.writeDosedSpecificComponentsToCSV(Ontology.dosedSpecificComponentMap);
			Utils.writeFormedSpecificComponentsToCSV(Ontology.formedSpecificComponentMap);
			Utils.writeBrandedDosedComponentsToCSV(Ontology.brandedDosedComponentMap);
			Utils.writeBrandedFormedComponentsToCSV(Ontology.brandedFormedComponentMap);
			Utils.writeBrandedDosedSpecificComponentsToCSV(Ontology.brandedDosedSpecificComponentMap);
			Utils.writeBrandedFormedSpecificComponentsToCSV(Ontology.brandedFormedSpecificComponentMap);
			System.out.println("Maps written to CSVs");

			
//			
//			//manager pour manipuler les ontologies 
//			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//
//			OWLDataFactory df = OWLManager.getOWLDataFactory();
//
//			//enregistrer en local
//			File OntologieAEnregistrer = new File("/Users/wung/Documents/Ontologies/BDPMOntology.owl");
//			OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
//			IRI documentIRI = IRI.create(OntologieAEnregistrer.toURI());
//
//
//			try {
//				//new ontology
//				OWLOntology ontology = manager.createOntology();
//				manager.saveOntology(ontology, owlxmlFormat, documentIRI);
//
//				//prefixe manager 
//				PrefixManager prefixe = new DefaultPrefixManager("BDPMontology#");
//
//
//				//~~~~~~~~~~~~~~~~~~~~~~~create classes or axioms~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//				//for each branded drug, need to create a class? 
//				OWLClass brandedDrug = OwlUtils.declareClass(df, documentIRI, ontology, "BrandedDrug");
//				OWLClass clinicalDrug = OwlUtils.declareClass(df, documentIRI, ontology, "ClinicalDrug");
//				OWLClass form = OwlUtils.declareClass(df, documentIRI, ontology, "Form");
//				OWLClass dose = OwlUtils.declareClass(df, documentIRI, ontology, "Dose");
//				OWLClass ingredient = OwlUtils.declareClass(df, documentIRI, ontology, "Ingredient");
//				OWLClass specificIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "SpecificIngredient");
//				OWLClass brandName = OwlUtils.declareClass(df, documentIRI, ontology, "BrandName");
//				OWLClass dosedComponent = OwlUtils.declareClass(df, documentIRI, ontology, "DosedComponent");
//				OWLClass dosedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, "DosedSpecificComponent");
//				OWLClass formedComponent = OwlUtils.declareClass(df, documentIRI, ontology, "FormedComponent");
//				OWLClass formedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, "FormedSpecificComponent");
//				
//				OWLClass brandedDosedComponent = OwlUtils.declareClass(df, documentIRI, ontology, "BrandedDosedComponent");
//				OWLClass brandedDosedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, "BrandedDosedSpecificComponent");
//				OWLClass brandedFormedComponent = OwlUtils.declareClass(df, documentIRI, ontology, "BrandedFormedComponent");
//				OWLClass brandedFormedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, "BrandedFormedSpecificComponent");
//				//~~~~~~~~~~~~~~~~~~~~~~~~~properties~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//				
//				// use property to connect to class 
//				OWLObjectProperty hasForm = df.getOWLObjectProperty(IRI.create(prefixe + "#hasForm"));
//				OWLObjectProperty hasIngredients = df.getOWLObjectProperty(IRI.create(prefixe + "#hasIngredients"));
//				OWLObjectProperty hasSpecificIngredients = df.getOWLObjectProperty(IRI.create(prefixe + "#hasSpecificIngredients"));
//				OWLObjectProperty hasDose = df.getOWLObjectProperty(IRI.create(prefixe + "#hasDose"));
//				OWLObjectProperty hasDoseIngredientCombo = df.getOWLObjectProperty(IRI.create(prefixe + "#hasDoseIngredientCombo"));
//				OWLObjectProperty hasDoseSpecificIngredientCombo = df.getOWLObjectProperty(IRI.create(prefixe + "#hasDoseSpecificIngredientCombo"));
//				OWLObjectProperty hasMatchingSpecificIngredient = df.getOWLObjectProperty(IRI.create(prefixe + "#hasMatchingSpecificIngredient"));
//				OWLObjectProperty hasMatchingIngredient = df.getOWLObjectProperty(IRI.create(prefixe + "#hasMatchingIngredient"));
//				OwlUtils.makeInverse(df, ontology, manager, hasMatchingSpecificIngredient, hasMatchingIngredient);
//				OWLObjectProperty hasBrandedDosedComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasBrandedDosedComponent"));
//				OWLObjectProperty hasDosedComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasDosedComponent"));
//				OWLObjectProperty hasDosedSpecificComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasDosedSpecificComponent"));
//				OWLObjectProperty hasBrandedDosedSpecificComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasBrandedDosedSpecificComponent"));
//				OWLObjectProperty hasFormedComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasFormedComponent"));
//				OWLObjectProperty hasFormedSpecificComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasFormedSpecificComponent"));
//				OWLObjectProperty hasBrandedFormedComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasBrandedFormedComponent"));
//				OWLObjectProperty hasBrandedFormedSpecificComponent = df.getOWLObjectProperty(IRI.create(prefixe + "#hasBrandedFormedSpecificComponent"));
//				
//				
//				OWLObjectProperty hasBrandName = df.getOWLObjectProperty(IRI.create(prefixe + "#hasBrandName"));
//				OWLObjectProperty BrandNameOf = df.getOWLObjectProperty(IRI.create(prefixe + "#BrandNameOf"));
//				OwlUtils.makeInverse(df, ontology, manager, hasBrandName, BrandNameOf);
//				//restrict cardinality
//				OWLObjectExactCardinality hasFormRestriction = df.getOWLObjectExactCardinality(1, hasForm);
//	
//				
//				//~~~~~~~~~~~~~~~~~~~~~~~~~create instances (or classes)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//				
//			
//				
//				//form
//				List<OWLClass> listFormClasses = new ArrayList<OWLClass>(); //to contain all classes to be added under Form
//				for (Map.Entry<String, Form> formEntry : Ontology.formMap.entrySet()){
//					OWLClass formClass = OwlUtils.declareClass(df, documentIRI, ontology, formEntry.getKey());
//					listFormClasses.add(formClass);
//					OwlUtils.annotateLabel(df, documentIRI, ontology, formEntry.getValue().getFormLabel(),formClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listFormClasses, form);
//				
//				//dose
//				List<OWLClass> listDoseClasses = new ArrayList<OWLClass>(); //to contain all classes to be added under Dose
//				for (Map.Entry<String, Dose> doseEntry : Ontology.doseMap.entrySet()){
//					OWLClass doseClass = OwlUtils.declareClass(df, documentIRI, ontology, doseEntry.getKey());
//					listDoseClasses.add(doseClass);
//					OwlUtils.annotateLabel(df, documentIRI, ontology, doseEntry.getValue().doseLabel, doseClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listDoseClasses, dose);			
//				
//				//ingredients
//				List<OWLClass> listIngredientClasses = new ArrayList<OWLClass>(); // to be added under Ingredients
//				for (Map.Entry<String, Ingredient> ingredientEntry : Ontology.ingredientMap.entrySet()){
//					OWLClass ingredientClass = OwlUtils.declareClass(df, documentIRI, ontology, "i"+ingredientEntry.getKey()); //create class with i+codeSubstance as URI
//					listIngredientClasses.add(ingredientClass);
//					//go through alternate names set to collect into a String 
//					String commentAlternateNames = "";
//					for (String alternateName : ingredientEntry.getValue().otherNames){ 
//						commentAlternateNames += alternateName + ",";
//					}
//					OwlUtils.annotateComment(df, documentIRI, ontology, commentAlternateNames, ingredientClass, "fr"); //put string of alternate names as comment 
//					//add matching specific ingredients
//					List<OWLClass> matchingSpecificIngredientClasses = new ArrayList<OWLClass>();
//					for (SpecificIngredient matchingSpecificIngredient : ingredientEntry.getValue().matchingSpecificIngredients){
//						OWLClass matchingSpecificIngredientClass = OwlUtils.declareClass(df, documentIRI, ontology, "si"+matchingSpecificIngredient.codeSubstance);
//						matchingSpecificIngredientClasses.add(matchingSpecificIngredientClass);
//						OwlUtils.createSomeRestriction(df, ontology, manager, ingredientClass, hasMatchingSpecificIngredient, matchingSpecificIngredientClass);
//					}
//					OwlUtils.annotateLabel(df, documentIRI, ontology, ingredientEntry.getValue().ingredientName, ingredientClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listIngredientClasses, ingredient);
//				
//				//specific ingredients
//				List<OWLClass> listSpecificIngredientClasses = new ArrayList<OWLClass>(); // to be added under SpecificIngredients
//				for (Map.Entry<String, SpecificIngredient> spIngredientEntry : Ontology.specificIngredientMap.entrySet()){
//					OWLClass spIngredientClass = OwlUtils.declareClass(df, documentIRI, ontology, "si"+spIngredientEntry.getKey()); //create class with si+codeSubstance as URI (need to separate ingredients from specific ingredients as some overlap; also codeSubstance could potentially interfere with doses)
//					listSpecificIngredientClasses.add(spIngredientClass);
//					OwlUtils.annotateLabel(df, documentIRI, ontology, spIngredientEntry.getValue().specificIngredientName,spIngredientClass, "fr");
//					String commentAltNames = "";
//					for (String alternateName : spIngredientEntry.getValue().otherNames){
//						commentAltNames += alternateName + ",";
//					}
//					OwlUtils.annotateComment(df, documentIRI, ontology, commentAltNames, spIngredientClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listSpecificIngredientClasses, specificIngredient);
//				
//				//dosed component TODO
//				List<OWLClass> listDosedComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, DosedComponent> dosedComponentEntry : Ontology.dosedComponentMap.entrySet()){
//					OWLClass dosedComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, dosedComponentEntry.getKey());
//					listDosedComponentClasses.add(dosedComponentClass);
//					String generatedLabel = "";//string to which we will add all the ingredients and doses
//					List<OWLClassExpression> allDosedIngredients = new ArrayList<OWLClassExpression>();
//					for (Map.Entry<Ingredient, Dose>ingredDoseEntry : dosedComponentEntry.getValue().getIngredientDoseMap().entrySet()){
//						generatedLabel += ingredDoseEntry.getKey().ingredientName + "|" + ingredDoseEntry.getValue().doseLabel + ",";
//						//while we're going through the ingredients, add them under hasIngredients
//						OWLClass getIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "i"+ingredDoseEntry.getKey().codeSubstance);
//						OWLClassExpression hasIngredientThisIngredient = OwlUtils.createSomeRestriction(df, ontology, manager, dosedComponentClass, hasIngredients, getIngredient); //add ingredient to hasIngredients
//						// do the same for Doses 
//						OWLClass getDose = OwlUtils.declareClass(df, documentIRI, ontology, ingredDoseEntry.getValue().doseLabel);
//						OWLClassExpression hasDoseThisDose = df.getOWLObjectSomeValuesFrom(hasDose, getDose); //add dose to hasDose
//						//put dose ingredient combo
//						OWLClassExpression intersectDosedIngredient = df.getOWLObjectIntersectionOf(hasIngredientThisIngredient, hasDoseThisDose); //first make intersect
//						OWLClassExpression hasDosedIngredientTheseDosedIngreds=OwlUtils.createSomeRestrictionIntersect(df, ontology, manager, dosedComponentClass, hasDoseIngredientCombo, intersectDosedIngredient);
//						allDosedIngredients.add(hasDosedIngredientTheseDosedIngreds);
//					}
//					//put label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, generatedLabel,dosedComponentClass, "fr");
//					//equivalent 
//					//intersect all (has dosed ingred B AND has dosed ingred B etc)
//					if(dosedComponentEntry.getValue().getIngredientDoseMap().size()>0){ //only make equivalent statement if there are ingredients
//						OWLClassExpression intersectAllHasDosedIngreds = df.getOWLObjectIntersectionOf(allDosedIngredients);
//						OwlUtils.makeEquivalent(df, ontology, manager, dosedComponentClass, intersectAllHasDosedIngreds);
//					}
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listDosedComponentClasses, dosedComponent);
//				
//				//dosed specific component
//				List<OWLClass> listDosedSpecificComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, DosedSpecificComponent> dosedSpecificComponentEntry : Ontology.dosedSpecificComponentMap.entrySet()){
//					OWLClass dosedSpecificComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, dosedSpecificComponentEntry.getKey());
//					listDosedSpecificComponentClasses.add(dosedSpecificComponentClass);
//					String ingredDoseString = ""; //string to which we will add all the specific ingredients and doses
//
//					List<OWLClassExpression> allDosedSpecificIngredients = new ArrayList<OWLClassExpression>();
//					for (Map.Entry<SpecificIngredient, Dose>ingredDoseEntry : dosedSpecificComponentEntry.getValue().getSpecificIngredientDoseMap().entrySet()){
//						ingredDoseString += ingredDoseEntry.getKey().specificIngredientName + "|" + ingredDoseEntry.getValue().doseLabel + ",";
//						//while we're going through the specific ingredients, add them under hasSpecificIngredients
//						OWLClass getSpecificIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "si"+ingredDoseEntry.getKey().codeSubstance);
//						OWLClassExpression hasSpecificIngredientThisSpecificIngredient = OwlUtils.createSomeRestriction(df, ontology, manager, dosedSpecificComponentClass, hasSpecificIngredients, getSpecificIngredient); //add spec ingredient to hasSpecificIngredients
//						// do the same for Doses 
//						OWLClass getDose = OwlUtils.declareClass(df, documentIRI, ontology, ingredDoseEntry.getValue().doseLabel);
//						OWLClassExpression hasDoseThisDose = df.getOWLObjectSomeValuesFrom(hasDose, getDose); //add dose to hasDose
//						//put dose ingredient combo
//						OWLClassExpression intersectDosedSpecificIngredient = (ingredDoseEntry.getValue().doseLabel.length()==0) ? hasSpecificIngredientThisSpecificIngredient : df.getOWLObjectIntersectionOf(hasSpecificIngredientThisSpecificIngredient, hasDoseThisDose); //first make intersect
//						OWLClassExpression hasDosedIngredientTheseDosedIngreds= OwlUtils.createSomeRestrictionIntersect(df, ontology, manager, dosedSpecificComponentClass, hasDoseSpecificIngredientCombo, intersectDosedSpecificIngredient);
//						allDosedSpecificIngredients.add(hasDosedIngredientTheseDosedIngreds);
//					}
//					//put label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, ingredDoseString,dosedSpecificComponentClass, "fr");
//					//equivalent 
//					//intersect all (has dosed ingred B AND has dosed ingred B etc)
//					if(dosedSpecificComponentEntry.getValue().getSpecificIngredientDoseMap().size()>0){ //only make equivalent statement if there are ingredients
//						OWLClassExpression intersectAllHasDosedSpecificIngreds = df.getOWLObjectIntersectionOf(allDosedSpecificIngredients);
//						OwlUtils.makeEquivalent(df, ontology, manager, dosedSpecificComponentClass, intersectAllHasDosedSpecificIngreds);
//					}
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listDosedSpecificComponentClasses, dosedSpecificComponent);
//				
//				//formed component 
//				List<OWLClass> listFormedComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, FormedComponent> formedComponentEntry : Ontology.formedComponentMap.entrySet()){
//					OWLClass formedComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, formedComponentEntry.getKey());
//					listFormedComponentClasses.add(formedComponentClass);
//					String generatedLabel = formedComponentEntry.getValue().getForm().getFormLabel() + "|"; //form + all ingredients. first add form
//					List<OWLClass> containedIngredients = new ArrayList<OWLClass>();
//					List<OWLClassExpression> hasIngredientsList = new ArrayList<OWLClassExpression>();
//					if (formedComponentEntry.getValue().getIngredientsSet().size()>0){
//						for (Ingredient ingred : formedComponentEntry.getValue().getIngredientsSet()){
//							generatedLabel += ingred.ingredientName + ",";
//							//while we're going through the ingredients, add ingredients under hasIngredients
//							OWLClass getIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "i"+ingred.codeSubstance);
//							OWLClassExpression hasIngredThisIngred=OwlUtils.createSomeRestriction(df, ontology, manager, formedComponentClass, hasIngredients, getIngredient); //add ingredient to hasIngredients
//							
//							//add ingreds list to be added to union
//							containedIngredients.add(getIngredient);
//							// add restriction to intersection (has ingredientA and has ingredientB and has ingredientC)
//							hasIngredientsList.add(hasIngredThisIngred);
//						}
//						//has only (A or B or C)
//						OWLClassExpression unionContainedIngredients = df.getOWLObjectUnionOf(containedIngredients);  //first make union of all contained ingreds
//						OwlUtils.createOnlyRestriction(df, ontology, manager, formedComponentClass, hasIngredients, unionContainedIngredients); //then put all those  ingredients into an Only restriction 
//					}
//					//put label 
//					OwlUtils.annotateLabel(df, documentIRI, ontology, generatedLabel, formedComponentClass, "fr");
//					//add form
//					OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, formedComponentEntry.getValue().getForm().getFormLabel());
//					OWLClassExpression hasFormThisForm = OwlUtils.createSomeRestriction(df, ontology, manager, formedComponentClass, hasForm, newFormClass);	
//					//equivalent definition (has A) + (has B) ...  + (has form) 
//					OWLClassExpression intersectHasAllSpIngredients = df.getOWLObjectIntersectionOf(hasIngredientsList);
//					OWLClassExpression intersectContainedIngredsAndForm = (containedIngredients.size()==0) ? hasFormThisForm :  df.getOWLObjectIntersectionOf(intersectHasAllSpIngredients, hasFormThisForm);
//					OwlUtils.makeEquivalent(df, ontology, manager, formedComponentClass, intersectContainedIngredsAndForm);
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listFormedComponentClasses, formedComponent);
//				
//				//formed specific component 
//				List<OWLClass> listFormedSpecificComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, FormedSpecificComponent> formedSpecificComponentEntry : Ontology.formedSpecificComponentMap.entrySet()){
//					OWLClass formedSpecificComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, formedSpecificComponentEntry.getKey());
//					listFormedSpecificComponentClasses.add(formedSpecificComponentClass);
//					String generatedLabel = formedSpecificComponentEntry.getValue().getForm().getFormLabel() + "|"; //form + all ingredients. first add form
//					List<OWLClass> containedSpecificIngredients = new ArrayList<OWLClass>();
//					List<OWLClassExpression> hasSpIngredientsList = new ArrayList<OWLClassExpression>();
//					if(formedSpecificComponentEntry.getValue().getSpecificIngredientSet().size()>0){
//						
//						for (SpecificIngredient spIngredient : formedSpecificComponentEntry.getValue().getSpecificIngredientSet()){
//							generatedLabel += spIngredient.specificIngredientName + ",";
//							//while we're going through the spec ingredients, add them as specific ingredients property
//							OWLClass getSpecificIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "si"+spIngredient.codeSubstance);
//							OWLClassExpression hasSpecIngredThisSpecIngred = OwlUtils.createSomeRestriction(df, ontology, manager, formedSpecificComponentClass, hasSpecificIngredients, getSpecificIngredient); //add spec ingredient to hasSpecificIngredients
//							//add spec ingreds list to be added to union
//							containedSpecificIngredients.add(getSpecificIngredient);
//							// add restriction to intersection (has ingredientA and has ingredientB and has ingredientC)
//							hasSpIngredientsList.add(hasSpecIngredThisSpecIngred);
//						}
//						//has only (A or B or C) restriction
//						OWLClassExpression unionContainedSpecificIngredients = df.getOWLObjectUnionOf(containedSpecificIngredients);  //first make union of all contained ingreds
//						OwlUtils.createOnlyRestriction(df, ontology, manager, formedSpecificComponentClass, hasSpecificIngredients, unionContainedSpecificIngredients); //then put all those  ingredients into an Only restriction
//						//put form 
//						OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, formedSpecificComponentEntry.getValue().getForm().getFormLabel());
//						OWLClassExpression hasFormThisForm = OwlUtils.createSomeRestriction(df, ontology, manager, formedSpecificComponentClass, hasForm, newFormClass);	
//						//equivalent definition (has A) + (has B) ...  + (has form) 
//						OWLClassExpression intersectHasAllSpIngredients = df.getOWLObjectIntersectionOf(hasSpIngredientsList);
//						OWLClassExpression intersectContainedIngredsAndForm = (containedSpecificIngredients.size()==0) ? hasFormThisForm :  df.getOWLObjectIntersectionOf(intersectHasAllSpIngredients, hasFormThisForm);
//						OwlUtils.makeEquivalent(df, ontology, manager, formedSpecificComponentClass, intersectContainedIngredsAndForm);
//					}
//				
//					//label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, generatedLabel, formedSpecificComponentClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listFormedSpecificComponentClasses, formedSpecificComponent);
//
//				//brand names
//				List<OWLClass> listBrandNameClasses = new ArrayList<OWLClass>(); // to be added under brandnames
//				for (Map.Entry<String, BrandName> brandNameEntry : Ontology.brandNameMap.entrySet()){
//					OWLClass brandNameClass = OwlUtils.declareClass(df, documentIRI, ontology, brandNameEntry.getKey()); //create class with map key as URI
//					listBrandNameClasses.add(brandNameClass);
//					OwlUtils.annotateLabel(df, documentIRI, ontology, brandNameEntry.getValue().brandNameLabel, brandNameClass, "fr");
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandNameClasses, brandName);
//				
//				
//				
//				//clinical drug
//				List<OWLClass> listClinicalDrugClasses = new ArrayList<OWLClass>(); // list to collect all clinical drug objects 
//				for (Map.Entry<String, NonbrandedDrug> nonbrandedDrugEntry : Ontology.nonbrandedDrugMap.entrySet()){
//					OWLClass nonbrandedDrugClass = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getKey());
//					listClinicalDrugClasses.add(nonbrandedDrugClass);
//					//label temporary (ingreds and specific ingreds dose maps to string)
//					OwlUtils.annotateLabel(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().getIngredientDoseMap().toString() + nonbrandedDrugEntry.getValue().getSpecificIngredientDoseMap().toString(), nonbrandedDrugClass, "fr");
//					//put form 
//					OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().getForm().getFormLabel());
//					OwlUtils.createSomeRestriction(df, ontology, manager, nonbrandedDrugClass, hasForm, newFormClass);	
//					//put dosed component
//					OWLClass newDosedComponent = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().dosedComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, nonbrandedDrugClass, hasDosedComponent, newDosedComponent);
//					//put dosed specific component
//					OWLClass newDosedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().dosedSpecificComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, nonbrandedDrugClass, hasDosedSpecificComponent, newDosedSpecificComponent);
//					//put formed component
//					OWLClass newFormedComponent = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().formedComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, nonbrandedDrugClass, hasFormedComponent, newFormedComponent);
//					//put formed specific component 
//					OWLClass newFormedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, nonbrandedDrugEntry.getValue().formedSpecificComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, nonbrandedDrugClass, hasFormedSpecificComponent, newFormedSpecificComponent);
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listClinicalDrugClasses, clinicalDrug); //take list and put all as subclass of clinical drug class
//				
//				//branded drug
//				List<OWLClass> listBrandedDrugClasses = new ArrayList<OWLClass>(); // list to collect all branded drug objects 
//				for (Map.Entry<String, BrandedDrug> brandedDrugEntry : Ontology.brandedDrugMap.entrySet()){
//					OWLClass brandedDrugClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getKey());
//					listBrandedDrugClasses.add(brandedDrugClass);
//					//put label 
//					OwlUtils.annotateLabel(df, documentIRI, ontology, brandedDrugEntry.getValue().brandName.brandNameLabel, brandedDrugClass, "fr"); //brand name as label for now
//					//put form 
//					OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().getForm().getFormLabel());
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasForm, newFormClass);	
//					//put branded dosed component
//					OWLClass newBrandedDosedComponent = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().brandedDosedComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasBrandedDosedComponent, newBrandedDosedComponent);
//					//put dosed specific component 
//					OWLClass newBrandedDosedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().brandedDosedSpecificComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasBrandedDosedSpecificComponent, newBrandedDosedSpecificComponent);
//					//put brandname
//					OWLClass newBrandName = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().brandName.brandNameLabel);
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasBrandName, newBrandName);
//					//put branded formed component 
//					OWLClass newBrandedFormedComponent = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().brandedFormedComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasBrandedFormedComponent, newBrandedFormedComponent);
//					//put branded formed specific component 
//					OWLClass newBrandedFormedSpecificComponent = OwlUtils.declareClass(df, documentIRI, ontology, brandedDrugEntry.getValue().brandedFormedSpecificComponent.generateMapKey());
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDrugClass, hasBrandedFormedSpecificComponent, newBrandedFormedComponent);
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandedDrugClasses, brandedDrug); //take list and put all as subclass of branded class
//				
//			
//				
//				
//				//branded dosed component TODO
//				List<OWLClass> listBrandedDosedComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, BrandedDosedComponent> brandedDosedComponentEntry : Ontology.brandedDosedComponentMap.entrySet()){
//					OWLClass brandedDosedComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedDosedComponentEntry.getKey());
//					listBrandedDosedComponentClasses.add(brandedDosedComponentClass);
//					String ingredDoseString = "";//string to which we will add all the ingredients and doses
//					List<OWLClassExpression> allDosedIngredients = new ArrayList<OWLClassExpression>();
//					for (Map.Entry<Ingredient, Dose>ingredDoseEntry : brandedDosedComponentEntry.getValue().getIngredientDoseMap().entrySet()){
//						ingredDoseString += ingredDoseEntry.getKey().ingredientName + "|" + ingredDoseEntry.getValue().doseLabel + ",";
//						//while we're going through the ingredients, add them under hasIngredients
//						OWLClass getIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "i"+ingredDoseEntry.getKey().codeSubstance);
//						OWLClassExpression hasIngredientThisIngredient = OwlUtils.createSomeRestriction(df, ontology, manager, brandedDosedComponentClass, hasIngredients, getIngredient); //add ingredient to hasIngredients
//						// do the same for Doses 
//						OWLClass getDose = OwlUtils.declareClass(df, documentIRI, ontology, ingredDoseEntry.getValue().doseLabel);
//						OWLClassExpression hasDoseThisDose = df.getOWLObjectSomeValuesFrom(hasDose, getDose); //add dose to hasDose
//						//put dose ingredient combo
//						OWLClassExpression intersectDosedIngredient = (ingredDoseEntry.getValue().doseLabel.length()==0) ? hasIngredientThisIngredient : df.getOWLObjectIntersectionOf(hasIngredientThisIngredient, hasDoseThisDose); //first make intersect
//						OWLClassExpression hasDosedIngredientTheseDosedIngreds= OwlUtils.createSomeRestrictionIntersect(df, ontology, manager, brandedDosedComponentClass, hasDoseIngredientCombo, intersectDosedIngredient);
//						allDosedIngredients.add(hasDosedIngredientTheseDosedIngreds);
//					}
//					//put label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, ingredDoseString,brandedDosedComponentClass, "fr");
//					//put brandname
//					OWLClass newBrandName = OwlUtils.declareClass(df, documentIRI, ontology, brandedDosedComponentEntry.getValue().brandName.brandNameLabel);
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDosedComponentClass, hasBrandName, newBrandName);
//					//equivalent 
//					//intersect all (has dosed ingred B AND has dosed ingred B etc)
//					if(brandedDosedComponentEntry.getValue().getIngredientDoseMap().size()>0){ //only make equivalent statement if there are ingredients
//						OWLClassExpression intersectAllHasDosedIngreds = df.getOWLObjectIntersectionOf(allDosedIngredients);
//						OwlUtils.makeEquivalent(df, ontology, manager, brandedDosedComponentClass, intersectAllHasDosedIngreds);
//					}
//					
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandedDosedComponentClasses, brandedDosedComponent);
//				
//				//branded dosed specific component TODO
//				List<OWLClass> listBrandedDosedSpecificComponentClasses = new ArrayList<OWLClass>(); //collect all the classes to later put all of them under branded dosed specific component 
//				for (Map.Entry<String, BrandedDosedSpecificComponent> brandedDosedSpecificComponentEntry : Ontology.brandedDosedSpecificComponentMap.entrySet()){
//					OWLClass brandedDosedSpecificComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedDosedSpecificComponentEntry.getKey()); //create class w/ code cis 
//					listBrandedDosedSpecificComponentClasses.add(brandedDosedSpecificComponentClass); //add to list that will all go in as subclasses later 
//					String generatedLabel = "";//string to which we will add all the ingredients and doses
//					List<OWLClassExpression> allDosedSpecificIngredients = new ArrayList<OWLClassExpression>();
//					for (Map.Entry<SpecificIngredient, Dose>ingredDoseEntry : brandedDosedSpecificComponentEntry.getValue().getSpecificIngredientDoseMap().entrySet()){
//						generatedLabel += ingredDoseEntry.getKey().specificIngredientName + "|" + ingredDoseEntry.getValue().doseLabel + ",";
//						//while we're going through the specific ingredients, add them under hasSpecificIngredients
//						OWLClass getSpecificIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "si"+ingredDoseEntry.getKey().codeSubstance);
//						OWLClassExpression hasSpecificIngredientThisSpecificIngredient = OwlUtils.createSomeRestriction(df, ontology, manager, brandedDosedSpecificComponentClass, hasSpecificIngredients, getSpecificIngredient); //add spec ingredient to hasSpecificIngredients
//						// do the same for Doses 
//						OWLClass getDose = OwlUtils.declareClass(df, documentIRI, ontology, ingredDoseEntry.getValue().doseLabel);
//						OWLClassExpression hasDoseThisDose = df.getOWLObjectSomeValuesFrom(hasDose, getDose); //add dose to hasDose
//						//put dose ingredient combo
//						OWLClassExpression intersectDosedSpecificIngredient = (ingredDoseEntry.getValue().doseLabel.length()==0) ? hasSpecificIngredientThisSpecificIngredient : df.getOWLObjectIntersectionOf(hasSpecificIngredientThisSpecificIngredient, hasDoseThisDose); //first make intersect. if no dose, leave it out. 
//						OWLClassExpression hasDosedIngredientTheseDosedIngreds= OwlUtils.createSomeRestrictionIntersect(df, ontology, manager, brandedDosedSpecificComponentClass, hasDoseSpecificIngredientCombo, intersectDosedSpecificIngredient); //then put specific ingred + dose together
//						allDosedSpecificIngredients.add(hasDosedIngredientTheseDosedIngreds);
//					}
//					//put label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, generatedLabel,brandedDosedSpecificComponentClass, "fr");
//					//put brandname
//					OWLClass newBrandName = OwlUtils.declareClass(df, documentIRI, ontology, brandedDosedSpecificComponentEntry.getValue().brandName.brandNameLabel);
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedDosedSpecificComponentClass, hasBrandName, newBrandName);
//					//equivalent 
//					//intersect all (has dosed ingred B AND has dosed ingred B etc)
//					if(brandedDosedSpecificComponentEntry.getValue().getSpecificIngredientDoseMap().size()>0){ //only make equivalent statement if there are ingredients
//						OWLClassExpression intersectAllHasDosedSpecificIngreds = df.getOWLObjectIntersectionOf(allDosedSpecificIngredients);
//						OwlUtils.makeEquivalent(df, ontology, manager, brandedDosedSpecificComponentClass, intersectAllHasDosedSpecificIngreds);
//					}
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandedDosedSpecificComponentClasses, brandedDosedSpecificComponent);
//				
//				//branded formed component
//				List<OWLClass> listBrandedFormedComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, BrandedFormedComponent> brandedFormedComponentEntry : Ontology.brandedFormedComponentMap.entrySet()){
//					OWLClass brandedFormedComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedComponentEntry.getKey());
//					listBrandedFormedComponentClasses.add(brandedFormedComponentClass);
//					String label = "";//string to which we will add all the ingredients and doses
//					List<OWLClass> containedIngredients = new ArrayList<OWLClass>();
//					List<OWLClassExpression> hasIngredientsList = new ArrayList<OWLClassExpression>();
//					if (brandedFormedComponentEntry.getValue().getIngredientsSet().size()>0){
//						for (Ingredient ingredientEntry : brandedFormedComponentEntry.getValue().getIngredientsSet()){
//							label += ingredientEntry.ingredientName + ",";
//							//while we're going through the ingredients, add them as ingredients property
//							OWLClass getIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "i"+ingredientEntry.codeSubstance);
//							OWLClassExpression hasIngredThisIngred=OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedComponentClass, hasIngredients, getIngredient); //add ingredient to hasIngredients
//							
//							//add ingreds list to be added to union
//							containedIngredients.add(getIngredient);
//							// add restriction to intersection (has ingredientA and has ingredientB and has ingredientC)
//							hasIngredientsList.add(hasIngredThisIngred);
//						}
//					}
//					//has only (A or B or C) 
//					OWLClassExpression unionContainedIngredients = df.getOWLObjectUnionOf(containedIngredients);  //first make union of all contained ingreds
//					OwlUtils.createOnlyRestriction(df, ontology, manager, brandedFormedComponentClass, hasIngredients, unionContainedIngredients); //then put all those ingredients into an Only restriction 
//					//label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, label,brandedFormedComponentClass, "fr");
//					//put form 
//					OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedComponentEntry.getValue().getForm().getFormLabel());
//					OWLClassExpression hasFormThisForm = OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedComponentClass, hasForm, newFormClass);	
//					//put brandname
//					OWLClass newBrandName = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedComponentEntry.getValue().brandName.brandNameLabel);
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedComponentClass, hasBrandName, newBrandName);
//					//equivalent definition (has A) + (has B) ...  + (has form) 
//					OWLClassExpression intersectHasAllSpIngredients = df.getOWLObjectIntersectionOf(hasIngredientsList);
//					OWLClassExpression intersectContainedIngredsAndForm = (containedIngredients.size()==0) ? hasFormThisForm :  df.getOWLObjectIntersectionOf(intersectHasAllSpIngredients, hasFormThisForm);
//					OwlUtils.makeEquivalent(df, ontology, manager, brandedFormedComponentClass, intersectContainedIngredsAndForm);
//					
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandedFormedComponentClasses, brandedFormedComponent);
//				
//				//branded formed specific component TODO
//				List<OWLClass> listBrandedFormedSpecificComponentClasses = new ArrayList<OWLClass>();
//				for (Map.Entry<String, BrandedFormedSpecificComponent> brandedFormedSpecificComponentEntry : Ontology.brandedFormedSpecificComponentMap.entrySet()){
//					OWLClass brandedFormedSpecificComponentClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedSpecificComponentEntry.getKey());
//					listBrandedFormedSpecificComponentClasses.add(brandedFormedSpecificComponentClass);
//					String label = "";//string to which we will add all the specific ingredients and doses
//					
//					List<OWLClass> containedSpecificIngredients = new ArrayList<OWLClass>();
//					List<OWLClassExpression> hasSpIngredientsList = new ArrayList<OWLClassExpression>();
//					if (brandedFormedSpecificComponentEntry.getValue().getSpecificIngredientSet().size()>0){
//						for (SpecificIngredient specIngredientEntry : brandedFormedSpecificComponentEntry.getValue().getSpecificIngredientSet()){
//							label += specIngredientEntry.specificIngredientName + ",";
//							//while we're going through the spec ingredients, add them as spec ingredients property
//							OWLClass getSpecificIngredient = OwlUtils.declareClass(df, documentIRI, ontology, "si"+ specIngredientEntry.codeSubstance);
//							OWLClassExpression hasSpecIngredThisSpecIngred= OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedSpecificComponentClass, hasSpecificIngredients, getSpecificIngredient); //add spec ingredient to hasSpecificIngredients
//							//add specific ingreds list to be added to union (ingredientA or ingredientB or ingredientC) 
//							containedSpecificIngredients.add(getSpecificIngredient);
//							// add restriction to intersection (has ingredientA and has ingredientB and has ingredientC)
//							hasSpIngredientsList.add(hasSpecIngredThisSpecIngred);
//						}
//					}
//					//label
//					OwlUtils.annotateLabel(df, documentIRI, ontology, label, brandedFormedSpecificComponentClass, "fr");
//					//put form 
//					OWLClass newFormClass = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedSpecificComponentEntry.getValue().getForm().getFormLabel());
//					OWLClassExpression hasFormThisForm = OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedSpecificComponentClass, hasForm, newFormClass);	
//					//put brandname
//					OWLClass newBrandName = OwlUtils.declareClass(df, documentIRI, ontology, brandedFormedSpecificComponentEntry.getValue().brandName.brandNameLabel);
//					OwlUtils.createSomeRestriction(df, ontology, manager, brandedFormedSpecificComponentClass, hasBrandName, newBrandName);
//					//has only (A or B or C) 
//					OWLClassExpression unionContainedSpecificIngredients = df.getOWLObjectUnionOf(containedSpecificIngredients);  //specific ingreds (A or B or C) 
//					OwlUtils.createOnlyRestriction(df, ontology, manager, brandedFormedSpecificComponentClass, hasSpecificIngredients, unionContainedSpecificIngredients); //has only (A or B or C) 
//					//equivalent definition (has A) + (has B) ...  + (has form) 
//					OWLClassExpression intersectHasAllSpIngredients = df.getOWLObjectIntersectionOf(hasSpIngredientsList);
//					OWLClassExpression intersectContainedIngredsAndForm = (containedSpecificIngredients.size()==0) ? hasFormThisForm : df.getOWLObjectIntersectionOf(intersectHasAllSpIngredients, hasFormThisForm);
//					OwlUtils.makeEquivalent(df, ontology, manager, brandedFormedSpecificComponentClass, intersectContainedIngredsAndForm);
//					
//				}
//				OwlUtils.putAllAsSubclass(df, ontology, listBrandedFormedSpecificComponentClasses, brandedFormedSpecificComponent);
//				
//					
//				
////				manager.addAxiom(pizzaOntology, propertyAssertion);
//
//				//save ontology
//				manager.saveOntology(ontology, documentIRI);
//				//manager.removeOntology(pizzaOntology);
//
//
//
//			} catch (OWLOntologyCreationException e) {
//				e.printStackTrace();
//			} catch (OWLOntologyStorageException e) {
//				e.printStackTrace();
//			} 
//



		

		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		
		
	}



}