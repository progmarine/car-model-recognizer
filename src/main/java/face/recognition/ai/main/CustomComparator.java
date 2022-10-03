package face.recognition.ai.main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CustomComparator {

    public static void main(String[] args) throws IOException {
        getDiff();
    }

    public static void getDiff() throws IOException {

        BufferedImage imgA = null;
        BufferedImage imgB = null;

        // Try block to check for exception
        try {

            // Reading file from local directory by
            // creating object of File class
            File fileA = new File("C:\\Users\\user\\Desktop\\br.jpg");
            File fileB = new File("C:\\Users\\user\\Desktop\\br_1.jpg");

            // Reading files
            imgA = ImageIO.read(fileA);
            imgB = ImageIO.read(fileB);
        }

        // Catch block to check for exceptions
        catch (IOException e) {
            // Display the exceptions on console
            System.out.println(e.getMessage());
        }

        // Assigning dimensions to image
        assert imgA != null;
        int width1 = imgA.getWidth();
        assert imgB != null;
        int width2 = imgB.getWidth();
        int height1 = imgA.getHeight();
        int height2 = imgB.getHeight();

        // Checking whether the images are of same size or
        // not
        if ((width1 != width2) || (height1 != height2)) {
            // Display message straightaway
            System.out.println("Images dimensions" + " mismatch. Will try to adjust");
            //Adjust sizes
            adjust(imgA, imgB, width1, width2, height1, height2);
        }

        try {
            // By now, images are of same size

            long difference = 0;

            // treating images likely 2D matrix

            // Outer loop for rows(height)
            for (int y = 0; y < height1; y++) {

                // Inner loop for columns(width)
                for (int x = 0; x < width1; x++) {

                    int rgbA = imgA.getRGB(x, y);
                    int rgbB = imgB.getRGB(x, y);
                    int redA = (rgbA >> 16) & 0xff;
                    int greenA = (rgbA >> 8) & 0xff;
                    int blueA = (rgbA) & 0xff;
                    int redB = (rgbB >> 16) & 0xff;
                    int greenB = (rgbB >> 8) & 0xff;
                    int blueB = (rgbB) & 0xff;

                    difference += Math.abs(redA - redB);
                    difference += Math.abs(greenA - greenB);
                    difference += Math.abs(blueA - blueB);
                }
            }

            // Total number of red pixels = width * height
            // Total number of blue pixels = width * height
            // Total number of green pixels = width * height
            // So total number of pixels = width * height *
            // 3
            double total_pixels = width1 * height1 * 3;

            // Normalizing the value of different pixels
            // for accuracy

            // Note: Average pixels per color component
            double avg_different_pixels = difference / total_pixels;

            // There are 255 values of pixels in total
            int percentage = (int) Math.round((avg_different_pixels / 255) * 100);

            // Lastly print the difference percentage
            System.out.println("Difference Percentage-->" + percentage);
            return;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        throw new IOException("Something went wrong");
    }

    private static void adjust(
            BufferedImage imgA,
            BufferedImage imgB,
            int width1,
            int width2,
            int height1,
            int height2) {
        int width;
        int height;
        width = Math.min(width1, width2);
        height = Math.min(height1, height2);
        imgA = imgA.getSubimage(0, 0, width, height);
        imgB = imgB.getSubimage(0, 0, width, height);
    }

}
