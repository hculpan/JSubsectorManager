package org.culpan.subsector;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.culpan.subsector.generators.SwnSectorGenerator;
import org.culpan.subsector.messagebox.InputBox;
import org.culpan.subsector.messagebox.MessageBox;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

@SuppressWarnings("restriction")
public class Main extends Application {

	protected Image hexImage;
	
	protected Image hostilePlanetImage = new Image("/Planet_Hostile_01_LO.PNG");
	
	protected Image lifePlanetImage = new Image("/Planet_Life_01_LO.PNG");
	
	protected long currX;
	
	protected long currY;
	
	protected Canvas systemOutput;
	
	protected Element subsectorRoot;
	
	protected File currentDataFile;
	
	protected Point2D currentSelectedLocation;
	
	protected boolean dirty = false;
	
	protected Stage primaryStage;
	
	protected ContextMenu contextMenu;
	
	protected Point2D lastMousePosition;
	
	protected Point2D calcPlanetCenter(Point2D loc) {
		return calcPlanetCenter((long)loc.getX(), (long)loc.getY());
	}
	
	protected Point2D calcPlanetCenter(long x, long y) {
		double xspace = 72.75;
		double yspace = 81.75;
		int xbase = 59;
		int ybase = (x % 2 == 0 ? 92 : 51);
		double xpos = xbase + ((x - 1) * xspace);
		double ypos = ybase + ((y - 1) * yspace);
		
		return new Point2D(xpos, ypos);
	}
	
	protected Point2D systemLocation(String systemId) {
		if (systemId != null && systemId.length() == 4) {
			String xStr = systemId.substring(0, 2);
			String yStr = systemId.substring(2, 4);
			return new Point2D(Integer.parseInt(xStr), Integer.parseInt(yStr));
		} else {
			return null;
		}
	}
	
	protected void drawTradeRoute(GraphicsContext gc, String origin, String dest, String level) {
		Point2D oLoc = systemLocation(origin);
		Point2D dLoc = systemLocation(dest);
		if (oLoc != null && dLoc != null) {
			Point2D oPos = calcPlanetCenter(oLoc);
			Point2D dPos = calcPlanetCenter(dLoc);
			double lw = gc.getLineWidth();
			javafx.scene.paint.Paint p = gc.getStroke();
			gc.setLineWidth(2);
			gc.setStroke(Color.BLUE);
			gc.strokeLine(oPos.getX() + 10, oPos.getY() + 10, dPos.getX() + 10, dPos.getY() + 10);
			gc.setLineWidth(lw);
			gc.setStroke(p);
		}
	}
	
	protected boolean isAlienPopulation(Element star) {
		Element population = star.getChild("population");
		if (population != null) {
			String popDescr = population.getChildText("description");
			return (popDescr != null && "Alien".equalsIgnoreCase(popDescr));
		} else {
			return false;
		}
	}
	
	protected void drawPlanet(GraphicsContext gc, Element star, int x, int y) {
		Point2D pos = calcPlanetCenter(x, y);
		double lw = gc.getLineWidth();
		javafx.scene.paint.Paint strokep = gc.getStroke();
		javafx.scene.paint.Paint fillp = gc.getFill();
		
		if ("red".equalsIgnoreCase(star.getChildText("travel-zone"))) {
			gc.setFill(Color.LIGHTGRAY);
			gc.fillOval(pos.getX() - 10, pos.getY() - 10, 40, 40);
			
			gc.setLineWidth(2);
			gc.setStroke(Color.RED);
			gc.strokeOval(pos.getX() - 10, pos.getY() - 10, 40, 40);
		} else if ("amber".equalsIgnoreCase(star.getChildText("travel-zone"))) {
			gc.setLineWidth(2);
			gc.setStroke(Color.LIGHTGRAY);
			gc.strokeOval(pos.getX() - 10, pos.getY() - 10, 40, 40);
		}
		
		if (!isAlienPopulation(star)) {
			gc.setFill(Color.BLACK);
		} else {
			gc.setFill(Color.DARKGRAY);
		}
		gc.fillOval(pos.getX(), pos.getY(), 20, 20);
		
		if ("yes".equalsIgnoreCase(star.getChildText("gas-giant"))) {
			gc.setFill(Color.BLACK);
			gc.fillOval(pos.getX() + 20, pos.getY() - 8, 7, 7);
			gc.setLineWidth(1.5);
			gc.setStroke(Color.BLACK);
			gc.strokeOval(pos.getX() + 17.5, pos.getY() - 6, 12, 4);
		}

		gc.setLineWidth(lw);
		gc.setStroke(strokep);
		gc.setFill(fillp);
	}
	
	protected File getSelectedFile() {
        FileChooser fileChooser = new FileChooser();
        String userDirectoryString = System.getProperty("user.home");
        File userDirectory = new File(userDirectoryString);
        fileChooser.setInitialDirectory(userDirectory);
        
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        extFilter = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
       
        //Show open file dialog
        return fileChooser.showOpenDialog(null);
	}
	
	protected void buttonReloadDataEvent(GraphicsContext gc) {
		if (currentDataFile != null) {
		       if (currentDataFile != null) {
		    	   try {
		    		   SAXBuilder sax = new SAXBuilder();
		    		   Document doc = sax.build(currentDataFile);
		    		   subsectorRoot = doc.getRootElement();

		    		   hexImage = new Image("/hex_map.png");
		    		   gc.drawImage(hexImage, 0, 0);
		    			
		    		   Element tradeRoutes = subsectorRoot.getChild("trade-routes");
		    		   if (tradeRoutes != null) {
			    		   for (Element e : tradeRoutes.getChildren("trade-route")) {
			    			   drawTradeRoute(gc, e.getChildText("origin"), e.getChildText("destination"), e.getChildText("level"));
			    		   }
		    		   }
		    		   
		    		   Element stars = subsectorRoot.getChild("stars");
		    		   for (Element e : stars.getChildren("star")) {
		    			   String id = e.getAttributeValue("id");
		    			   int x = Integer.parseInt(id.substring(0, 2));
		    			   int y = Integer.parseInt(id.substring(2, 4));
		    			   drawPlanet(gc, e, x, y);
		    		   }
					} catch (JDOMException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
		       }
		}
	}
	
	protected void buttonLoadDataEvent(GraphicsContext gc) {
       currentDataFile = getSelectedFile();
       buttonReloadDataEvent(gc);
	}

    protected void buttonGenSubsectorEvent(GraphicsContext gc) {
        try (FileOutputStream out = new FileOutputStream("subsector.xml")) {
            SwnSectorGenerator gen = new SwnSectorGenerator();
            gen.process(out);
        } catch (IOException e) {

        }

        currentDataFile = new File("subsector.xml");
        buttonReloadDataEvent(gc);
    }

    protected void buttonSaveDataEvent(GraphicsContext gc) {
		if (subsectorRoot != null) {
			try {
				XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
				xout.output(subsectorRoot.getDocument(), new FileOutputStream(currentDataFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			dirty = false;
		}
	}
	
	public static final NumberFormat twoDigit = new DecimalFormat("00");
	
	protected Element findSystem(String systemId) {
		if (subsectorRoot == null) {
			return null;
		}
		
		Element result = null;
		for (Element e : subsectorRoot.getChild("stars").getChildren("star")) {
			if (systemId.equalsIgnoreCase(e.getAttributeValue("id"))) {
				result = e;
				break;
			}
		}
		
		return result;
	}
	
	protected String systemIdFromLoc(long x, long y) {
		return twoDigit.format(x) + twoDigit.format(y);
	}
	
	protected String systemIdFromLoc(Point2D p) {
		return systemIdFromLoc((long)p.getX(), (long)p.getY());
	}
	
	final static javafx.scene.text.Font topFont = javafx.scene.text.FontBuilder.create().name("Arial").size(24.0).build();

	final static javafx.scene.text.Font headerFont = javafx.scene.text.FontBuilder.create().name("Arial").size(20.0).build();
	
	final static javafx.scene.text.Font itemFont = javafx.scene.text.FontBuilder.create().name("Arial").size(16).build();
	
	protected void writeDescriptorOutput(GraphicsContext gc, long baseY, String singular, String plural, Element systemElement) {
		Element enemies = systemElement.getChild(plural);
		gc.setFont(headerFont);
		gc.fillText(Character.toUpperCase(plural.charAt(0)) + plural.substring(1) + ":", 10, baseY);
		gc.setFont(itemFont);
		int cnt = 0;
		for (Element enemy : enemies.getChildren(singular)) {
			gc.fillText(enemy.getText(), systemOutput.getWidth() / 4, baseY + (20 * cnt));
			cnt++;
		}
	}
	
	protected void writeSystemOutput(GraphicsContext gc, long x, long y) {
		String systemId = systemIdFromLoc(x, y); 
		
		gc.clearRect(3, 3, systemOutput.getWidth() - 5, systemOutput.getHeight() - 5);
		gc.setFont(topFont);
		gc.setFill(Color.WHITE);
		gc.fillText("System: " + systemId, ((systemOutput.getWidth() / 4) * 3) + 5, 30);
		
		double lw = gc.getLineWidth();
		gc.setLineWidth(2);
		gc.strokeLine(2,  42, systemOutput.getWidth(), 42);
		gc.strokeLine(2,  115, systemOutput.getWidth(), 115);
		gc.setLineWidth(lw);
		
		if (subsectorRoot != null) {
			Element systemElement = findSystem(systemId);
			if (systemElement != null) {
				if (systemElement.getChildText("name") != null) {
					gc.fillText("Name: " + systemElement.getChildText("name"), 10, 30);
				} else {
					gc.fillText("Name: unknown", 10, 30);
				}
				gc.setFont(itemFont);
				gc.fillText("Atmo.: " + systemElement.getChildText("atmosphere"), 10, 65);
				gc.fillText("Biosphere: " + systemElement.getChildText("biosphere"), (systemOutput.getWidth() / 2), 65);
				gc.fillText("Temp.: " + systemElement.getChildText("temperature"), 10, 85);
				Element techLevel = systemElement.getChild("tech-level");
				gc.fillText("Tech Level: " + techLevel.getChildText("description") + " [" + techLevel.getChildText("value") + "]", (systemOutput.getWidth() / 2), 85);
				Element population = systemElement.getChild("population");
				if ("alien".equalsIgnoreCase(population.getChildText("description"))) {
					gc.fillText("Population: " + population.getChildText("value") + " [Alien]", 10, 105);
				} else {
					gc.fillText("Population: " + population.getChildText("value"), 10, 105);
				}
				Element size = systemElement.getChild("world-size");
				gc.fillText("Size: " + size.getChildText("value") + " [" + size.getChildText("gravity") + "g]", (systemOutput.getWidth() / 2), 105);

				gc.setFont(headerFont);
				Element tags = systemElement.getChild("tags");
				Element tag1 = tags.getChildren("tag").get(0);
				Element tag2 = tags.getChildren("tag").get(1);
				gc.fillText(tag1.getValue(), (systemOutput.getWidth() / 10), 140);
				gc.fillText(tag2.getValue(), (systemOutput.getWidth() / 10) * 6, 140);
			
				writeDescriptorOutput(gc, 175, "enemy", "enemies", systemElement);
				writeDescriptorOutput(gc, 255, "friend", "friends", systemElement);
				writeDescriptorOutput(gc, 335, "complication", "complications", systemElement);
				writeDescriptorOutput(gc, 415, "thing", "things", systemElement);
				writeDescriptorOutput(gc, 495, "place", "places", systemElement);
			}
		}
	}
	
	protected long calcXFromXPos(double xpos) {
		long x = Math.round((xpos - 59)/72.75) + 1;
		if (x > 8) { 
			x = 8;
		} else if (x < 1) { 
			x = 1;
		}
		
		return x;
	}
	
	protected long calcYFromYPos(double ypos, long x) {
		double ybase = (x % 2 == 0 ? 92 : 51);
		long y = Math.round((ypos - ybase) / 81.75) + 1;
		if (y > 10) { 
			y = 10;
		} else if (y < 1) { 
			y = 1;
		}

		return y;
	}
	
	protected Point2D calcSystemFromPos(MouseEvent e) {
		long x = calcXFromXPos(e.getX());
		long y = calcYFromYPos(e.getY(), x);
		
		return new Point2D(x, y);
	}
	
	protected Point2D calcSystemFromPos(Point2D e) {
		long x = calcXFromXPos(e.getX());
		long y = calcYFromYPos(e.getY(), x);
		
		return new Point2D(x, y);
	}
	
	protected void reloadData(GraphicsContext gc) {
		buttonReloadDataEvent(gc);
	}
	
	protected void createTradeRoute(GraphicsContext gc, String startSys, String endSys) {
		Namespace ns = subsectorRoot.getNamespace();
		dirty = true;
		Element tradeRoutes = subsectorRoot.getChild("trade-routes");
		if (tradeRoutes == null) {
			tradeRoutes = new Element("trade-routes", ns);
			subsectorRoot.addContent(tradeRoutes);
		}
		Element tradeRoute = new Element("trade-route", ns);
		Element origin = new Element("origin", ns);
		origin.addContent(startSys);
		tradeRoute.addContent(origin);
		Element dest = new Element("destination", ns);
		dest.addContent(endSys);
		tradeRoute.addContent(dest);
		tradeRoutes.addContent(tradeRoute);
		buttonSaveDataEvent(gc);
		reloadData(gc);
	}
	
	protected void clearCurrentSelection(GraphicsContext gc) {
		Point2D pos = calcPlanetCenter(currentSelectedLocation);
		javafx.scene.paint.Paint p = gc.getStroke();
		double lw = gc.getLineWidth();
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(2);
		gc.strokeOval(pos.getX() - 5, pos.getY() - 5, 30, 30);
		gc.setStroke(p);
		gc.setLineWidth(lw);
		currentSelectedLocation = null;
	}

	protected void handleMouseDown(MouseEvent e, GraphicsContext gc) {
		Point2D sys = calcSystemFromPos(e);
		Element system = findSystem(systemIdFromLoc(sys));
		if (e.isSecondaryButtonDown() && subsectorRoot != null &&  system != null) {
			lastMousePosition = new Point2D(e.getX(), e.getY());
			contextMenu.show((javafx.scene.Node) e.getSource(), e.getScreenX(), e.getScreenY());
		} else if (subsectorRoot != null && system != null) {
			if (currentSelectedLocation == null) {
				currentSelectedLocation = calcSystemFromPos(e);
				Point2D pos = calcPlanetCenter(currentSelectedLocation);
				javafx.scene.paint.Paint p = gc.getStroke();
				gc.setStroke(Color.GREEN);
				gc.strokeOval(pos.getX() - 5, pos.getY() - 5, 30, 30);
				gc.setStroke(p);
			} else if (sys.equals(currentSelectedLocation)) {
				clearCurrentSelection(gc);
			} else {
				String startSys = systemIdFromLoc(sys);
				String endSys = systemIdFromLoc(currentSelectedLocation);
				int result = MessageBox.show(primaryStage,
						"Create a trade route between " + startSys + " to "
								+ endSys + "?", "Create trade route",
						MessageBox.ICON_INFORMATION | MessageBox.OK
								| MessageBox.CANCEL);
				if (result == MessageBox.OK) {
					clearCurrentSelection(gc);
					createTradeRoute(gc, startSys, endSys);
				}
			}
		}
	}

	protected void handleMouseMoved(MouseEvent e, GraphicsContext gc) {
		long x = calcXFromXPos(e.getX());
		long y = calcYFromYPos(e.getY(), x);
		
		if (currX != x) {
			writeSystemOutput(gc, x, y);
		}
	}
	
	protected String getStringInput(String text) {
		InputBox ip = new InputBox(text);
		return ip.show();
	}
	
	protected void handleChangeName(final GraphicsContext gc) {
		if (lastMousePosition != null) {
			String name = getStringInput("Enter the system's new name:");
			System.out.println("name=" + name);
			if (name != null) {
				Point2D sys = calcSystemFromPos(lastMousePosition);
				Element system = findSystem(systemIdFromLoc(sys));
				Element nameElement = system.getChild("name");
				if (nameElement == null) {
					nameElement = new Element("name");
					system.addContent(nameElement);
				}
				nameElement.setText(name);
				buttonSaveDataEvent(gc);
				reloadData(gc);
			}
			lastMousePosition = null;
		}
	}
	
	protected void createPopupMenu(final GraphicsContext gc) {
		contextMenu = new ContextMenu();

		MenuItem item1 = new MenuItem("Change name");
		item1.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
		    public void handle(javafx.event.ActionEvent e) {
		    	handleChangeName(gc);
		    }
		});
		contextMenu.getItems().addAll(item1);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
        final int sceneWidth = 1440;
        final int sceneHeight = 903;

		this.primaryStage = primaryStage;
		
		primaryStage.setTitle("Subsector Mapper");
		Group root = new Group();
		Scene scene = new Scene(root, sceneWidth, sceneHeight, Color.BLACK);
		
		Canvas canvas = new Canvas(650, 903);
		double padSpace = 0; //(scene.getHeight() - canvas.getHeight()) / 2;
		canvas.setLayoutX(sceneWidth - 1300);
		canvas.setLayoutY(padSpace);
		final GraphicsContext gc = canvas.getGraphicsContext2D();
		
		systemOutput = new Canvas(650, 903);
		systemOutput.setLayoutX(sceneWidth - 650);
		systemOutput.setLayoutY(padSpace);
		final GraphicsContext outputGc = systemOutput.getGraphicsContext2D();
		double lw = outputGc.getLineWidth();
		outputGc.setLineWidth(2);
		outputGc.setStroke(Color.WHITE);
		outputGc.strokeRect(1,  1, systemOutput.getWidth() - 2, systemOutput.getHeight() - 2);
		outputGc.strokeLine(2,  42, systemOutput.getWidth(), 42);
		canvas.setOnMouseMoved(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				handleMouseMoved(arg0, outputGc);
			}
		});
		canvas.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				handleMouseDown(arg0, gc);
			}
		});
		outputGc.setLineWidth(lw);

        Button btnGenSubsector = new Button();
        btnGenSubsector.setText("Generate");
        btnGenSubsector.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent arg0) {
                buttonGenSubsectorEvent(gc);
            }
        });
        btnGenSubsector.setLayoutX(25);
        btnGenSubsector.setLayoutY(25);
        btnGenSubsector.setMinWidth(100);
        root.getChildren().add(btnGenSubsector);

		Button btnLoadData = new Button();
		btnLoadData.setText("Load Data");
		btnLoadData.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				buttonLoadDataEvent(gc);
			}
		});
		btnLoadData.setLayoutX(25);
		btnLoadData.setLayoutY(75);
		btnLoadData.setMinWidth(100);
		root.getChildren().add(btnLoadData);
		
		Button btnReloadData = new Button();
		btnReloadData.setText("Reload Data");
		btnReloadData.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				buttonReloadDataEvent(gc);
			}
		});
		btnReloadData.setLayoutX(25);
		btnReloadData.setLayoutY(115);
		btnReloadData.setMinWidth(100);
		root.getChildren().add(btnReloadData);
		
		Button btnSaveData = new Button();
		btnSaveData.setText("Save Data");
		btnSaveData.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent arg0) {
				buttonSaveDataEvent(gc);
			}
		});
		btnSaveData.setLayoutX(25);
		btnSaveData.setLayoutY(155);
		btnSaveData.setMinWidth(100);
		root.getChildren().add(btnSaveData);
		
		createPopupMenu(gc);

		hexImage = new Image("/hex_map.png");
		gc.drawImage(hexImage, 0, 0);
		
		root.getChildren().add(canvas);
		root.getChildren().add(systemOutput);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}

}

