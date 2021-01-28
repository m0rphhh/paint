package sample;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;
import java.io.*;

public class ImageEditor extends Application {
    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    static Mat mainImage = new Mat(1080, 1920, CvType.CV_8UC3, new Scalar(255, 255, 255));

    static ImageView imageView = new ImageView();
    static AnchorPane blueprintLayout = new AnchorPane();
    static BorderPane imgLayout = new BorderPane();

    @Override
    public void start(Stage stage) throws Exception {
        SplitPane root = new SplitPane();
        root.setOrientation(Orientation.VERTICAL);
        root.setDividerPosition(0, 0.7);

        imgLayout.setMinSize(0, 0);
        imgLayout.setMaxHeight(mainImage.rows());
        blueprintLayout.setMinSize(0, 0);
        blueprintLayout.setStyle("-fx-background-color: #F4F4F4;");

        VBox imgLayoutVBox = new VBox();
        imgLayoutVBox.setAlignment(Pos.CENTER);


        MenuBar menuBar = new MenuBar();
        Menu filterMenu = new Menu("Эффект..");
        MenuItem ChangeSaturation = new MenuItem("Насыщенность");
        MenuItem ChangeContrast = new MenuItem("Негатив");
        MenuItem ChangeSepia = new MenuItem("Сепия");
        MenuItem Sharpness = new MenuItem("Резкость");
        MenuItem VertFlip = new MenuItem("Отразить по вертикали");
        MenuItem GorFlip = new MenuItem("Отразить по горизонтали");
        MenuItem BothFlip = new MenuItem("Отразить по вертикали и горизонтали");

        Menu fileMenu = new Menu("Файл");
        MenuItem save = new MenuItem("Сохранить");
        MenuItem saveAs = new MenuItem("Сохранить как...");

        fileMenu.getItems().addAll(save, saveAs);
        filterMenu.getItems().addAll(ChangeSaturation, ChangeContrast, ChangeSepia, Sharpness, VertFlip,
                GorFlip, BothFlip);
        menuBar.getMenus().addAll(fileMenu, filterMenu);

        DraggableNode startBlueprint = new DraggableNode();
        Nodes.pool.add(startBlueprint);
        stage.getIcons().add(new Image("https://img.icons8.com/ios-filled/2x/apex-legends.png"));


        ChangeSaturation.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.ChangeSaturation;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        ChangeContrast.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.SetNegative;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        save.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                boolean st = Imgcodecs.imwrite(Nodes.currentImagePath, mainImage);
                if (!st)
                    System.out.println("Не удалось сохранить изображение");
                else
                    System.out.println("Успешно сохранено");
            }
        });

        saveAs.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("nameless");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image","*.png", "*.jpg", "*.webp"));
                File someFile = fileChooser.showSaveDialog(null);
                String newFilePath = someFile.toURI().toString().substring(6);
                boolean st = Imgcodecs.imwrite(newFilePath, mainImage);
                if (!st)
                    System.out.println("Не удалось сохранить изображение");
                else
                    System.out.println("Успешно сохранено");
            }
        });
        Sharpness.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.Sharpness;
                DraggableNode toAdd = new DraggableNode();
                Nodes.pool.add(toAdd);
                blueprintLayout.getChildren().add(toAdd);
            }
        });

        ChangeSepia.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.AddSepia;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        VertFlip.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.VertFlip;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        GorFlip.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.GorFlip;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        BothFlip.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Nodes.prefMode = BlueprintMode.BothFlip;
                blueprintLayout.getChildren().add(new DraggableNode());
            }
        });

        ScrollPane ScrollPane = new ScrollPane(imageView);
        ScrollPane.setFitToWidth(true);
        imgLayoutVBox.getChildren().addAll(ScrollPane, menuBar);

        blueprintLayout.getChildren().add(startBlueprint);
        root.getItems().addAll(imgLayoutVBox, blueprintLayout);
        imageView.setImage(MatToImageFX(mainImage));
        
        stage.setTitle("avatan lol");
        stage.setScene(new Scene(root, 1300.0, 800.0));
        stage.show();
    }

    public static void main(String args[]){
        launch(args);
    }

    public static void RefreshImage(){
        DraggableNode current = Nodes.pool.get(0);
        while(current != null)
        {
            current.doAction();
            current = current.getNextActionNode();
        }
    }

    public static void changeSaturation(Integer value1, Integer value2, Integer value3){

        Mat imgTransform = new Mat();
        Imgproc.cvtColor(mainImage, imgTransform, Imgproc.COLOR_BGR2HSV);
        Core.add(imgTransform, new Scalar(value1, value2, value3), imgTransform);
        Imgproc.cvtColor(imgTransform, mainImage, Imgproc.COLOR_HSV2BGR);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void setNegative(){

        Mat m = new Mat(mainImage.rows(), mainImage.cols(), mainImage.type(), new Scalar(255, 255, 255));
        Mat negative = new Mat();
        Core.subtract(m, mainImage, negative);
        mainImage = negative;
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void SetImage(String path){

        Mat imgToAdd = Imgcodecs.imread(path);
        if (imgToAdd.empty()) {
            System.out.println("Не удалось загрузить изображение");
            mainImage = new Mat(1080, 1920, CvType.CV_8UC3, new Scalar(255, 255, 255));
        }
        else
        {
            mainImage = imgToAdd;
        }

        imgLayout.setMaxHeight(mainImage.rows());
        imageView.setImage(MatToImageFX(mainImage));

    }

    public static void Sharpness(){
        Mat imgTransform = mainImage;
        double n = 1.0;
        Mat sharp = new Mat(3, 3, CvType.CV_32FC1);
        sharp.put(0, 0,
                n - 1, -n, n - 1,
                -n, n + 5, -n,
                n - 1, -n, n - 1);
        Core.divide(sharp, new Scalar(n + 1), sharp);
        Imgproc.filter2D(imgTransform, mainImage, -1, sharp);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void AddSepia(){
        Mat imgTransform = mainImage;
        Mat useSepia = new Mat(3, 3, CvType.CV_32FC1);
        useSepia.put(0, 0,
                0.131, 0.534, 0.272, // blue = b * b1 + g * g1 + r * r1
                0.168, 0.686, 0.349, // green = b * b2 + g * g2 + r * r2
                0.189, 0.769, 0.393 // red = b * b3 + g * g3 + r * r3
        );
        Core.transform(imgTransform, mainImage, useSepia);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void VertFlip(){
        Mat flipped = mainImage;
        Core.flip(flipped, mainImage, 0);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void GorFlip(){
        Mat flipped = mainImage;
        Core.flip(flipped, mainImage, 1);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static void BothFlip(){
        Mat flipped = mainImage;
        Core.flip(flipped, mainImage, -1);
        imageView.setImage(MatToImageFX(mainImage));
    }

    public static WritableImage MatToImageFX(Mat m) {
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U) {}
        else if (m.depth() == CvType.CV_16U) {
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            m = m_16;
        }
        else if (m.depth() == CvType.CV_32F) {
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        }
        else
            return null;
        if (m.channels() == 1) {
            Mat m_bgra = new Mat();
            Imgproc.cvtColor(m, m_bgra, Imgproc.COLOR_GRAY2BGRA);
            m = m_bgra;
        }
        else if (m.channels() == 3) {
            Mat m_bgra = new Mat();
            Imgproc.cvtColor(m, m_bgra, Imgproc.COLOR_BGR2BGRA);
            m = m_bgra;
        }
        else if (m.channels() == 4) { }
        else
            return null;
        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        WritableImage wim = new WritableImage(m.cols(), m.rows());
        PixelWriter pw = wim.getPixelWriter();
        pw.setPixels(0, 0, m.cols(), m.rows(),
                WritablePixelFormat.getByteBgraInstance(),
                buf, 0, m.cols() * 4);
        return wim;
    }
}