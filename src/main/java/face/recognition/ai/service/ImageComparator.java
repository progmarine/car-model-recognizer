package face.recognition.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class ImageComparator {

    private static final Logger logger = LoggerFactory.getLogger(ImageComparator.class);

    public String getDiff(MultipartFile multipartFile1, MultipartFile multipartFile2) throws IOException {

        BufferedImage imgA = null;
        BufferedImage imgB = null;

        // Try block to check for exception
        try {
            // Reading files
            imgA = ImageIO.read(multipartFile1.getInputStream());
            imgB = ImageIO.read(multipartFile2.getInputStream());
        }

        // Catch block to check for exceptions
        catch (IOException e) {
            // Display the exceptions on server
            logger.error(e.getMessage());
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
            logger.info("Images dimensions" + " mismatch. Will try to adjust");
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
            return ("Difference Percentage-->" + percentage);
        } catch (Exception e) {
            logger.info(e.getMessage());
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
