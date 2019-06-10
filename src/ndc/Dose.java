package ndc;

public class Dose {
Unit unit;
String doseNumber;
String doseLabel;
	
	//constructor
	Dose(String doseNumber, Unit unit){
		this.doseNumber=doseNumber;
		this.unit = unit;
	}
	
	Dose (String doseLabel) {
		this.doseLabel=doseLabel;
	}
	
	String getLabel(){
		return doseLabel;
	}
	
	public Dose() {
	}

	public String toString() {
		return this.doseLabel;
	}
}
