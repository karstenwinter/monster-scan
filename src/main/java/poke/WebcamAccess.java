package poke;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Map;

public class WebcamAccess extends Application {
    String url = "";

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }

    public WebcamAccess() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox hBox = new VBox();
        final ImageView imageView = new ImageView();
        hBox.setPrefHeight(600 + 100);
        hBox.setPrefWidth(800 + 100);
        final Label status = new Label();
        Button button = new Button("Exit");

        final Property<Boolean> aborted = new SimpleBooleanProperty(false);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                exit(status, aborted);
            }
        });

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                exit(status, aborted);
            }
        });

        final WebView webView = new WebView();
        hBox.getChildren().addAll(status, imageView, button, webView);

        primaryStage.setScene(new Scene(hBox));
        primaryStage.show();

        status.setText("Loading Cam...");
        final Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open(false);

        status.setText("Loading OCR...");
        final OCR ocr = new OCR();
        ocr.loadOCR();

        status.setText("Loading Online List...");
        ocr.loadList();

        status.setText("Starting recognition...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!aborted.getValue()) {
                    try {

                        final BufferedImage image = webcam.getImage();
                        // webcam.close();
                        // webcam.getDevice().dispose();
                        if (image != null) {
                            StringBuffer sb = new StringBuffer();
                            final Image image2 = SwingFXUtils.toFXImage(image, null);

                            final String value = "Got Image at " + new Date();
                            System.out.println(value);
                            final SimpleStringProperty urlStr = new SimpleStringProperty("");
                            try {
                                String name = "temp";
                                Map.Entry<Boolean, String> f = null;
                                f = ocr.process(image, name, false, sb);

                                if (f == null) {
                                    f = ocr.process(image, name, true, sb);

                                    if (f == null) {
                                        System.err.println("! " + name + "=>" + sb.toString().replaceAll("\n", " "));
                                    }
                                }
                                if (f != null && f.getValue() != null) {
                                    urlStr.setValue(f.getValue());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImage(image2);
                                    status.setText(value);

                                    String newUrl = urlStr.getValue();
                                    if (!url.equals(newUrl) && newUrl != null && newUrl.length() != 0) {
                                        url = newUrl;
                                        webView.getEngine().load(url);
                                    }
                                }
                            });
                            if (false) {
                                File output = new File("cam.png");
                                ImageIO.write(image, "PNG", output);
                                FileInputStream is = new FileInputStream(output);
                                is.close();
                            }
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        //System.err.println(e.toString());
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();

        /*webcam.addWebcamListener(new WebcamListener() {
            @Override
            public void webcamOpen(WebcamEvent we) {
            }

            @Override
            public void webcamClosed(WebcamEvent we) {
            }

            @Override
            public void webcamDisposed(WebcamEvent we) {
            }

            @Override
            public void webcamImageObtained(WebcamEvent we) {
                Image image = SwingFXUtils.toFXImage(we.getImage(), null);

                imageView.setImage(image);
            }
        });*/
    }

    private void exit(Label status, Property<Boolean> aborted) {
        status.setText("Exiting...");
        aborted.setValue(true);
        System.exit(0);
    }
}
