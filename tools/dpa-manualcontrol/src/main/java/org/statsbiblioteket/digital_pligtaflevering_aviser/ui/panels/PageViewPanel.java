package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * panel for viewing pdf-pages
 * The panel allocates space for 4X4 pages
 */
public class PageViewPanel extends GridLayout {

    protected Logger log = LoggerFactory.getLogger(getClass());

    public PageViewPanel() {
        super(4,4);
    }

    /**
     * Initiate pdf-viewers to be viewed in the UI
     * @param urls
     */
    public void initiate(String... urls) {

        //Cleanup all embedded pdf-components to make sure that the application can handle changes on how many pdf-files to view
        cleanupsEmbeddedPdfComponents();

        int componentCreateIndex = 0;
        for (String url : urls) {
            //No caption for the pdf-view is wanted
            BrowserFrame embeddedComponent = new BrowserFrame(null, createStreamResource(urls[componentCreateIndex]));
            embeddedComponent.setWidth(100, Unit.PERCENTAGE);
            embeddedComponent.setHeight(100, Unit.PERCENTAGE);
            //pdfComponents.add(embeddedComponent);
            if(urls.length == 1) {
                addComponent(embeddedComponent,0,0,3,3);
            } else if(urls.length <= 4) {
                int col = (componentCreateIndex%2)*2;
                int row = (componentCreateIndex/2)*2;
                addComponent(embeddedComponent, col, row,col+1,row+1);
            } else if(urls.length <= 16) {
                addComponent(embeddedComponent,componentCreateIndex%4,componentCreateIndex/4, componentCreateIndex%4, componentCreateIndex/4);
            } else {
                //It is decided that it is very unlikely to have more then 16 frontpages
                Notification.show("The application can not show so many frontpages", Notification.Type.WARNING_MESSAGE);
            }

            componentCreateIndex++;
        }
    }

    /**
     * Remove all pdf-viewers an cleanup after them
     */
    private void cleanupsEmbeddedPdfComponents() {
        //Make sure all streams in pdf-viewer is closed before pdf-viewers is removed
        //Not actually sure if this is necessary but better safe then sorry
        Iterator<Component> componentIterator = super.iterator();
        while(componentIterator.hasNext()) {
            BrowserFrame pdfViewComponent = (BrowserFrame)componentIterator.next();
            IOUtils.closeQuietly(((StreamResource)pdfViewComponent.getSource()).getStreamSource().getStream());
        }
        //Removing all the pdf-components in the UI
        super.removeAllComponents();
    }


    /**
     * Convert the urlString to the pdfComponent-file into a StreamResource for viewing in UI
     *
     * @param urlString
     * @return
     * @throws Exception
     */
    private synchronized StreamResource createStreamResource(final String urlString) {

        final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {

                String bitrepositoryMountpoint = NewspaperContextListener.configurationmap.getRequired(BITREPOSITORY_SBPILLAR_MOUNTPOINT);
                String bitrepositoryUrlPrefix = NewspaperContextListener.configurationmap.getRequired(BITMAG_BASEURL_PROPERTY);

                if (urlString.startsWith(bitrepositoryUrlPrefix) == false) {
                    try {
                        URL url = new URL(urlString);
                        InputStream inps = url.openStream();
                        return inps;
                    } catch (IOException e) {
                        Notification.show("The application can not read the pdf-file", Notification.Type.WARNING_MESSAGE);
                        log.error("The stream could not get opened " + urlString, e);
                    }
                } else {
                    if (urlString.length() < bitrepositoryUrlPrefix.length()) {
                        Notification.show("The application can not create a link to the url, please contact support", Notification.Type.ERROR_MESSAGE);
                    }
                    String resourceName = urlString.substring(bitrepositoryUrlPrefix.length());
                    final File file;
                    try {
                        Path path = Paths.get(bitrepositoryMountpoint, URLDecoder.decode(resourceName, CharEncoding.UTF_8));
                        file = path.toFile();
                        InputStream inputStream = new FileInputStream(file);
                        return inputStream;
                    } catch (UnsupportedEncodingException e) {
                        Notification.show("The application can not read the pdf-file (Url encoding)", Notification.Type.WARNING_MESSAGE);
                        log.error("The stream could not get opened " + urlString, e);
                    } catch (FileNotFoundException e) {
                        Notification.show("The application can not read the pdf-file (FileNotFound)", Notification.Type.WARNING_MESSAGE);
                        log.error("The stream could not get opened " + urlString, e);
                    }
                }

                return null;

            }
        }, "pages.pdf"); // Short pagename is needed
        resource.setMIMEType("application/pdf");
        resource.setCacheTime(1000);
        return resource;
    }
}
