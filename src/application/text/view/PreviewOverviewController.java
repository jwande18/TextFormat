package application.text.view;

// import(s)
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import application.text.MainApp;
import application.text.process.Processing;

public class PreviewOverviewController {
	@FXML
	private TextArea previewArea;
	
	@FXML
	private TextArea statusArea;
	
	// instance variables
	private int errorCount;
	File inputFile;
	File outputFile;
	File statusLog;
	
	// flags
	private boolean _r; // right justified
	private boolean _c; // center (left & right)
	private boolean _l; // left justified
	private boolean _t; // centered, no justification
	private boolean _d; // double spaced
	private boolean _s; // single spaced
	private boolean _i; // paragraph indentation (5 spaces)
	private boolean _b; // block indentation (10 spaces)
	private boolean _1; // single column (80 characters)
	private boolean _2; // double column (35, 10, 35 characters)
	private boolean _e; // blank line insertion
	private boolean _n; // no paragraph indentation
	
	// reference to the main application
	private MainApp mainApp;
	private Stage primaryStage;
	
	/**
	 * The constructor initializes variables.
	 */
	public PreviewOverviewController() {
		errorCount = 0;
		
		// set the flag default(s)
		_r = false;
		_c = false;
		_l = true;  // default
		_t = false;
		_d = false;
		_s = true;  // default
		_i = false;
		_b = false;
		_1 = true;  // default
		_2 = false;
		_e = false;
		_n = true;  // default
	}
	
	/**
	 * initialize initializes the controller class - automatically
	 * called when the fxml is loaded.
	 */
	@FXML
	private void initialize() {
		// set preview area properties
		this.previewArea.setText("");
		this.previewArea.setEditable(false);
		
		// set status area properties
		this.statusArea.setText("");
		this.statusArea.setEditable(false);
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
	 * loadFile opens the dialog box and loads a
	 * file.
	 */
	public void loadFile() {		
		FileChooser fileSelector = new FileChooser();
		
		// set properties
		configureFileChooserLoad(fileSelector);
		inputFile = fileSelector.showOpenDialog(primaryStage);
		
		if(inputFile != null) {
			statusArea.setText(statusArea.getText() + currentTime() + "File loaded.\n");
		}
		else {
			statusArea.setText(statusArea.getText() + currentTime() + "File not loaded.\n");
		}
	}
	
	/**
	 * configureFileChooserLoad sets the properties of the load
	 * dialog box.
	 * @param fileSelector
	 */
	private void configureFileChooserLoad(FileChooser fileSelector) {
		// set dialog box properties
		fileSelector.setTitle("File to Process");
		fileSelector.setInitialDirectory(new File(System.getProperty("user.home")));
		
		// set the acceptable file types
		fileSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
	}
	
	/**
	 * saveFile opens the dialog box and sets the file to
	 * write to.
	 */
	public void saveFile() {
		FileChooser fileSelector = new FileChooser();
		
		// set properties
		configureFileChooserSave(fileSelector);
		
		// save to file
		outputFile = fileSelector.showSaveDialog(primaryStage);
	
		// check if saved properly
		if(outputFile != null) {
			try {
				// write text to file
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toString()));
				writer.write(previewArea.getText());
				
				writer.close();
				statusArea.setText(statusArea.getText() + currentTime() + "File saved.\n");
			} catch(IOException exception) {
				statusArea.setText(statusArea.getText() + currentTime() + formatErrorCount() + "File does not exist.\n");
			}
		}
		else {
			statusArea.setText(statusArea.getText() + currentTime() + "File not saved.\n");
		}
	}
	
	/**
	 * configureFileChooserSave sets the properties of the save
	 * dialog box.
	 * @param fileSelector
	 */
	private void configureFileChooserSave(FileChooser fileSelector) {
		// set dialog box properties
		fileSelector.setTitle("File to Save");
		fileSelector.setInitialDirectory(new File(System.getProperty("user.home")));
		
		fileSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
	}
	
	/**
	 * processFile processes the file 'inputFile' for the appropriate
	 * formatting.
	 * @throws IOException 
	 */
	public void processFile() throws IOException {
		// reset the preview area
		previewArea.setText("");
		int characterCount = 0;
		int lineLength = 0;
		
		try {
			if(inputFile != null) {
				FileReader reader = new FileReader(inputFile.toString());
				String word;
				int character, flag;
				
				while((character = reader.read()) != -1) {
					word = "";
					
					// read flag(s)
					while(character == 45) {						
						// get flag value
						flag = reader.read();
					
						// set the formatting properties
						setFormatProperties(flag);
						
						// read to end of flag line
						while((character = reader.read()) != 10) {
							;
						}
						
						// read next character
						character = reader.read();
					}
					
					setTextProperties();
					
					while(character != 32 && character != 13 && character != -1) {
						// building a single word
						word = word + (char) character;
						
						if(character == 13) {
							characterCount = 0;
						}
						else if(character == 45) {
							break;
						}
						// read next character
						character = reader.read();
						++characterCount;
					}
					
					if(characterCount < 80) {
						if(character != -1) {
							previewArea.setText(previewArea.getText() + word + (char) character);
						}
						
						if(character == 13) {
							characterCount = 0;
						}
						else {
							++characterCount;
						}
					}
					else {
						// over the line character limit
						if(character != -1) {
							previewArea.setText(previewArea.getText() + "\n" + word + (char) character);
						}
						
						characterCount = 0;
					};
				}
				
				statusArea.setText(statusArea.getText() + currentTime() + "File processed.\n");
			}
			else {
				++errorCount;
				statusArea.setText(statusArea.getText() + currentTime() + formatErrorCount() + "File not loaded.\n");
			}
			
		} catch (FileNotFoundException exception) {
			++errorCount;
			statusArea.setText(statusArea.getText() + currentTime() + formatErrorCount() + "File not found.\n");
		}
	}
	
	/**
	 * exitApplication exits the program and closes
	 * all files if necessary.
	 */
	public void exitApplication() {
		// write status and error log out
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("status-error-log.txt"));
			writer.write(statusArea.getText());
			
			writer.close();
		} catch(IOException exception) {
			++errorCount;
			statusArea.setText(statusArea.getText() + currentTime() + formatErrorCount() + "Unable to close program.\n");
		}
		
		// exit stage
		primaryStage.close();
	}
	
	/**
	 * currentTime returns the system's current time in
	 * HH:MM:SS format.
	 * @return
	 */
	public String currentTime() {
		LocalTime time = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		
		return "[" + time.format(formatter) + "]: ";
	}
	
	/**
	 * formatErrorCount returns the current error count with a
	 * specified format.
	 * @return
	 */
	public String formatErrorCount() {
		return "(Error #" + errorCount + ") ";
	}
	
	/**
	 * setFormatProperties enables the respective flag based on its set properties
	 * @param flag
	 */
	public void setFormatProperties(int flag) {
		switch(flag) {
		case (int) 'r': {
			_r = true;
			
			// disable other justifications
			_c = false;
			_l = false;
			_t = false;
		}
		break;
		case (int) 'c': {
			_c = true;
		}
		break;
		case (int) 'l': {
			_l = true;
		}
		break;
		case (int) 't': {
			_t = true;
		}
		break;
		case (int) 'd': {
			_d = true;
		}
		break;
		case (int) 's': {
			_s = true;
		}
		break;
		case (int) 'i': {
			_i = true;
		}
		break;
		case (int) 'b': {
			_b = true;
		}
		break;
		case (int) '1': {
			_1 = true;
		}
		break;
		case (int) '2': {
			_2 = true;
		}
		break;
		case (int) 'e': {
			_e = true;
		}
		break;
		case (int) 'n': {
			_n = true;
		}
		break;
		}
	}
	
	public void setTextProperties() {
		// set properties
		if(_r) {
			
		}
		
		if(_c) {
			
		}
		
		if(_l) {
			
		}
		
		if(_t) {
			
		}
		
		if(_d) {
			
		}
		
		if(_s) {
			
		}
		
		if(_i) {
			previewArea.setText(previewArea.getText() + "     ");
			_i = false;
		}
		
		if(_b) {
			
		}
		
		if(_1) {
			
		}
		
		if(_2) {
			
		}
		
		if(_e) {
			
		}
		
		if(_n) {
			
		}
	}
}