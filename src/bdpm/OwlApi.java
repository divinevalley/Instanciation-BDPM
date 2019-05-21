package bdpm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
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


public class OwlApi {
	public static void main( String[] args )
	{
		//manager pour manipuler les ontologies 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLDataFactory df = OWLManager.getOWLDataFactory();

		//enregistrer en local
		File OntologieAEnregistrer = new File("/Users/wung/Documents/Ontologies/BDPMOntology.owl");
		OWLXMLDocumentFormat owlxmlFormat = new OWLXMLDocumentFormat();
		IRI documentIRI = IRI.create(OntologieAEnregistrer.toURI());


		try {
			//new ontology
			OWLOntology ontology = manager.createOntology();
			manager.saveOntology(ontology, owlxmlFormat, documentIRI);


			//prefixe manager 
			PrefixManager prefixe = new DefaultPrefixManager("http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/ontology#");

			//Annotation
			OWLAnnotation commentAnno = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("A pizza ontology that describes various pizzas based on their toppings", "en"));
			df.getOWLAnnotationAssertionAxiom(documentIRI, commentAnno);


			//~~~~~~~~~~~~~~~~~~~~~~~create classes or axioms~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//classes topping and base
			OWLClass pizza = OwlUtils.declareClass(df, documentIRI, ontology, "Pizza");
			OWLClass topping = OwlUtils.declareClass(df, documentIRI, ontology, "PizzaTopping");
			OWLClass base = OwlUtils.declareClass(df, documentIRI, ontology, "PizzaBase");
			OWLClass deepPanBase = OwlUtils.declareClass(df, documentIRI, ontology, "DeepPanBase");


			//make classes disjoint?
			List<OWLClass> disjointClasses = new ArrayList<OWLClass>();
			Collections.addAll(disjointClasses, topping, pizza, base);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointClasses);

			OwlUtils.putAsSubclass(df, ontology, deepPanBase, base); //deep pan base is under base



			//toppings
			OWLClass cheese = OwlUtils.declareClass(df, documentIRI, ontology, "CheeseTopping");
			OWLClass mozzarella = OwlUtils.declareClass(df, documentIRI, ontology, "MozzarellaTopping");
			OWLClass parmesan = OwlUtils.declareClass(df, documentIRI, ontology, "ParmesanTopping");

			//put as subclasses
			List<OWLClass> cheeseSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(cheeseSubclasses, mozzarella, parmesan); //create list of all cheese subclasses
			OwlUtils.putAllAsSubclass(df, ontology, cheeseSubclasses, cheese); //put all cheese subclasses under overall cheese class

			OWLClass meat = OwlUtils.declareClass(df, documentIRI, ontology, "MeatTopping");
			OWLClass ham = OwlUtils.declareClass(df, documentIRI, ontology, "HamTopping");
			OWLClass pepperoni = OwlUtils.declareClass(df, documentIRI, ontology, "PepperoniTopping");
			OWLClass salami = OwlUtils.declareClass(df, documentIRI, ontology, "SalamiTopping");
			OWLClass spicybeef = OwlUtils.declareClass(df, documentIRI, ontology, "SpicyBeefTopping");

			//put as subclasses
			List<OWLClass> meatSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(meatSubclasses, ham, pepperoni, salami, spicybeef); //create list of all meat subclasses
			OwlUtils.putAllAsSubclass(df, ontology, meatSubclasses, meat); //put all meat subclasses under meat class

			OWLClass seafood = OwlUtils.declareClass(df, documentIRI, ontology, "SeafoodTopping");
			OWLClass anchovy = OwlUtils.declareClass(df, documentIRI, ontology, "AnchovyTopping");
			OWLClass prawn = OwlUtils.declareClass(df, documentIRI, ontology, "PrawnTopping");
			OWLClass tuna = OwlUtils.declareClass(df, documentIRI, ontology, "TunaTopping");

			//put as subclasses
			List<OWLClass> seafoodSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(seafoodSubclasses, anchovy, prawn, tuna); //create list of all seafood subclasses
			OwlUtils.putAllAsSubclass(df, ontology, seafoodSubclasses, seafood); //put all seafood subclasses under seafood overall class


			OWLClass vegetable = OwlUtils.declareClass(df, documentIRI, ontology, "VegetableTopping");
			OWLClass caper = OwlUtils.declareClass(df, documentIRI, ontology, "CaperTopping");
			OWLClass mushroom = OwlUtils.declareClass(df, documentIRI, ontology, "MushroomTopping");
			OWLClass olive = OwlUtils.declareClass(df, documentIRI, ontology, "OliveTopping");
			OWLClass onion = OwlUtils.declareClass(df, documentIRI, ontology, "OnionTopping");
			OWLClass tomato = OwlUtils.declareClass(df, documentIRI, ontology, "TomatoTopping");
			OWLClass pepper = OwlUtils.declareClass(df, documentIRI, ontology, "PepperTopping");

			List<OWLClass> vegSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(vegSubclasses, caper, mushroom, olive, onion, tomato, pepper); //create list of all veg subclasses
			OwlUtils.putAllAsSubclass(df, ontology, vegSubclasses, vegetable); //put all veg subclasses under veg class

			OWLClass redPepper = OwlUtils.declareClass(df, documentIRI, ontology, "RedPepperTopping");
			OWLClass greenPepper = OwlUtils.declareClass(df, documentIRI, ontology, "GreenPepperTopping");
			OWLClass jalapenoPepper = OwlUtils.declareClass(df, documentIRI, ontology, "JalapenoPepperTopping");

			List<OWLClass> pepperSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(pepperSubclasses, redPepper, greenPepper, jalapenoPepper); //create list of all pepper subclasses
			OwlUtils.putAllAsSubclass(df, ontology, pepperSubclasses, pepper); //put all pepper subclasses under pepper class

			//put toppings subclasses all under pizzatopping class
			List<OWLClass> toppingSubclasses = new ArrayList<OWLClass>();
			Collections.addAll(toppingSubclasses, cheese, meat, seafood, vegetable);
			OwlUtils.putAllAsSubclass(df, ontology, toppingSubclasses, topping);


			//make topping classes disjoint
			List<OWLClass> disjointClassesToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointClassesToppings, cheese, meat, seafood, vegetable);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointClassesToppings);

			//make topping subclasses disjoint
			List<OWLClass> disjointCheeseToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointCheeseToppings, mozzarella, parmesan);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointCheeseToppings);

			List<OWLClass> disjointMeatToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointMeatToppings,ham, pepperoni, salami, spicybeef);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointMeatToppings);

			List<OWLClass> disjointSeafoodToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointSeafoodToppings, anchovy, prawn, tuna);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointSeafoodToppings);

			List<OWLClass> disjointVegToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointVegToppings, caper, mushroom, olive, onion, pepper, tomato);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointVegToppings);

			List<OWLClass> disjointPepperToppings = new ArrayList<OWLClass>();
			Collections.addAll(disjointPepperToppings, redPepper, greenPepper, jalapenoPepper);
			OwlUtils.makeDisjoint(df, documentIRI, ontology, disjointPepperToppings);

			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Object properties~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			//Create some object properties
			OWLObjectProperty hasIngredient = df.getOWLObjectProperty(":hasIngredient", prefixe);
			OWLObjectProperty hasTopping = df.getOWLObjectProperty(":hasTopping", prefixe);
			OWLObjectProperty hasBase = df.getOWLObjectProperty(":hasBase", prefixe);
			OwlUtils.makeSubproperty(df, ontology, manager, hasTopping, hasIngredient); //subproperties
			OwlUtils.makeSubproperty(df, ontology, manager, hasBase, hasIngredient);

			//more object properties 
			OWLObjectProperty isToppingOf = df.getOWLObjectProperty(":isToppingOf", prefixe);
			OWLObjectProperty isBaseOf = df.getOWLObjectProperty(":isBaseOf", prefixe);
			//make them inverse properties 
			OwlUtils.makeInverse(df, ontology, manager, hasTopping, isToppingOf);
			OwlUtils.makeInverse(df, ontology, manager, hasBase, isBaseOf);

			//domains
			OwlUtils.setDomain(df, ontology, manager, hasTopping, pizza);


			OwlUtils.setDomain(df, ontology, manager, isBaseOf, base);
			OwlUtils.setRange(df, ontology, manager, isBaseOf, pizza);

			//new class NamedPizza
			OWLClass namedPizza = OwlUtils.declareClass(df, documentIRI, ontology, "NamedPizza");
			OwlUtils.putAsSubclass(df, ontology, namedPizza, pizza);
			
			//new class Margherita pizza
			OWLClass margheritaPizza = OwlUtils.declareClass(df, documentIRI, ontology, "MargheritaPizza");
			OwlUtils.putAsSubclass(df, ontology, margheritaPizza, namedPizza);

			//annotation
			OwlUtils.annotateComment(df, documentIRI, ontology, "A pizza that only has Mozarella and Tomato toppings", margheritaPizza, "en");


			//create restriction 
			OwlUtils.createSomeRestriction(df, ontology, manager, margheritaPizza, hasTopping, mozzarella);
			OwlUtils.createSomeRestriction(df, ontology, manager, margheritaPizza, hasTopping, tomato);
			
			OwlUtils.createSomeRestriction(df, ontology, manager, pizza, hasBase, base);
			
			//new class Americana pizza
			OWLClass americanaPizza = OwlUtils.declareClass(df, documentIRI, ontology, "AmericanaPizza");
			OwlUtils.putAsSubclass(df, ontology, americanaPizza, namedPizza);
			OwlUtils.createSomeRestriction(df, ontology, manager, americanaPizza, hasTopping, mozzarella); //hasTopping some mozarella 
			OwlUtils.createSomeRestriction(df, ontology, manager, americanaPizza, hasTopping, tomato); //etc 
			OwlUtils.createSomeRestriction(df, ontology, manager, americanaPizza, hasTopping, pepperoni);
			
			//new class vegetarian pizza
			OWLClass vegPizza = OwlUtils.declareClass(df, documentIRI, ontology, "VegetarianPizza");
			OwlUtils.putAsSubclass(df, ontology, vegPizza, namedPizza);
			
			//only cheese or veg toppings restriction
			OWLClassExpression unionCheeseOrVeg = df.getOWLObjectUnionOf(cheese, vegetable);  //first make union (cheese or veg)
			OWLClassExpression hasOnlyCheeseVeg = OwlUtils.createOnlyRestriction(df, ontology, manager, vegPizza, hasTopping, unionCheeseOrVeg); //then create restriction hastopping (cheese or veg)
			OWLClassExpression pizzaAndOnlyCheeseVeg = df.getOWLObjectIntersectionOf(pizza, hasOnlyCheeseVeg); //now put those two together 
			
			OWLEquivalentClassesAxiom axiom = df.getOWLEquivalentClassesAxiom(vegPizza, pizzaAndOnlyCheeseVeg); //and put that as equivalent to vegPizza
			manager.applyChange(new AddAxiom(ontology, axiom));
			
			OWLClassExpression unionMozzOrTomato = df.getOWLObjectUnionOf(mozzarella, tomato);
			OwlUtils.createOnlyRestriction(df, ontology, manager, margheritaPizza, hasTopping, unionMozzOrTomato);
			
			//new class vegetarian pizza
			OWLClass sohoPizza = OwlUtils.declareClass(df, documentIRI, ontology, "SohoPizza");
			OwlUtils.putAsSubclass(df, ontology, sohoPizza, namedPizza);
			OwlUtils.createOnlyRestriction(df, ontology, manager, sohoPizza, hasTopping, unionMozzOrTomato);

			
			
			//new classes ValuePartition and subclass called SpicinessValuePartition
			OWLClass valuePartition = OwlUtils.declareClass(df, documentIRI, ontology, "ValuePartition");
			OWLClass spicinessValuePartition = OwlUtils.declareClass(df, documentIRI, ontology, "SpicinessValuePartition");
			OwlUtils.putAsSubclass(df, ontology, spicinessValuePartition, valuePartition);
			OWLClass hot = OwlUtils.declareClass(df, documentIRI, ontology, "Hot");
			OWLClass medium = OwlUtils.declareClass(df, documentIRI, ontology, "Medium");
			OWLClass mild = OwlUtils.declareClass(df, documentIRI, ontology, "Mild");
			List<OWLClass>spiciness = new ArrayList<OWLClass>();
			Collections.addAll(spiciness, hot, medium, mild);
			//put as subclasses 
			OwlUtils.putAllAsSubclass(df, ontology, spiciness, spicinessValuePartition);
			//make disjoint
			OwlUtils.makeDisjoint(df, documentIRI, ontology, spiciness);
			
			//concept of "covering axioms"
			OWLClassExpression unionSpiciness = df.getOWLObjectUnionOf(hot, medium, mild); 
			OwlUtils.makeEquivalent(df, ontology, manager, spicinessValuePartition, unionSpiciness);
			
			// restriction so that toppings all have a spiciness  TODO do this for all other toppings
			OWLObjectProperty hasSpiciness = df.getOWLObjectProperty(":hasSpiciness", prefixe);
			OwlUtils.createSomeRestriction(df, ontology, manager, tomato, hasSpiciness, spicinessValuePartition);
			
			//spicy pizza class
			OWLClass spicyPizza = OwlUtils.declareClass(df, documentIRI, ontology, "SpicyPizza");
			OWLClassExpression hasToppingSomeHot = df.getOWLObjectSomeValuesFrom(hasTopping, hot); // => hasTopping SOME hot 
			OWLClassExpression toppingAndSomeHot = df.getOWLObjectIntersectionOf(topping, hasToppingSomeHot); // pizzaTopping and (hasSpiciness some Hot)
			OWLClassExpression hasToppingSomeToppingAndHot = df.getOWLObjectSomeValuesFrom(hasTopping, toppingAndSomeHot);
			OWLClassExpression pizzaAndHasToppingSomeToppingAndHot = df.getOWLObjectIntersectionOf(pizza, hasToppingSomeToppingAndHot);
			OwlUtils.makeEquivalent(df, ontology, manager, spicyPizza, pizzaAndHasToppingSomeToppingAndHot);

			//controler violations de profile 
			OWL2DLProfile profile = new OWL2DLProfile();
			OWL2ELProfile profile2 = new OWL2ELProfile();
			OWL2QLProfile profile3 = new OWL2QLProfile();
			System.out.println(profile3.getName());
			OWLProfileReport report = profile3.checkOntology(ontology);
			for(OWLProfileViolation v:report.getViolations()){
				System.out.println(v);
			}
			
			
				
			//save ontology
			manager.saveOntology(ontology, documentIRI);
			//manager.removeOntology(pizzaOntology);



		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} 






	}
}
