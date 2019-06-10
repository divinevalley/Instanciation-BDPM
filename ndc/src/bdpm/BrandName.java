package bdpm;

public class BrandName {
	String brandNameLabel = "";

	//constructors
	public BrandName(String brandNameLabel) {
		this.brandNameLabel = brandNameLabel;
	}
	
	public String generateMapKey(){
		return Utils.hash(this.brandNameLabel);
	}
	
	public BrandName(){
	}

	public String toString() {
		return brandNameLabel;
	}
	
	
	
	
}
