package javafxlibrary.utils;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafxlibrary.TestFxAdapterTest;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.testfx.api.FxRobot;

import java.util.ArrayList;
import java.util.List;

public class FinderTest extends TestFxAdapterTest {

    private Finder finder;
    @Mocked Stage stage;
    @Mocked VBox root;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        finder = new Finder();
    }

    @Test
    public void find_DefaultRoot_NewParameterTypesChained() {
        Button button = new Button();
        button.getStyleClass().add("anotherClass");
        Group subGroup = new Group();
        subGroup.getStyleClass().add("sub=group");
        subGroup.getChildren().add(button);
        Group group = new Group();
        group.getStyleClass().addAll("test", "orangeBG");
        group.getChildren().add(subGroup);
        HBox hBox = new HBox();
        hBox.setId("testNode");
        hBox.getChildren().add(group);

        List<Window> windows = new ArrayList<>();
        windows.add(stage);
        new Expectations() {
            {
                getRobot().listTargetWindows(); result = windows;
                stage.getScene().getRoot(); result = root;
                root.lookupAll((String) any); result = hBox;
            }
        };
        Node result = finder.find("id=testNode css=.test.orangeBG .sub=group css=.anotherClass");
        Assert.assertEquals(button, result);
    }

    @Test
    public void find_CustomRoot_TestFXSelector() {
        Group group = new Group();
        HBox hBox = new HBox();
        Button button = new Button("Target");
        hBox.getChildren().add(button);
        group.getChildren().add(hBox);

        // Use a real FxRobot instance for this test
        FxRobot robot = new FxRobot();
        TestFxAdapter.setRobot(robot);

        new Expectations() {
            {
                robot.from(group).query(); result = group;
            }
        };

        Node result = finder.find(".button", group);
        Assert.assertEquals(button, result);
    }

    @Test
    public void getPrefix_AcceptedValues() {
        Assert.assertEquals(Finder.FindPrefix.ID, finder.getPrefix("id=nodeId"));
        Assert.assertEquals(Finder.FindPrefix.CLASS, finder.getPrefix("class=java.lang.String"));
        Assert.assertEquals(Finder.FindPrefix.CSS, finder.getPrefix("css=.vBox .hBox"));
        Assert.assertEquals(Finder.FindPrefix.XPATH, finder.getPrefix("xpath=//Rectangle[@id=\"lime\"]"));
        Assert.assertEquals(Finder.FindPrefix.PSEUDO, finder.getPrefix("pseudo=hover"));
        Assert.assertEquals(Finder.FindPrefix.TEXT, finder.getPrefix("text=\"Text\""));
    }

    @Test
    public void getPrefix_NoEquals() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Query \"noEquals\" does not contain any supported prefix");
        finder.getPrefix("noEquals");
    }

    @Test
    public void getPrefix_InvalidValue() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Query \"notaprefix=someValue\" does not contain any supported prefix");
        finder.getPrefix("notaprefix=someValue");
    }
}
