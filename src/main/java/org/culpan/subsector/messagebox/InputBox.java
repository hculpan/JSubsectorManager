package org.culpan.subsector.messagebox;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class InputBox {
	protected String result;
	
	protected Stage dialogStage;
	
	public String getResult() {
		return result;
	}
	
	public InputBox(String text) {
		this(text, null);
	}

	@SuppressWarnings("rawtypes")
	public InputBox(String text, String defValue) {
		dialogStage = new Stage();
		dialogStage.setResizable(false);
		dialogStage.setMinHeight(200);
		dialogStage.setMinWidth(400);
		dialogStage.centerOnScreen();
		dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
		
		final TextField tf = new TextField();
		if (defValue != null) {
			tf.setText(defValue);
		}
		
		javafx.scene.text.Text t = new javafx.scene.text.Text(text);
		Button btnOk = new Button("Ok");
		btnOk.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				result = tf.getText();
				dialogStage.close();
			}
		});
		btnOk.setDefaultButton(true);
		
		Button btnCancel = new Button("Cancel");
		btnCancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				result = null;
				dialogStage.close();
			}
		});
		
		javafx.scene.layout.HBoxBuilder hbox = javafx.scene.layout.HBoxBuilder.create().
			    children(btnOk, btnCancel).alignment(javafx.geometry.Pos.CENTER).padding(new javafx.geometry.Insets(20));
		
		javafx.scene.layout.VBoxBuilder box = javafx.scene.layout.VBoxBuilder.create().
			    children(t, tf, hbox.build()).alignment(javafx.geometry.Pos.CENTER).padding(new javafx.geometry.Insets(20));
		
		dialogStage.setScene(new Scene(box.build()));
	}
	
	public String show() {
		dialogStage.showAndWait();
		return getResult();
	}
}
