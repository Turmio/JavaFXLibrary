/*
 * Copyright 2017-2018   Eficode Oy
 * Copyright 2018-       Robot Framework Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javafxlibrary.keywords.Keywords;

import javafxlibrary.exceptions.JavaFXLibraryNonFatalException;
import javafxlibrary.utils.finder.Finder;
import javafxlibrary.utils.HelperFunctions;
import javafxlibrary.utils.RobotLog;
import javafxlibrary.utils.TestFxAdapter;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

@RobotKeywords
public class PointLocation extends TestFxAdapter {

    @RobotKeyword("Sets the current position pointer to a point located using given locator and returns a PointQuery object for it. \n\n"
            + "``locator`` is either a _query_ or _Object:Bounds, Node, Point2D, Scene, Window_ for identifying the element, see "
            + "`3. Locating or specifying UI elements`. \n\n"
            + "\nExample: \n"
            + "| ${point query}= | Point To | ${node} |\n"
            + "| Move To | ${point query} | \n"
            + "| ${point query position}= | Call Method | ${point query} | getPosition | \n"
            + "| Set Target Position | BOTTOM_RIGHT | \n"
            + "| ${point query}= | Point To | ${some node} | \n"
            + "| Move To | ${point query} | | | # moves to bottom right corner of a node that was stored in PointQuery object. |\n")
    @ArgumentNames({"locator"})
    public Object pointTo(Object locator) {
        RobotLog.info("Creating a point query for target \"" + locator + "\"");

        Object resolvedLocator = (locator instanceof String)
                ? new Finder().find((String) locator)
                : locator;

        AtomicReference<Object> point = new AtomicReference<>();
        AtomicReference<JavaFXLibraryNonFatalException> error = new AtomicReference<>();
        robot.interact(() -> {
            try {
                Method method = MethodUtils.getMatchingAccessibleMethod(robot.getClass(), "point", resolvedLocator.getClass());
                point.set(HelperFunctions.mapObject(method.invoke(robot, resolvedLocator)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                error.set(new JavaFXLibraryNonFatalException("Could not execute point to using locator \"" + resolvedLocator
                        + "\": " + e.getCause().getMessage()));
            }
        });

        if(error.get() != null) {
            throw error.get();
        }

        return point.get();
    }

    @RobotKeyword("Sets the current position pointer to new location based on x,y coordinates and returns a PointQuery object for it.\n\n"
            + "``x`` and ``y`` defines the Integer values for the x- and y -coordinates.\n\n"
            + "\nExample: \n"
            + "| ${point query}= | Point To Coordinates | 100 | 200 | \n")
    @ArgumentNames({"x", "y"})
    public Object pointToCoordinates(int x, int y) {
        AtomicReference<Object> point = new AtomicReference<>();
        AtomicReference<JavaFXLibraryNonFatalException> error = new AtomicReference<>();
        RobotLog.info("Returning a pointquery to coordinates: [" + x + ", " + y + "]");
        robot.interact(() -> {
            try {
                point.set(HelperFunctions.mapObject(robot.point((double) x, (double) y)));
            } catch (Exception e) {
                if(e instanceof JavaFXLibraryNonFatalException)
                    error.set((JavaFXLibraryNonFatalException)e);
                error.set(new JavaFXLibraryNonFatalException("Unable to point to coordinates: [" + x + ", " + y + "]", e));
            }
        });

        if(error.get() != null) {
            throw error.get();
        }

        return point.get();
    }
}