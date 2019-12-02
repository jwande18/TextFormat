package application.text.view;

// import(s)
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import application.text.MainApp;
import application.text.process.Processing;

public class PreviewOverviewController {
	@FXML
	private TextArea previewArea;
	
	@FXML
	private TextArea errorArea;
	
	// reference to the main application
	private MainApp mainApp;
	private Stage primaryStage;
	
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
	public void setMainApp(MainApp mainApp, Stage primaryStage) {
		this.mainApp = mainApp;
		this.primaryStage = primaryStage;
	}
	
	/**
	 * loadFile opens the Window's file explorer.
	 */
	public void loadFile() {
		FileChooser fileSelector = new FileChooser();
		
		// set properties
		configureFileChooserLoad(fileSelector);
		File inputFile = fileSelector.showOpenDialog(primaryStage);
		
		this.previewArea.setText(inputFile.toString());
	}
	
	private void configureFileChooserLoad(FileChooser fileSelector) {
		// set dialog box properties
		fileSelector.setTitle("File to Process");
		fileSelector.setInitialDirectory(new File(System.getProperty("user.home")));
		
		// set the acceptable file types
		fileSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
	}
	
	public void saveFile() {
		FileChooser fileSelector = new FileChooser();
		
		// set properties
		configureFileChooserSave(fileSelector);
		
		// save to file
		File fileToSaveTo = fileSelector.showSaveDialog(primaryStage);
		
		// check if saved properly
		if(fileToSaveTo != null) {
			try {
				// write text to file
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSaveTo.toString()));
				writer.write(previewArea.getText()); // TODO
				
				writer.close();
			} catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	private void configureFileChooserSave(FileChooser fileSelector) {
		// set dialog box properties
		fileSelector.setTitle("File to Save");
		fileSelector.setInitialDirectory(new File(System.getProperty("user.home")));
		
		fileSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
	}
}
