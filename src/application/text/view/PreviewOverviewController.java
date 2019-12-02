package application.text.view;

// import(s)
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import application.text.MainApp;
import application.text.process.Processing;

public class PreviewOverviewController {
	@FXML
	private TextArea previewArea;
	
	@FXML
	private TextArea errorArea;
	
	// reference to the main application
	private MainApp mainApp;
	
	/**
	 * The constructor initializes variables.
	 */
	public PreviewOverviewController() {
	}
	
	/**
	 * initialize initializes the controller class - automatically
	 * called when the fxml is loaded.
	 */
	@FXML
	private void initialize() {
		this.previewArea.setText("THIS IS THE OUTPUT");
		this.errorArea.setText("THIS IS AN ERROR");
	}
	
	/**
	 * setMainApp is called by the main application to give a reference
	 * back to itself.
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
