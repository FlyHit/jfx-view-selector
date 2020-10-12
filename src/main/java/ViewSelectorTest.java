import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewSelectorTest extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewSelector viewSelector = new ViewSelector();
        viewSelector.setViewHeight(400);
        viewSelector.setMaxViewNum(4);
        viewSelector.setRoundRadius(0);
        List<byte[]> images = getImages();
        viewSelector.setThumbnails(images);
        Stage stage = viewSelector.getStage();
        stage.setHeight(600);
        stage.show();
    }

    private List<byte[]> getImages() throws URISyntaxException, IOException {
        List<byte[]> images = new ArrayList<>();
        File imageFolder = new File(getClass().getResource("/images").toURI());
        for (File image : Objects.requireNonNull(imageFolder.listFiles())) {
            images.add(Files.readAllBytes(image.toPath()));
        }
        return images;
    }
}
