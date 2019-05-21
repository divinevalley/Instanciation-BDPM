package bdpm;

public class Form {

	private String formLabel;

		public Form (String form) {
			this.formLabel = form; 
		}

		public Form() {
		}

		public String toString() {
			return this.formLabel;
		}

		public String getFormLabel() {
			return formLabel;
		}

		public void setFormLabel(String formLabel) {
			this.formLabel = formLabel;
		}
}
