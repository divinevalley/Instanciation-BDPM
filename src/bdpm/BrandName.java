package bdpm;

public class BrandName {
	String brandNameLabel = "";
	Boolean labelNeedsChecking = false;

	//constructors
	public BrandName(String brandNameLabel) {
		this.brandNameLabel = brandNameLabel;
	}
	
	public String generateMapKey(){
		return Utils.hash(this.brandNameLabel);
	}
	
	public void setBrandNameLabel(String brandNameLabel){
		this.brandNameLabel=brandNameLabel;
	}
	
	public BrandName(){
	}

	public String toString() {
		return brandNameLabel;
	}
	
	
	
	
}
