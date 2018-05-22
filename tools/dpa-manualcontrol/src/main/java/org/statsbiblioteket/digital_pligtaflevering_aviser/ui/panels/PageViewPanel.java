package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import org.apache.commons.codec.CharEncoding;
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
import java.util.ArrayList;
import java.util.List;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * panel for viewing pdf-pages
 */
public class PageViewPanel extends HorizontalLayout {

    protected Logger log = LoggerFactory.getLogger(getClass());
    private List<Embedded> pdfComponents;

    public PageViewPanel() {

    }

    public void initiate(String... urls) {
        if (pdfComponents == null) {
            pdfComponents = new ArrayList<Embedded>();
        }
        while (pdfComponents.size() < urls.length) {
            Embedded embeddedComponent = new Embedded(null, null);
            embeddedComponent.setMimeType("application/pdf");
            embeddedComponent.setType(Embedded.TYPE_BROWSER);
            embeddedComponent.setWidth(100, Unit.PERCENTAGE);
            embeddedComponent.setHeight(100, Unit.PERCENTAGE);
            pdfComponents.add(embeddedComponent);
            addComponent(embeddedComponent);
        }

        int count = 0;
        for (String url : urls) {
            pdfComponents.get(count).setSource(createStreamResource(url));
            pdfComponents.get(count).setWidth(100, Unit.PERCENTAGE);
            pdfComponents.get(count).setVisible(true);
            count++;
        }
        if(pdfComponents.size() > urls.length) {
            for(int i=urls.length;i <pdfComponents.size(); i++) {
                pdfComponents.get(i).setVisible(false);
            }
        }
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
