package bdpm;

public class Dose {
Unit unit;
String doseNumber;
String doseLabel;
Boolean needsChecking = false;
	
	//constructor
	Dose(String doseNumber, Unit unit){
		this.doseNumber=doseNumber;
		this.unit = unit;
		doseLabel = doseNumber + unit.unitLabel;
	}
	
	Dose(String doseNumber, String unitLabel){
		this.doseNumber=doseNumber;
		this.unit = new Unit (unitLabel);
		doseLabel = doseNumber + unitLabel;
	}
	
	Dose (String doseLabel) {
		this.doseLabel=doseLabel;
		unit = new Unit("");
	}
	
	
	public void setUnit(Unit unit){
		this.unit=unit;
	}
	
	public Unit getUnit(){
		return unit;
	}
	
	String getLabel(){
		return doseLabel;
	}
	
	public Dose() {
	}

	public String toString() { 
		return doseNumber + "  " + unit.unitLabel;
	}
}
