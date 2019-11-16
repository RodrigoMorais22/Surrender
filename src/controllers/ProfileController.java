package controllers;

import business.*;
import data.enums.ImagesUrl;
import data.enums.Tiers;
import data.api.ApiHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;
import utils.GraphicUtils;
import utils.NumberUtils;
import utils.RiotUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProfileController implements Initializable {
    private Executor exec;

    @FXML
    Label playerLevel;

    @FXML
    Circle imageCircle;

    @FXML
    Label summonerName;

    @FXML
    ImageView tier;

    @FXML
    Label leaguePoints;

    @FXML
    Label rankedWins;

    @FXML
    Label rankedLosses;

    // Top played champions components
    @FXML
    Circle championTop1;
    @FXML
    Circle championTop2;
    @FXML
    Circle championTop3;

    // Match history champion icons components
    @FXML
    Circle playedMatchChampionIcon1;
    @FXML
    Circle playedMatchChampionIcon2;
    @FXML
    Circle playedMatchChampionIcon3;

    @FXML
    Label kdaMatchHistory1;
    @FXML
    Label kdaMatchHistory2;
    @FXML
    Label kdaMatchHistory3;

    @FXML
    Label championName1;
    @FXML
    Label championName2;
    @FXML
    Label championName3;

    @FXML
    Label calculatedKDA1;
    @FXML
    Label calculatedKDA2;
    @FXML
    Label calculatedKDA3;

    @FXML
    Label gameDuration1;
    @FXML
    Label gameDuration2;
    @FXML
    Label gameDuration3;

    @FXML
    Label result1;
    @FXML
    Label result2;
    @FXML
    Label result3;


    @FXML
    HBox playedMatch1;

    @FXML
    HBox playedMatchItems11;
    @FXML
    HBox playedMatchItems12;

    @FXML
    HBox playedMatchItems21;
    @FXML
    HBox playedMatchItems22;

    @FXML
    HBox playedMatchItems31;
    @FXML
    HBox playedMatchItems32;
    @FXML
    private BorderPane bp;

    private List<Rectangle> rectangles;
    private String searchedSummoner;

    @FXML
    private void handleButtonAction() {
        System.exit(0);
    }

    @FXML
    private void minimizeButtonAction() {
        Stage stage = (Stage) bp.getScene().getWindow();
        stage.setIconified(true);
    }


    @FXML
    private void analysesButtonAction() throws IOException {
        Stage s = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("../screens/Analyses.fxml"));
        s.setScene(new Scene(root));
        s.initStyle(StageStyle.TRANSPARENT);
        s.show();
    }

    public void initialize(URL url, ResourceBundle rb) {
        exec = Executors.newCachedThreadPool(runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    private void backButtonAction() throws IOException {

        Stage s = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("../screens/Menu.fxml"));
        s.setScene(new Scene(root));
        s.initStyle(StageStyle.TRANSPARENT);
        s.show();

        Stage stage = (Stage) bp.getScene().getWindow();
        stage.close();
    }

    void initData(Player searchedSummoner, Pane menuBp) throws Exception {

        ApiHelper apiHelper = new ApiHelper();
        JSONObject championArrData = apiHelper.getChampionData().getJSONObject("data");

        // Get summoner ranked data
        Task<Ranked> rankedSummonerInfoTask = new Task<Ranked>() {
            @Override
            protected Ranked call() throws Exception {
                return new Ranked(searchedSummoner.getSummonerId());
            }
        };

        rankedSummonerInfoTask.setOnFailed(eRanked -> {
            rankedSummonerInfoTask.getException().printStackTrace();
        });

        rankedSummonerInfoTask.setOnSucceeded(eRanked -> {
            Ranked rankedSummonerInfo = rankedSummonerInfoTask.getValue();

            // Set league points text
            leaguePoints.setText(rankedSummonerInfo.getLeaguePoints() + " LP");

            // Set league points text
            rankedWins.setText(rankedSummonerInfo.getWins() + " W");

            // Set league points text
            rankedLosses.setText(rankedSummonerInfo.getLosses() + " L");

            // Set tier image
            File file = new File(Tiers.valueOf(rankedSummonerInfo.getTier()).getImageEloPath());
            tier.setImage(new Image(file.toURI().toString()));
        });

        exec.execute(rankedSummonerInfoTask);


        // Get top played champions info of searched summoner
        Task<SummonerChampions> summonerChampionsTask = new Task<SummonerChampions>() {
            @Override
            protected SummonerChampions call() throws Exception {
                return new SummonerChampions(
                        searchedSummoner.getSummonerId(),
                        championArrData
                );
            }
        };

        summonerChampionsTask.setOnFailed(eChampions -> {
            summonerChampionsTask.getException().printStackTrace();
        });

        summonerChampionsTask.setOnSucceeded(eChampions -> {
            SummonerChampions summonerChampions = summonerChampionsTask.getValue();
            // Display top played champions image icon
            Image championTop1Image = new Image(
                    RiotUtils
                            .getImageChampionBuiltUrl(
                                    summonerChampions.getTopPlayedChampions().get(0), ImagesUrl.SQUARE
                            )
            );
            championTop1.setFill(new ImagePattern(championTop1Image));

            Image championTop2Image = new Image(
                    RiotUtils
                            .getImageChampionBuiltUrl(
                                    summonerChampions.getTopPlayedChampions().get(1), ImagesUrl.SQUARE
                            )
            );
            championTop2.setFill(new ImagePattern(championTop2Image));

            Image championTop3Image = new Image(
                    RiotUtils
                            .getImageChampionBuiltUrl(summonerChampions.getTopPlayedChampions().get(2), ImagesUrl.SQUARE
                            )
            );
            championTop3.setFill(new ImagePattern(championTop3Image));
            Stage s = (Stage) menuBp.getScene().getWindow();
            s.close();
        });

        exec.execute(summonerChampionsTask);


        // Get match history of searched summoner
        Task<SummonerMatchList> summonerMatchListTask = new Task<SummonerMatchList>() {
            @Override
            protected SummonerMatchList call() throws Exception {
                return new SummonerMatchList(
                        searchedSummoner.getAccountId(),
                        3,
                        championArrData
                );
            }
        };

        summonerMatchListTask.setOnFailed(eMatch -> {
            summonerChampionsTask.getException().printStackTrace();
        });

        summonerMatchListTask.setOnSucceeded(eMatch -> {
            try {
                int matchSize = summonerMatchListTask.getValue().getPureMatchHistory().length();
                // Display match stats
                for (int i = 0; i < matchSize; i++) {

                    Match summonerMatch = new Match(
                            String.valueOf(
                                    ((JSONObject) summonerMatchListTask.getValue().getPureMatchHistory().get(i))
                                            .getInt("gameId")
                            )
                    );

                    JSONObject matchPlayerStats = summonerMatch.
                            getParticipantDtoBySummonerAccountId(searchedSummoner.getAccountId())
                            .getJSONObject("stats");

                    JSONObject participantChampion = summonerMatch.
                            getParticipantDtoBySummonerAccountId(searchedSummoner.getAccountId());

                    Champion champion = new Champion(
                            String.valueOf(participantChampion.getInt("championId")),
                            championArrData
                    );

                    String matchResult = summonerMatch.getMatchResultByParticipantId(
                            String.valueOf(participantChampion.getInt("participantId")));

                    String resultLabelColor = matchResult.equals("Victory")
                            ? "#31ab47"
                            : "#bf616a";

                    // Create items rectangles
                    rectangles = new ArrayList<>();
                    for (int j = 1; j <= 2; j++) {
                        rectangles = GraphicUtils.createRectangleItemsRow(
                                summonerMatch.getItemsSlotsByParticipantId(
                                        String.valueOf(participantChampion.getInt("participantId")), j),
                                3,
                                1
                        );
                        Field HBoxRectangleField = getClass().getDeclaredField("playedMatchItems" + (i + 1) + "" + j);
                        HBox HBox = (HBox) HBoxRectangleField.get(this);
                        HBox.getChildren().addAll(rectangles);
                    }

                    // Set game result match label
                    Field resultField = getClass().getDeclaredField("result" + (i + 1));
                    Label resultLabel = (Label) resultField.get(this);
                    resultLabel.setText(matchResult);
                    resultLabel.setTextFill(Paint.valueOf(resultLabelColor));

                    // Set game duration label
                    Field gameDurationField = getClass().getDeclaredField("gameDuration" + (i + 1));
                    Label gameDurationLabel = (Label) gameDurationField.get(this);
                    gameDurationLabel.setText(String.valueOf(summonerMatch.getGameDuration()));

                    // Set champion name
                    Field championNameField = getClass().getDeclaredField("championName" + (i + 1));
                    Label championNameLabel = (Label) championNameField.get(this);
                    championNameLabel.setText(
                            champion.getChampionData().getString("name")
                    );

                    // Set icon played champion
                    Field championIconField = getClass().getDeclaredField("playedMatchChampionIcon" + (i + 1));
                    Circle circle = (Circle) championIconField.get(this);
                    Image championImage = new Image(champion.getImageChampionBuiltUrl(ImagesUrl.SQUARE));
                    circle.setFill(new ImagePattern(championImage));

                    // Set kda match
                    Field kdaPlayerField = getClass().getDeclaredField("kdaMatchHistory" + (i + 1));
                    Label kdaLabel = (Label) kdaPlayerField.get(this);
                    kdaLabel.setText(
                            matchPlayerStats.getInt("kills") + "/" +
                                    matchPlayerStats.getInt("deaths") + "/" +
                                    matchPlayerStats.getInt("assists")
                    );

                    Field calculatedKdaPlayerField = getClass().getDeclaredField("calculatedKDA" + (i + 1));
                    Label calculatedKdaLabel = (Label) calculatedKdaPlayerField.get(this);
                    double calculatedKDA = NumberUtils.round(
                            (
                                    (matchPlayerStats.getDouble("kills") + matchPlayerStats.getDouble("assists"))
                                            / summonerMatch.setDeathToWhenItIsZero(matchPlayerStats.getDouble("deaths"))
                            ),
                            2
                    );
                    calculatedKdaLabel.setText("KDA " + calculatedKDA);
                    int a = 8;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        exec.execute(summonerMatchListTask);

        // Set profile image icon
        String profileIconId = String.valueOf(searchedSummoner.getIconId());
        Image image = new Image(
                "http://ddragon.leagueoflegends.com/cdn/9.21.1/img/profileicon/"
                        + profileIconId + ".png", false);
        imageCircle.setFill(new ImagePattern(image));

        // Set summoner level text
        playerLevel.setText(String.valueOf(searchedSummoner.getSummonerLevel()));

        // Set summoner name text
        summonerName.setText(searchedSummoner.getSummonerName());
    }
}
