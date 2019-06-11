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

class Program {
    File dir;
    StringBuffer sb;
    Tesseract t;
    TreeMap<String, String> names;

    void run(String dirStr) throws Exception {
        final OCR ocr = new OCR();

        System.out.println("Reading files in " + dir + " ...");

        String[] list = dir.list();
        if (list == null) {
            System.err.println("Not a file");
            System.exit(-2);
        }

        for (final String name : list) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        StringBuffer sb2 = new StringBuffer();
                        Path imgP = Paths.get(dir.getAbsolutePath(), name);

                        BufferedImage img = ImageIO.read(imgP.toFile());
                        Map.Entry<Boolean, String> f = ocr.process(img, name, false, sb2);

                        if (f == null) {
                            f = ocr.process(img, name, true, sb2);

                            if (f != null) {
                                System.err.println("! " + name + "=>" + sb.toString().replaceAll("\n", " "));
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).run(); // start();
            // System.out.println(name + "=>" + res);
        }
        //treshold(path);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Run with 1 argument that is a folder of images.");
            System.exit(-1);
        }
        new Program().run(args[0]);
    }
}
