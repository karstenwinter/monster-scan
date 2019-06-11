package poke;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;

class OCR {
    File dir;
    StringBuffer sb;
    Tesseract t;
    TreeMap<String, String> names;

    public void loadList() throws Exception {
        System.out.print("Loading...");
        Pokemon p = new Pokemon();
        sb = p.downloadList();
        System.out.println(sb.length() + " bytes.");
        // System.out.println(sb);
        System.out.print("Building index...");
        TreeMap<String, String> names2 = p.buildMap(sb);
        names = new TreeMap<>();
        for (Map.Entry<String, String> e : names2.entrySet()) {
            names.put(clean(e.getKey()), e.getValue());
        }
        System.out.println(names.size() + " entries.");


        t = new Tesseract();
        t.setLanguage("deu");

        sb.delete(0, sb.length());
    }

    public void loadOCR() throws Exception {
        t = new Tesseract();
        t.setLanguage("deu");
    }

    String clean(String textContent) {
        return textContent
                .toLowerCase()
                .replaceAll("f", "t")
                .replaceAll("/", "t")
                .replaceAll("w", "v")
                .replaceAll("c", "e")
                .replaceAll("l", "t")
                .replaceAll("i", "t")
                .replaceAll("j", "t");
    }

    Map.Entry<Boolean, String> process(BufferedImage img, String name, boolean withTres, StringBuffer sb) throws Exception {

        // BufferedImage img = ImageIO.read(imgP.toFile());
        if (withTres) {
            img = treshold(img, name);
        }

        String res = "";
        try {
            res = clean(t.doOCR(img));
        } catch (Throwable e) {
            System.err.println("! " + name + "=>" + e.getMessage());
        }
        sb.delete(0, sb.length());
        sb.append(res);
        boolean found = false;
        for (Map.Entry<String, String> entry : names.entrySet()) {
            if (res.contains(entry.getKey())) {
                String link = "https://www.cardmarket.com/de/Pokemon/Products/Search?mode=gallery&searchString=" + entry.getValue();
                System.out.println((withTres ? "T " : "C ") + name + "=>" + link);
                return new AbstractMap.SimpleImmutableEntry<Boolean, String>(withTres, link);
                // found = true;
            }
        }
        // return found;
        return null;
    }

    BufferedImage treshold(BufferedImage image, String name) throws Exception {
        int arrayWidth = image.getWidth(null);
        int arrayHeight = image.getHeight(null);
        int[] pixels = new int[arrayHeight * arrayWidth];
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, arrayWidth, arrayHeight, pixels, 0, arrayWidth);

        grabber.grabPixels();

        int s = 10;
        for (int j = 0; j < pixels.length; j++) {
            int pixel = pixels[j];
            pixel = Math.abs(pixel) > s * 1000000 ? 0xFF000000 : 0xFFFFFFFF;
            pixels[j] = pixel;
        }

        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        // ImageIO.write(image, "PNG", Paths.get(name + "out.png").toFile());
        return image;
    }
}
