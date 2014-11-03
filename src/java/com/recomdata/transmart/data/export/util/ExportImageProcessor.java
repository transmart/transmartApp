


/**
 *
 */
package com.recomdata.transmart.data.export.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * @author SMunikuntla
 */
public class ExportImageProcessor {

    private static final ThreadGroup imagesThreadGroup = new ThreadGroup("Images");

    private String getFilename(URI imageURI) {
        String filename = null;
        if (StringUtils.equalsIgnoreCase("file", imageURI.getScheme())) {
            filename = (new File(imageURI.toString())).getName();
        } else {
            if (null != imageURI) {
                String imageURIStr = imageURI.toString();
                if (StringUtils.isNotEmpty(imageURIStr)) {
                    int loc = imageURIStr.lastIndexOf("/");
                    if (loc == imageURIStr.length() - 1) {
                        loc = (imageURIStr.substring(0, loc - 1)).lastIndexOf("/");
                    }
                    filename = imageURIStr.substring(loc + 1, imageURIStr.length());
                }
            }
        }

        return filename;
    }

    /**
     * @param imageURLStr
     * @param filename
     * @return
     * @throws URISyntaxException
     */
    public void getImageFromURI(String imageURIStr, String filename) throws URISyntaxException {
        if (StringUtils.isEmpty(filename)) {
            filename = getFilename(new URI(imageURIStr));
        }
        new Thread(imagesThreadGroup, new ExportImageThread(imageURIStr, filename)).start();
    }

    public void getImages(List<String> imageURIs) {
        for (String imageURI : imageURIs) {
            try {
                getImageFromURI(imageURI, null);
            } catch (URISyntaxException e) {
                System.out.println("Invalid URI for image :: " + imageURI);
                e.printStackTrace();
            }
        }
    }
}

class ExportImageThread extends Thread {
    private String imageURI;
    private String filename;

    /**
     * change to configurable from property file
     */
    private static final String imagesTempDir = "C://images";

    public ExportImageThread(String imageURI, String filename) {
        this.imageURI = imageURI;
        this.filename = filename;
    }

    public void run() {
        URL imageURL = null;
        File imageFile = new File(imagesTempDir + File.separator + filename);
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            if (StringUtils.isEmpty(imageURI))
                return;

            imageURL = new URL(imageURI);
            rbc = Channels.newChannel(imageURL.openStream());
            fos = new FileOutputStream(imageFile);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
