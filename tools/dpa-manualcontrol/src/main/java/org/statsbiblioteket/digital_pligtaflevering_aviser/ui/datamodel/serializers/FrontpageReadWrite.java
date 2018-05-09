package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.Image.SCALE_FAST;
//TODO:MMJ CHECK THIS

/**
 * Utilities for reading ingested frontpages and writing them as
 */
public class FrontpageReadWrite {

    public static String mountpoint = null;

    public static void writeTitlesOfFirst(String date) throws Exception {

        String stuff = mountpoint + "/dl_" + date + "01_rt1";

        FileFilter filterRegex = new WildcardFileFilter("*_section01_*#0001.pdf");

        ArrayList<Path> arl = new ArrayList<Path>();

        List<File> files = Arrays.asList(new File(stuff).listFiles());
        String thumbnailsPath = "/tmp/dpaviser/"+date+"/thumbnails";
        File tp = new File(thumbnailsPath);
        if(!tp.exists()) {
            tp.mkdir();
        }
        for(File f : files) {
            new File(thumbnailsPath + "/"+f.getName()).mkdir();
        }

    }

    public static List<Path> folderFinder(String filterRegex) throws Exception {

        FileFilter filter = new RegexFileFilter(filterRegex);
        ArrayList<Path> arl = new ArrayList<Path>();

        try (Stream<Path> paths = Files.walk(new File(mountpoint).toPath())) {
            paths.filter(f -> filter.accept(f.toFile())).forEach(f -> {
                arl.add(f);
            });
        }
        return arl;
    }

    /**
     * Find a list of frontpages which is contained in the deliverd folder
     * @param batchPath
     * @return
     * @throws Exception
     */
    public static File findFrontpages(Path batchPath) throws Exception {
        File dir = new File(batchPath.toString());
        FileFilter fileFilter = new WildcardFileFilter("*_section01_*#0001.pdf");
        File[] files = dir.listFiles(fileFilter);
        if(files!=null && files.length>0) {
            return files[0];//There is only expected to be one file
        } else {
            return null;
        }
    }

    /**
     * Write the dlivered list of *.pdf files as scaled images (thumbnails) to a folder
     * @param files
     * @param dstFolder
     * @throws Exception
     */
    public static void writeImages(List<File> files, String dstFolder) throws Exception {

        for(File f : files) {
            if(f!=null) {
                File outputfile = new File(dstFolder + File.separator + f.getName().replaceAll("#", "_") + ".png");
                PDDocument document = PDDocument.load(f);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage fullImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                BufferedImage downscaleImage = scale(fullImage, 0.02, SCALE_FAST);
                document.close();
                ImageIO.write(downscaleImage, "png", outputfile);
            }
        }
    }

    /**
     * Create a scaled version of the image
     * @param before
     * @param scale
     * @param type
     * @return
     */
    @NotNull
    private static BufferedImage scale(final BufferedImage before, final double scale, final int type) {
        int w = before.getWidth();
        int h = before.getHeight();
        int w2 = (int) (w * scale);
        int h2 = (int) (h * scale);
        BufferedImage after = new BufferedImage(w2, h2, before.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, type);
        scaleOp.filter(before, after);
        return after;
    }
}
