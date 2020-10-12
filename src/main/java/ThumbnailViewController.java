import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.controlsfx.tools.Borders;

import java.io.ByteArrayInputStream;


/**
 * @author Chen Jiongyu
 */
public class ThumbnailViewController {
    @FXML
    private VBox thumbnailView;
    @FXML
    private StackPane stackPane;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField nameField;

    private final StringProperty name;
    private final BooleanProperty removed;
    static double viewHeight = 400;
    static double roundRadius = 20;

    public ThumbnailViewController() {
        name = new SimpleStringProperty();
        removed = new SimpleBooleanProperty();
        removed.setValue(false);
    }

    @FXML
    private void initialize() {
        thumbnailView.setAlignment(Pos.CENTER);
        initNameField();

        stackPane.prefWidthProperty().bind(imageView.fitWidthProperty());
        Label removeViewBtn = new Label();
        ImageView removeIcon = new ImageView("icons/close_32.png");
        removeIcon.setFitWidth(16);
        removeIcon.setPreserveRatio(true);
        removeViewBtn.setGraphic(removeIcon);
        removeViewBtn.setOnMouseClicked(event -> {
            removed.setValue(true);
        });
        removeViewBtn.setId("remove-btn");
        Decorator.addDecoration(stackPane, new GraphicDecoration(removeViewBtn, Pos.TOP_RIGHT, 16,16));
    }

    private void initNameField() {
        nameField.setAlignment(Pos.CENTER);
        nameField.setFont(new Font(nameField.getFont().getName(), 20));
        nameField.prefWidthProperty().bind(imageView.fitWidthProperty());
        nameField.setEditable(false);
        nameField.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                int count = event.getClickCount();
                if (count > 1) {
                    nameField.setEditable(true);
                }
            }
        });
        nameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                nameField.setEditable(false);
            }
        });
        nameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                nameField.setEditable(false);
            }
        });

        nameField.editableProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                name.setValue(nameField.getText());
            }
        });
    }

    public void setThumbnail(byte[] thumbnail) {
        Image thumbnailImg = new Image(new ByteArrayInputStream(thumbnail));
        imageView.setImage(thumbnailImg);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(viewHeight);
        imageView.setSmooth(true);

        if (roundRadius > 0) {
            double ivWidth = thumbnailImg.getWidth() / thumbnailImg.getHeight() * viewHeight;
            Rectangle clip = new Rectangle(ivWidth, imageView.getFitHeight());
            clip.setArcWidth(roundRadius);
            clip.setArcHeight(roundRadius);
            imageView.setClip(clip);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage image = imageView.snapshot(parameters, null);
            // remove the rounding clip and use the newly created image so that our effect can show through
            imageView.setClip(null);
            imageView.setImage(image);
        }
    }

    public VBox getThumbnailView() {
        return thumbnailView;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public boolean getRemoved() {
        return removed.get();
    }

    public BooleanProperty removedProperty() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed.set(removed);
    }
}
