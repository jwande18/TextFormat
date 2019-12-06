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
import java.io.RandomAccessFile;
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
	private boolean _r; // right justified                       (works)
	private boolean _c; // center (left & right)                 (works)
	private boolean _l; // left justified                        (works)
	private boolean _t; // centered, no justification
	private boolean _d; // double spaced                         (works)
	private boolean _s; // single spaced                         (works)
	private boolean _i; // paragraph indentation (5 spaces)      (works)
	private boolean _b; // block indentation (10 spaces)         (works)
	private boolean _1; // single column (80 characters)         (works)
	private boolean _2; // double column (35, 10, 35 characters) 
	private boolean _e; // blank line insertion                  (works)
	private boolean _n; // no paragraph indentation              (works)
	
	private boolean newParagraph = true;
	private boolean firstRead = true;
	private int linesPerColumn = 0;
	private int readerPosition = 0;
	private int lineCount = 0;
	private int currLine = 0;
	
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
		//this.previewArea.setEditable(false);
		
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
			
			resetFlags();
			statusArea.setText(statusArea.getText() + currentTime() + "Properties set to default.\n");
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
		
		try {
			if(inputFile != null) {
				RandomAccessFile reader = new RandomAccessFile(inputFile.toString(), "r");
				String word, line = "";
				int character, flag;
				boolean setPropertiesBefore = false;
				
				while((character = reader.read()) != -1) {
					++readerPosition;
					
					word = "";
					
					// read flag(s)
					while(character == 45) {
						newParagraph = true;
						
						// get flag value
						flag = reader.read();
					
						System.out.println("Flag: " + (char) flag);
						// set the formatting properties
						setFormatProperties(flag);
						
						// read to end of flag line
						while((character = reader.read()) != 10) {
							++readerPosition;
						}
						
						// read next character
						character = reader.read();
						++readerPosition;
					}
					
					if(!setPropertiesBefore) {
						// set line properties before any words are added
						line = setLinePropertiesBefore(line, reader);
						
						setPropertiesBefore = true;
						newParagraph = false;
					}
					
					while(character != 32 && character != 13 && character != 10 && character != -1) {
						// building a single word
						word = word + (char) character;
						
						if(character == 45) {
							break;
						}
						
						// read next character
						character = reader.read();
						++readerPosition;
					}
					
					if(!_2) {
						if(line.length() + word.length() < 80) {				
							if(character != -1 && character != 13 && character != 10) {
								line = line + word + (char) character;
							}
							else {
								line = line + word;
							}
							
							if(character == 13 || character == 10 || character == -1) {
								if(!line.equals("")) {
									if((int) line.charAt(line.length() - 1) == (int) ' ') {
										line = line.substring(0, line.length() - 1);
									}
									
									if(!line.equals("         ")) {
										previewArea.setText(previewArea.getText() + setLineProperties(line) + "\n");
									}
									line = "";
									setPropertiesBefore = false;
								}
							}						
						}
						else {
							// over the line character limit
							if(character != -1) {
								// remove whitespace at the end of the line
								if((int) line.charAt(line.length() - 1) == (int) ' ') {
									line = line.substring(0, line.length() - 1);
								}
								
								previewArea.setText(previewArea.getText() + setLineProperties(line) + "\n");
								line = "";
								setPropertiesBefore = false;
								
								if(character != -1) {
									line = line + word + (char) character;
								}
								else {
									line = line + word;
								}
							}
						}
					}
					else {
						// 2 column property enabled
						if(lineCount < linesPerColumn) {							
							// left column
							if(line.length() + word.length() < 35) {				
								if(character != -1 && character != 13 && character != 10) {
									line = line + word + (char) character;
								}
								else {
									line = line + word;
								}
								
								if(character == 13 || character == 10 || character == -1) {
									if(!line.equals("")) {
										if((int) line.charAt(line.length() - 1) == (int) ' ') {
											line = line.substring(0, line.length() - 1);
										}
										
										// append space to equal 45 in length
										while(line.length() < 45) {											
											line = line + " ";
										}
										
										if(!line.equals("         ")) {
											if(!firstRead) {
												previewArea.setText(previewArea.getText() + setLineProperties(line) + "\n");
											
												// increment line count
												++lineCount;
												
												firstRead = false;
											}
										}
										line = "";
										//setPropertiesBefore = false;
									}
								}						
							}
							else {
								// over the line character limit
								if(character != -1) {
									// remove whitespace at the end of the line
									if((int) line.charAt(line.length() - 1) == (int) ' ') {
										line = line.substring(0, line.length() - 1);
									}
									
									// append space to equal 45 in length
									while(line.length() < 45) {
										line = line + " ";
									}
									
									previewArea.setText(previewArea.getText() + setLineProperties(line) + "\n");
									
									// increment line count
									++lineCount;
									
									line = "";
									//setPropertiesBefore = false;
									
									if(character != -1) {
										line = line + word + (char) character;
									}
									else {
										line = line + word;
									}
								}
							}
						}
						else {
							// right column
							if(currLine < linesPerColumn) {
								if(line.length() + word.length() < 35) {				
									if(character != -1 && character != 13 && character != 10) {
										line = line + word + (char) character;
									}
									else {
										line = line + word;
									}
									
									if(character == 13 || character == 10 || character == -1) {
										if(!line.equals("")) {
											if((int) line.charAt(line.length() - 1) == (int) ' ') {
												line = line.substring(0, line.length() - 1);
											}
											
											// append space to equal 35 in length
											while(line.length() < 35) {
												line = line + " ";
											}
											
											if(!line.equals("         ")) {
												previewArea.setText(previewArea.getText().substring(0, currLine * 80 + 45 + currLine) + setLineProperties(line) +
														previewArea.getText().substring(currLine * 80 + 45 + currLine));
												++currLine;
											}
											line = "";
											//setPropertiesBefore = false;
										}
									}						
								}
								else {
									// over the line character limit
									if(character != -1) {
										// remove whitespace at the end of the line
										if((int) line.charAt(line.length() - 1) == (int) ' ') {
											line = line.substring(0, line.length() - 1);
										}
										
										// append space to equal 35 in length
										while(line.length() < 35) {
											line = line + " ";
										}
										
										previewArea.setText(previewArea.getText().substring(0, currLine * 80 + 45 + currLine) + setLineProperties(line) +
												previewArea.getText().substring(currLine * 80 + 45 + currLine));
										++currLine;
										
										line = "";
										//setPropertiesBefore = false;
										
										if(character != -1) {
											line = line + word + (char) character;
										}
										else {
											line = line + word;
										}
									}
								}
							}
						}
					}
				}
				
				reader.close();
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
			
			// disable other justifications
			_r = false;
			_l = false;
			_t = false;
		}
		break;
		case (int) 'l': {
			_l = true;
			
			// disable other justifications
			_r = false;
			_c = false;
			_t = false;
		}
		break;
		case (int) 't': {
			_t = true;
			
			// disable other justifications
			_r = false;
			_c = false;
			_l = false;
		}
		break;
		case (int) 'd': {
			_d = true;
			
			// disable other spacing
			_s = false;
		}
		break;
		case (int) 's': {
			_s = true;
			
			// disable other spacing
			_d = false;
		}
		break;
		case (int) 'i': {
			if(!_2) {
				_i = true;
			}
			else {
				_i = false;
			}
			
			// disable non-indent
			_b = false;
			_n = false;
		}
		break;
		case (int) 'b': {
			if(!_2) {
				_b = true;
			}
			else {
				_b = false;
			}
			
			// disable other indentation
			_i = false;
			_n = false;
		}
		break;
		case (int) '1': {
			_1 = true;
			
			// disable other column
			_2 = false;
		}
		break;
		case (int) '2': {
			_2 = true;
			
			// disable other column
			_1 = false;
			
			// disable special indentations
			_i = false;
			_b = false;
			_t = false;
			
			// enable default(s)
			_n = true;
		}
		break;
		case (int) 'e': {
			_e = true;
		}
		break;
		case (int) 'n': {
			_n = true;
			
			// disable indent
			_i = false;
			_b = false;
		}
		break;
		}
	}
	
	/**
	 * setLinePropertiesBefore sets the properties of the text that are required before
	 * any text is added. This includes indentations, newlines, and columns.
	 * @param line
	 * @param reader
	 * @return
	 */
	public String setLinePropertiesBefore(String line, RandomAccessFile reader) {
		if(_b) {
			line = "          " + line;
		}
		
		if(_e) {
			previewArea.setText(previewArea.getText() + "\n");
			_e = false;
		}
		
		if(_i) {
			if(newParagraph) {
				line = "     " + line;
			}
		}
		
		if(_1) {
			// default
		}
		
		if(_2) {	
			int character, characterCount = 0;
			
			// count the number of characters
			try {
				while((character = reader.read()) != -1 && character != 45) {
					++characterCount;
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			
			linesPerColumn = (characterCount / 35) / 2;
			
			// minimal one line per column
			++linesPerColumn;
			
			System.out.println("Lines Per Column: " + linesPerColumn);
			
			// reset the reader to its last position
			try {
				reader.seek(readerPosition);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			
			System.out.println("Character Count: " + characterCount);
		}
		
		return line;
	}

	/**
	 * setLineProperties sets the properties of the text depending on the flags
	 * supplied by the user.
	 * @param line
	 * @return
	 */
	public String setLineProperties(String line) {		
		// set properties
		if(_r) {
			int count = 0;
			int spaces = 80 - line.length();
			
			while(count < spaces) {
				line = " " + line;
				++count;
			}
		}
		
		if(_c) {
			int count = 0;
			int spaces = 80 - line.length();
			int spaceDiv = spaces / 2;
			
			while(count < spaceDiv) {
				line = " " + line;
				++count;
			}
		}
		
		if(_l) {
			// default
		}
		
		if(_t) {
			int spacesLeft = 80 -  line.length();
			int spacesInLine = 0;
			int spacesToAddPerSpace = 0;
			int position = 0;
			
			// iterate through line and count spaces
			for(int i = 0; i < line.length();++i) {
				if((int) line.charAt(i) == (int) ' ') {
					spacesInLine++;
				}
			}
			
			if(spacesInLine != 0 && line.length() < 80) {
				spacesToAddPerSpace = spacesLeft / spacesInLine;
				System.out.println("Spaces to Add: " + spacesToAddPerSpace);
				System.out.println("Spaces Left: " + spacesLeft);
				
				++spacesToAddPerSpace;
				
				int lineLength = line.length();
				
				while(position < lineLength && spacesLeft > 0 && line.length() < 80) {
					if((int) line.charAt(position) == (int) ' ') {
						// add the appropriate number of spaces
						for(int i = 0; i < spacesToAddPerSpace; ++i) {
							line = line.substring(0, position) + " " + line.substring(position);
							--spacesLeft;
							++position;
						}
					}
					
					++position;
				}
			}
			else {
				// center the word
				int spacesAdded = 0;
				
				while(spacesAdded < (spacesLeft / 2)) {
					line = " " + line;
					++spacesAdded;
				}
			}
		}
		
		if(_d) {
			line =  line + "\n";
		}
		
		if(_s) {
			// default
		}
		
		if(_n) {
			// default
		}
		
		return line;
	}
	
	/**
	 * resetFlags resets all text properties to their
	 * default format settings.
	 */
	public void resetFlags() {
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
}