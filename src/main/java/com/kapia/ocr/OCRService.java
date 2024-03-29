package com.kapia.ocr;

import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class OCRService {

    @Value("${tessdata.path}")
    private String TESSDATA_PATH;

    private static final Logger LOGGER = LoggerFactory.getLogger(OCRService.class);

    @PostConstruct
    public void init() {
        checkIfTesseractPathIsSet();
    }

    public String processImage(MultipartFile image) throws IOException, TesseractException {

        try {
            BufferedImage bufferedImage = convertToImage(image);
            return runTesseract(bufferedImage);
        } catch (IllegalArgumentException e) {
            LOGGER.error("File could not be read");
            throw new IOException("File could not be read");
        }

    }

    private BufferedImage convertToImage(MultipartFile multipartFile) throws IOException, IllegalArgumentException {

        if (multipartFile == null) {
            LOGGER.debug("File passed to convertToImage was null");
            throw new IllegalArgumentException("File cannot be null");
        }
        byte[] array = multipartFile.getBytes();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(array));
        if (bufferedImage == null) {
            LOGGER.debug("Conversion result was null");
            throw new IOException("Result of conversion was null");
        }
        return bufferedImage;

    }

    private String runTesseract(BufferedImage bufferedImage) throws TesseractException {

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        return tesseract.doOCR(bufferedImage);

    }

    private void checkIfTesseractPathIsSet() {
        if (TESSDATA_PATH == null || TESSDATA_PATH.isEmpty()) {
            LOGGER.error("Tessdata path is not set");
            throw new IllegalArgumentException("Tessdata path is not set");
        }
    }

}
