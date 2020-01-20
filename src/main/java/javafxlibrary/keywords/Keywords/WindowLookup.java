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
import javafxlibrary.utils.HelperFunctions;
import javafxlibrary.utils.RobotLog;
import javafxlibrary.utils.TestFxAdapter;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RobotKeywords
public class WindowLookup extends TestFxAdapter {

    @RobotKeyword("Returns a list of all available windows currently open. \n\n "
        + "\nExample:\n"
        + "| ${windows}= | List Windows | \n"
        + "| Log List | ${windows} | \n")
    public List<Object> listWindows() {
            AtomicReference<List<Object>> windows = new AtomicReference<List<Object>>();
            AtomicReference<JavaFXLibraryNonFatalException> error = new AtomicReference<JavaFXLibraryNonFatalException>();
            robot.interact(() -> {
                try {
                    windows.set(HelperFunctions.mapObjects(robot.listWindows()));
                } catch (Exception e) {
                    if(e instanceof JavaFXLibraryNonFatalException)
                        error.set((JavaFXLibraryNonFatalException)e);
                    error.set(new JavaFXLibraryNonFatalException("Unable to list windows", e));
                }
            });
            if(error.get() != null) {
                throw error.get();
            }
            return windows.get();
    }

    @RobotKeyword("Returns a list of windows that are ordered by proximity to the last target window.\n\n")
    public List<Object> listTargetWindows() {
        AtomicReference<List<Object>> targetWindows = new AtomicReference<List<Object>>();
        AtomicReference<JavaFXLibraryNonFatalException> error = new AtomicReference<JavaFXLibraryNonFatalException>();
        robot.interact(() -> {
            try {
                targetWindows.set(HelperFunctions.mapObjects(robot.listTargetWindows()));
            } catch (Exception e) {
                if(e instanceof JavaFXLibraryNonFatalException)
                    error.set((JavaFXLibraryNonFatalException)e);
                error.set(new JavaFXLibraryNonFatalException("Unable to list target windows", e));
            }
        });
        if(error.get() != null) {
            throw error.get();
        }
        return targetWindows.get();
    }

    @RobotKeyword("Returns window object.\n\n"
            + "``locator`` is either a _query_ or _Object:Node, Scene_ for identifying the Window. In addition to normal _query_, "
            + "locator can be a search string for _pattern=_, _title=_ or Integer number. See `3. Locating or specifying UI elements`. \n\n"
            + "\nExamples for different kind of locators: \n\n"
            + "Pattern (defaults to title):\n"
            + "| ${window}= | Get Window | My window title | \n"
            + "| ${window}= | Get Window | title=My window title | \n"
            + "| ${window}= | Get Window | pattern=W[i-w]{5} Title | \n\n"
            + "Index:\n"
            + "| ${window}= | Get Window | 0 | \n"
            + "| ${window}= | Get Window | ${2} | \n\n"
            + "Node:\n"
            + "| ${some_node}= | Find | \\#some_id | \n"
            + "| ${window}= | Get Window | ${some_node} | \n\n"
            + "Scene: \n"
            + "| ${some_scene}= | Get Nodes Scene | ${some_node} | \n"
            + "| ${window}= | Get Window | ${some_scene} | \n"
    )
    @ArgumentNames({"locator"})
    public Object getWindow(Object locator) {
        RobotLog.info("Getting window using locator \"" + locator + "\"");

        AtomicReference<Object> window = new AtomicReference<>();
        AtomicReference<JavaFXLibraryNonFatalException> error = new AtomicReference<>();

            if (locator instanceof String) {
                if ( ((String) locator).matches("[0-9]+")) {
                    return getWindow(Integer.parseInt(locator.toString()));
                } else {
                    String resolvedLocator = resolveWindowLocatorString((String) locator);
                    robot.interact(() -> {
                        window.set(HelperFunctions.mapObject(robot.window(resolvedLocator)));
                    });
                }
            } else {
                robot.interact(() -> {
                    try {
                        Method method = MethodUtils.getMatchingAccessibleMethod(robot.getClass(), "window", locator.getClass());
                        window.set(HelperFunctions.mapObject(method.invoke(robot, locator)));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        error.set(new JavaFXLibraryNonFatalException("Could not execute get window using locator \"" + locator + "\""));
                    } catch (Exception e) {
                        if (e instanceof JavaFXLibraryNonFatalException)
                            error.set((JavaFXLibraryNonFatalException)e);
                        error.set(new JavaFXLibraryNonFatalException("Unable to find window: \"" + locator.toString() + "\"", e));
                    }
                });
            }

        if(error.get() != null) {
            throw error.get();
        }

        return window.get();
    }

    /**
     * This method expects that String is not actually an integer. Replaces `pattern=`
     * or `title=` with an empty string.
     */
    private String resolveWindowLocatorString(String locator) {
        if (locator.startsWith("pattern=")) {
            return locator.replace("pattern=", "");
        } else if(locator.startsWith("title=")) {
            return locator.replace("title=", "");
        } else {
            return locator;
        }
    }
}