package com.nemojin.sosikbot.util;

import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Base64;

public class ImageHelper {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");

    /// [Util] Download Event Image From Notice Url
    public static boolean downloadImageFromUrl(File imgDir, String imgUrl, String title, LocalDate date) throws Exception {
        String fileName = title + "[" + date + "]"+ ".png";
        File outputFile = new File(imgDir, fileName);

        if (outputFile.exists()) {
            // throw new BusinessException(BotException.IMAGE_ALREADY_EXISTS);
            return true;
        }

        if (imgUrl != null && imgUrl.startsWith("http")) {
            // Case.1 : Download images starting with http
            return downloadURLBaseImage(imgUrl, outputFile);

        } else if (imgUrl != null && imgUrl.startsWith("data:image")) {
            // Case.2 : Download images starting with Base64
            return downloadBase64Image(imgUrl, outputFile);

        } else {
            throw new BusinessException(BotException.IMAGE_FORMAT_NOT_SUPPORTED);
        }
    }

    /// [Util] Download a URL-Base image string (http:/~) to a file.
    private static boolean downloadURLBaseImage(String imageUrl, File outputFile) throws Exception {
        HttpURLConnection connection = null;

        try {
            // Create connection to the image URL
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Read image data from the input stream
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new BusinessException(BotException.IMAGE_CONNECTION_FAIL);
            }

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[2048];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                logger.info("Http Image saved : " + outputFile.getAbsolutePath());
            }

            return true;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /// [Util] Download a Base64-encoded image string (data:image/~) to a file.
    private static boolean downloadBase64Image(String base64Data, File outputFile) throws Exception {
        String[] parts = base64Data.split(",");

        if (parts.length != 2 || !parts[0].startsWith("data:image")) {
            throw new BusinessException(BotException.IMAGE_FORMAT_NOT_SUPPORTED);
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BotException.IMAGE_DOWNLOAD_FAIL);
        }

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(imageBytes);
            logger.info("Base64 image saved : " + outputFile.getAbsolutePath());
        }

        return true;
    }
}
