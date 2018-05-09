package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
//TODO:MMJ CHECK THIS

/**
 * DatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class FrontpagePanel extends GridLayout {
    private Logger log = LoggerFactory.getLogger(getClass());

    public FrontpagePanel() {
        super(7,7);
    }

    /**
     * Convert the urlString to the pdfComponent-file into a StreamResource for viewing in UI
     * @param urlString
     * @return
     * @throws Exception
     */
    private synchronized StreamResource createStreamResource(final String urlString) {

        final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                InputStream inps = null;
                    try {
                        URL url = new URL(urlString);
                        inps = url.openStream();
                    } catch (IOException e) {
                        Notification.show("The application can not read the pdf-file", Notification.Type.WARNING_MESSAGE);
                        log.error("The stream could not get opened " + urlString, e);
                    }
                return inps;

            }
        }, "pages.png"); // Short pagename is needed
        resource.setMIMEType("application/png");
        resource.setCacheTime(1000);
        return resource;
    }

    /**
     * Set a list of DeliveryTitleInfo and deploy values into the relevant days in the currently viewed month
     *

     */
    public void setInfo(String monthPath) {
        this.removeAllComponents();
        String[] list = new File(monthPath).list();
        int column = 0;
        int row = 0;
        for(String filename : list) {
            try {
                Image image = new Image();
                image.setSource(createStreamResource("file:" + monthPath + "/" + filename));
                this.addComponent(image, column, row, column, row);
                column++;
                if(column>6) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                log.error("Could not write file:" + monthPath + "/" + filename, e);
            }
        }
    }

    /**
     * Set the component to be vieved as enabled in the UI
     *
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
