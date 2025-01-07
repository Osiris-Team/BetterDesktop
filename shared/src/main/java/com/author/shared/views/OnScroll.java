package com.author.shared.views;

import com.osiris.betterdesktop.MyFile;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.jlib.logger.AL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OnScroll {

    private static final Object lock = new Object();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static volatile ScheduledFuture<?> scheduledFuture = null;

    public static void registerForMyFilesContainer(Component<?, ?> container, boolean runAtStart) {
        register(container, comps -> {
            for (Component<?, ?> c : comps) {
                MyFile f = (MyFile) c;
                f.fetchIcon();
            }
        }, runAtStart);
    }

    /**
     * @param container
     * @param onScroll executed on scroll, provides currently visible components list.
     */
    public static void register(Component<?, ?> container, Consumer<List<Component<?,?>>> onScroll, boolean runAtStart) {
        Runnable code = () -> {
            // JavaScript code to find all visible child components after scroll
            String jsCode =
                "var children = comp.children; " + // Get all child elements
                    "for (var i = 0; i < children.length; i++) { " +
                    "    var child = children[i]; " +
                    "    var rect = child.getBoundingClientRect(); " + // Get the bounding rectangle of the child
                    "    if (rect.top >= 0 && rect.bottom <= window.innerHeight) { " + // Check if the child is within the viewport
                    "        message += child.getAttribute('java-id') + ' '; " + // Add the Java ID to the array
                    "    } " +
                    "} ";

            // Execute the JavaScript code
            container.executeJS(jsCode, msg -> {
                var visibleCompsIds = msg.split(" ");
                List<Component<?, ?>> visibleComponents = new ArrayList<>();

                for (String id : visibleCompsIds) {
                    for (Component<?,?> c : container.children) {
                        try{
                            if(c.id == Integer.parseInt(id)) visibleComponents.add(c);
                        } catch (Exception e) {
                        }

                    }
                }
                onScroll.accept(visibleComponents);
            }, AL::warn);
        };


        if(runAtStart)
            code.run();

        container.onScroll(e -> {

            synchronized (lock){
                // Cancel the previous scheduled task if it exists
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(false);
                }

                UI ui = UI.get();
                // Schedule a new task to run after 250ms
                scheduledFuture = scheduler.schedule(() -> {
                    ui.access(() -> {
                        try{
                            System.out.println("SCROLL");
                            code.run();
                            System.out.println("EXECUTED, PENDING APPENDS: "+ ui.pendingAppends.size());
                            scheduledFuture = null; // Reset the scheduledFuture after execution
                        } catch (Exception ex) {
                            AL.warn(ex);
                        }
                    });
                }, 250, TimeUnit.MILLISECONDS);
            }
        });
    }
}
