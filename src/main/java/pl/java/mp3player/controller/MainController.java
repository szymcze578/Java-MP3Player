package pl.java.mp3player.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import pl.java.mp3player.mp3.Mp3Parser;
import pl.java.mp3player.mp3.Mp3Song;
import pl.java.mp3player.player.MP3Player;

import java.io.File;
import java.io.IOException;

public class MainController {

    @FXML
    private ControlPaneController controlPaneController;

    @FXML
    private ContentPaneController contentPaneController;

    @FXML
    private MenuPaneController menuPaneController;

    private MP3Player player;

    public void initialize() {
        createPlayer();
        configureTableClick();
        configureButtons();
        addTestMp3();
        configureMenu();
    }


    private void createPlayer() {
        ObservableList<Mp3Song> items = contentPaneController.getContentTable().getItems();
        player = new MP3Player(items);
    }

    private void configureTableClick() {
        TableView<Mp3Song> contentTable = contentPaneController.getContentTable();
        contentTable.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                int indexSelected = contentTable.getSelectionModel().getSelectedIndex();
                playSelectedSong(indexSelected);
            }
        });
    }


    private void playSelectedSong(int indexSelected) {
        player.loadSong(indexSelected);
        configureProgressBar();
        configureVolume();
        controlPaneController.getPlayButton().setSelected(true);
    }

    private void configureVolume() {
        Slider volumeSlider = controlPaneController.getVolumeSlider();
        volumeSlider.valueProperty().unbind();
        volumeSlider.setMax(1.0);
        volumeSlider.valueProperty().bindBidirectional(player.getMediaPlayer().volumeProperty());
    }

    private void configureProgressBar() {
        Slider progressSlider = controlPaneController.getProgressSlider();
        //ustaw dlugość suwaga postępu
        player.getMediaPlayer().setOnReady(() -> progressSlider.setMax(player.getLoadedSongLength()));
        //zmiana czasu w odtwarzaczu
        player.getMediaPlayer().currentTimeProperty().addListener((arg, oldVal, newVal) -> progressSlider.setValue(newVal.toSeconds()));
        //przesuniecie suwaka
        progressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (progressSlider.isValueChanging())
                player.getMediaPlayer().seek(Duration.seconds(newValue.doubleValue()));
        });
    }

    private void configureButtons() {
        TableView<Mp3Song> contentTable = contentPaneController.getContentTable();
        ToggleButton playButton = controlPaneController.getPlayButton();
        Button prevButton = controlPaneController.getPreviousButton();
        Button nextButton = controlPaneController.getNextButton();

        playButton.setOnAction(event -> {
            if (playButton.isSelected()) {
                player.play();
            } else {
                player.stop();
            }
        });

        nextButton.setOnAction(event -> {
            contentTable.getSelectionModel().select(contentTable.getSelectionModel().getSelectedIndex() + 1);
            playSelectedSong(contentTable.getSelectionModel().getSelectedIndex());
        });

        prevButton.setOnAction(event -> {
            contentTable.getSelectionModel().select(contentTable.getSelectionModel().getSelectedIndex() - 1);
            playSelectedSong(contentTable.getSelectionModel().getSelectedIndex());
        });
    }

    private void addTestMp3() {
        ObservableList<Mp3Song> items = contentPaneController.getContentTable().getItems();
        Mp3Song mp3SongFromPath = createMp3SongFromPath("super-max.mp3");
        items.add(mp3SongFromPath);
        items.add(mp3SongFromPath);
        items.add(mp3SongFromPath);
    }

    private Mp3Song createMp3SongFromPath(String filePath) {
        File file = new File(filePath);
        try {
            MP3File mp3File = new MP3File(file);
            String absolutePath = file.getAbsolutePath();
            String title = mp3File.getID3v2Tag().getSongTitle();
            String author = mp3File.getID3v2Tag().getLeadArtist();
            String album = mp3File.getID3v2Tag().getAlbumTitle();
            return new Mp3Song(title, author, album, absolutePath);
        } catch (IOException | TagException e) {
            e.printStackTrace();
            return null; //ignore
        }
    }

    private void configureMenu() {
        MenuItem openFile = menuPaneController.getFileMenuItem();
        MenuItem openDir = menuPaneController.getDirMenuItem();

        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mp3", "*.mp3"));
            File file = fileChooser.showOpenDialog(new Stage());
            try {
                contentPaneController.getContentTable().getItems().add(Mp3Parser.createMp3Song(file));
                showMessage("Załadowano plik " + file.getName());
            } catch (TagException | IOException e) {
                showMessage("Nie można otworzyć pliku " + file.getName());
            }
        });

        openDir.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(new Stage());
            try {
                contentPaneController.getContentTable().getItems().addAll(Mp3Parser.createMp3List(directory));
                showMessage("Wczytano dane z foleru " + directory.getName());
            } catch (TagException | IOException e) {
                showMessage("Wystąpił błąd podczas odczytu folderu");
            }
        });
    }

    private void showMessage(String message) {
        controlPaneController.getMessageTextField().setText(message);
    }
}