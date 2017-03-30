package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;


import com.sun.org.apache.xerces.internal.dom.DeferredTextImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by mmj on 3/9/17.
 */
public class XmlView extends VerticalLayout {


    private Tree tree;
    Label richText = new Label("Metadata");


    public XmlView() {
        richText.setHeight("500px");

        richText.addStyleName("wrap");

        /*try {
            richText.setValue(readFile("/tmp/testDeliverySerialize.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        this.addComponent(richText);
    }

    public void setValue(String value) throws Exception {
        richText.setValue(getText(value));
    }


    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }


    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    /*public XmlView() {

        Tree tree = new Tree();

        try {
            File fXmlFile = new File("/tmp/testDeliverySerialize.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            Element root = doc.getDocumentElement();
            Object rootItem = root.getNodeName();
            tree.addItem(rootItem);
            addChildrenToTree(tree, root.getChildNodes(), rootItem);
            this.addComponent(tree);
        } catch (Exception e) {


            e.printStackTrace();
        }
    }

    int count = 0;

    private void addChildrenToTree(Tree tree, NodeList children, Object parent) {
        if (children.getLength() > 0) {
            Item ii = null;
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                Object child = node.getNodeName();

                Item item = tree.addItem(child);

                tree.setParent(child, parent);




                addChildrenToTree(tree, node.getChildNodes(), child);


                count++;
            }


        } else {
            //tree.addItem("TST"+count);
        }
    }*/
}
