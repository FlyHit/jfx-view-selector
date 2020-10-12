import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Jiongyu
 */
public class ViewSelector {
    private Stage stage;
    private HBox navigatePane;
    private HBox viewBox;
    private Button previousBtn;
    private Button nextBtn;
    private Button addBtn;

    private List<byte[]> thumbnails;
    private List<Node> thumbnailViews;
    private Map<Node, StringProperty> viewToName;
    private Map<Node, BooleanProperty> viewToRemoved;

    private double stageWidth;
    private double stageHeight;
    private double stageX;
    private double stageY;
    private int maxViewNum = 4;

    private final IntegerProperty focusViewIndex;
    private final IntegerProperty beginIndex;

    private static final String TRANSPARENT = "transparent-control";
    private static final String VIEW_FOCUSED = "current-focused-view";
    private static final Color ICON_COLOR = Color.gray(0.6);

    public ViewSelector() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stageWidth = screenBounds.getWidth();
        stageHeight = 500;
        stageX = (screenBounds.getWidth() - stageWidth) / 2;
        stageY = (screenBounds.getHeight() - stageHeight) / 2;

        beginIndex = new SimpleIntegerProperty();
        beginIndex.addListener((observable, oldValue, newValue) -> {
            previousBtn.setDisable(newValue.intValue() <= 0);
            nextBtn.setDisable(newValue.intValue() >= thumbnailViews.size() - maxViewNum);
        });
        focusViewIndex = new SimpleIntegerProperty();
        focusViewIndex.addListener((observable, oldValue, newValue) -> {
            updateFocusView(newValue.intValue());
        });

        initStage();
        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-background-color: rgba(255,255,255,1);");

        initNavigatePane();
        stackPane.getChildren().add(navigatePane);

        Scene scene = new Scene(stackPane);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("NavigateView.css").toExternalForm());
        stage.setScene(scene);

        thumbnailViews = new ArrayList<>();
        viewToName = new HashMap<>();
        viewToRemoved = new HashMap<>();
    }

    private void updateFocusView(int index) {
        int viewCount = viewBox.getChildren().size();
        if (viewCount == 0) {
            previousBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }

        for (int i = 0; i < viewBox.getChildren().size(); i++) {
            Node view = viewBox.getChildren().get(i);
            if (i == index) {
                view.getStyleClass().add(VIEW_FOCUSED);
            } else {
                view.getStyleClass().remove(VIEW_FOCUSED);
            }
        }
    }

    private void initStage() {
        stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        stage.setX(stageX);
        stage.setY(stageY);

        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                stage.close();
            }
        });
    }

    private void initViewBox() {
        viewBox = new HBox();
        viewBox.setFocusTraversable(false);
        viewBox.setSpacing(60);
        viewBox.setAlignment(Pos.CENTER);
    }

    private void initNavigatePane() {
        navigatePane = new HBox();
        navigatePane.setFocusTraversable(false);
        navigatePane.setSpacing(60);
        navigatePane.setAlignment(Pos.CENTER);

        previousBtn = new Button();
        FontAwesomeIconView leftIcon = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_CIRCLE_LEFT);
        leftIcon.setGlyphSize(64);
        leftIcon.setFill(ICON_COLOR);
        previousBtn.setGraphic(leftIcon);
        previousBtn.setDisable(true);
        previousBtn.getStyleClass().add(TRANSPARENT);
        previousBtn.setOnAction(event -> {
            beginIndex.setValue(beginIndex.get() - 1);
            updateViews();
        });

        nextBtn = new Button();
        FontAwesomeIconView rightIcon = new FontAwesomeIconView(FontAwesomeIcon.CHEVRON_CIRCLE_RIGHT);
        rightIcon.setGlyphSize(64);
        rightIcon.setFill(ICON_COLOR);
        nextBtn.setGraphic(rightIcon);
        nextBtn.getStyleClass().add(TRANSPARENT);
        nextBtn.setOnAction(event -> {
            beginIndex.setValue(beginIndex.get() + 1);
            updateViews();
        });

        initViewBox();
        initAddBtn();

        navigatePane.getChildren().add(0, previousBtn);
        navigatePane.getChildren().add(1, viewBox);
        navigatePane.getChildren().add(2, addBtn);
        navigatePane.getChildren().add(3, nextBtn);
    }

    private void initAddBtn() {
        addBtn = new Button();
        addBtn.getStyleClass().add(TRANSPARENT);
        addBtn.setId("add-view-button");
        addBtn.setPrefWidth(200);
        FontAwesomeIconView addIcon = new FontAwesomeIconView(FontAwesomeIcon.PLUS);
        addIcon.setGlyphSize(64);
        addIcon.setFill(ICON_COLOR);
        addBtn.setGraphic(addIcon);
        addBtn.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                focusViewIndex.setValue(-1);
            }
        });
    }

    public Stage getStage() {
        return stage;
    }

    public void updateViews() {
        makeThumbnailViews();
        viewBox.getChildren().clear();
        for (int i = beginIndex.get(); i < beginIndex.get() + maxViewNum; i++) {
            if (i < thumbnailViews.size()) {
                viewBox.getChildren().add(thumbnailViews.get(i));
            }
        }

        addBtn.setPrefHeight(ThumbnailViewController.viewHeight);
        updateFocusView(focusViewIndex.get());
    }

    private void makeThumbnailViews() {
        thumbnailViews.clear();
        viewToRemoved.clear();
        viewToName.clear();
        for (byte[] thumbnail : thumbnails) {
            FXMLLoader fxmlLoader = new FXMLLoader();
            try {
                Node thumbnailView =
                        fxmlLoader.load(getClass().getResource("ThumbnailView.fxml").openStream());
                ThumbnailViewController controller = fxmlLoader.getController();
                controller.setThumbnail(thumbnail);
                controller.setName("test");
                thumbnailView.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        if (viewBox.getChildren().contains(thumbnailView)) {
                            int index = viewBox.getChildren().indexOf(thumbnailView);
                            focusViewIndex.setValue(index);
                        }
                    }
                });
                thumbnailView.setOnMousePressed(event -> {
                    if (viewBox.getChildren().contains(thumbnailView)) {
                        int index = viewBox.getChildren().indexOf(thumbnailView);
                        focusViewIndex.setValue(index);
                    }
                });
                controller.removedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        int index = viewBox.getChildren().indexOf(thumbnailView);
                        removeThumbnail(index);
                    }
                });
                thumbnailViews.add(thumbnailView);
                viewToName.put(thumbnailView, controller.nameProperty());
                viewToRemoved.put(thumbnailView, controller.removedProperty());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addThumbnail(byte[] thumbnail) {
        thumbnails.add(thumbnail);
        updateViews();
    }

    public void removeThumbnail(byte[] thumbnail) {
        thumbnails.remove(thumbnail);
        updateViews();
    }

    public void removeThumbnail(int index) {
        thumbnails.remove(index);
        updateViews();
    }

    public void setThumbnails(List<byte[]> thumbnails) {
        this.thumbnails = thumbnails;
        updateViews();
    }

    public double getViewHeight() {
        return ThumbnailViewController.viewHeight;
    }

    public void setViewHeight(double viewHeight) {
        ThumbnailViewController.viewHeight = viewHeight;
    }

    public int getMaxViewNum() {
        return maxViewNum;
    }

    public void setMaxViewNum(int maxViewNum) {
        this.maxViewNum = maxViewNum;
    }

    public double getRoundRadius() {
        return ThumbnailViewController.roundRadius;
    }

    public void setRoundRadius(double roundRadius) {
        ThumbnailViewController.roundRadius = roundRadius;
    }

    public Map<Node, StringProperty> getViewToName() {
        return viewToName;
    }

    public Map<Node, BooleanProperty> getViewToRemoved() {
        return viewToRemoved;
    }
}
