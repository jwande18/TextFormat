package application.text;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

// classes
import application.text.view.PreviewOverviewController;

public class MainApp extends Application {
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Text Formatter");
		
		// disable resize-ability
		this.primaryStage.setResizable(false);
		
		initRootLayout();
		showPreviewOverview();
	}
	
	/**
	 * initRootLayout initializes the root border pane.
	 */
	public void initRootLayout() {
		try {
			// load the root layout
			FXMLLoader loader = new FXMLLoader();
			
			// set the root layout to the loaded fxml
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			// set the stage and scene with the root pane
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			
			// show the stage
			primaryStage.show();
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * showPreviewOverview shows the general preview overview
	 * inside the root layout.
	 */
	public void showPreviewOverview() {
		try {
			// load the preview overview layout
			FXMLLoader loader = new FXMLLoader();
			
			// set the anchor pane to the loaded fxml
			loader.setLocation(MainApp.class.getResource("view/PreviewOverviewLayout.fxml"));
			AnchorPane previewOverview = (AnchorPane) loader.load();
			
			// set the preview overview to the center of the root layout
			rootLayout.setCenter(previewOverview);
			
			// give the controller access to the main app
			PreviewOverviewController controller = loader.getController();
			controller.setMainApp(this);
			
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * getPrimaryStage returns the primary stage.
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
