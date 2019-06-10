package ndc;

import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

public class OwlUtils {
	
	//declare a class
	public static OWLClass declareClass(OWLDataFactory df, IRI documentIRI, OWLOntology ontology, String tag){
		OWLClass newClass = df.getOWLClass(IRI.create(documentIRI+"#"+tag));
		OWLDeclarationAxiom declarationAxiom = df.getOWLDeclarationAxiom(newClass);
		ontology.add(declarationAxiom);
		return newClass;
	}
	
	public static void putAsSubclass(OWLDataFactory df, OWLOntology ontology, OWLClass subclass, OWLClass superclass){
		OWLAxiom subclassAxiom = df.getOWLSubClassOfAxiom(subclass, superclass); 
		ontology.add(subclassAxiom);
	}
	
	public static void putAllAsSubclass(OWLDataFactory df, OWLOntology ontology, List<OWLClass> subclasses, OWLClass superclass){
		for (OWLClass eachSubclass:subclasses){//for each item on list
			OWLAxiom subclassAxiom = df.getOWLSubClassOfAxiom(eachSubclass, superclass); //put as subclass of superclass
			ontology.add(subclassAxiom);
		}
	}
	
	public static void makeSubproperty(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLObjectProperty subproperty, OWLObjectProperty superproperty){
		OWLSubObjectPropertyOfAxiom axiom = df.getOWLSubObjectPropertyOfAxiom(subproperty, superproperty);
		ontology.add(axiom);
	}
	
	public static void makeInverse(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLObjectProperty forwardProperty, OWLObjectProperty inverseProperty){
		OWLInverseObjectPropertiesAxiom axiom = df.getOWLInverseObjectPropertiesAxiom(forwardProperty, inverseProperty);
		ontology.add(axiom);
	}
	
	public static void setDomain(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLObjectProperty property, OWLClass domain){
		OWLObjectPropertyDomainAxiom axiom = df.getOWLObjectPropertyDomainAxiom(property, domain);
		ontology.add(axiom);
	}
	
	public static void setRange(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLObjectProperty property, OWLClass range){
		OWLObjectPropertyRangeAxiom axiom = df.getOWLObjectPropertyRangeAxiom(property, range);
		ontology.add(axiom);
	}
	
	public static void makeDisjoint(OWLDataFactory df, IRI documentIRI, OWLOntology ontology, List<OWLClass> disjointClasses){
		OWLDisjointClassesAxiom disjointAxiom = df.getOWLDisjointClassesAxiom(disjointClasses);			
		ontology.add(disjointAxiom);
	}
	
	public static void annotateComment(OWLDataFactory df, IRI documentIRI, OWLOntology ontology, String annotationString, OWLClass classToAnnotate, String language){
		OWLAnnotation anno = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(annotationString, language));
		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(classToAnnotate.getIRI(), anno);
		ontology.add(ax);
	}
	
	public static void annotateLabel(OWLDataFactory df, IRI documentIRI, OWLOntology ontology, String annotationString, OWLClass classToAnnotate, String language){
		OWLAnnotation anno = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(annotationString, language));
		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(classToAnnotate.getIRI(), anno);
		ontology.add(ax);
	}
	
	public static OWLClassExpression createSomeRestriction(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLClass appliedclass, OWLObjectProperty property, OWLClass objectClass){
		OWLClassExpression restriction = df.getOWLObjectSomeValuesFrom(property, objectClass); //hasTopping, mozzarella => hasTopping SOME mozzarella 
		OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(appliedclass, restriction); // apply this restriction to the class
		manager.applyChange(new AddAxiom(ontology, ax));
		return restriction;
	}
	
	public static OWLClassExpression createSomeRestrictionIntersect(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLClass appliedclass, OWLObjectProperty property, OWLClassExpression intersect){
		OWLClassExpression restriction = df.getOWLObjectSomeValuesFrom(property, intersect); //hasTopping, (mozzarella and cheese) => hasTopping SOME (mozzarella and cheese) 
		OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(appliedclass, restriction); // apply this restriction to the class
		manager.applyChange(new AddAxiom(ontology, ax));
		return restriction;
	}
	
	public static OWLClassExpression createOnlyRestriction(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLClass appliedclass, OWLObjectProperty property, OWLClassExpression union){
		OWLClassExpression restriction = df.getOWLObjectAllValuesFrom(property, union); //hasTopping, (cheese or veg) => hasTopping ONLY (cheese or veg)
		OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(appliedclass, restriction); //apply this to the class
		manager.applyChange(new AddAxiom(ontology, ax));
		return restriction;
	}
	
	public static void makeEquivalent(OWLDataFactory df, OWLOntology ontology, OWLOntologyManager manager, OWLClass appliedclass, OWLClassExpression union){
		OWLEquivalentClassesAxiom axiom = df.getOWLEquivalentClassesAxiom(appliedclass, union); //ie. vegPizza equivalent to (union) 
		manager.applyChange(new AddAxiom(ontology, axiom));
	}

}
