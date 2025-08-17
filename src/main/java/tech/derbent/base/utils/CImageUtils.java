package tech.derbent.base.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for image processing operations including resizing, format conversion, and validation. Follows coding
 * guidelines by using proper logging, validation, and exception handling.
 */
public final class CImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CImageUtils.class);

    /** Standard profile picture width in pixels */
    public static final int PROFILE_PICTURE_WIDTH = 100;

    /** Standard profile picture height in pixels */
    public static final int PROFILE_PICTURE_HEIGHT = 100;

    /** Maximum allowed image file size in bytes (5MB) */
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    /** Supported image formats */
    public static final String[] SUPPORTED_FORMATS = { "jpg", "jpeg", "png", "gif" };

    /**
     * Creates a data URL from image bytes for display in HTML components.
     * 
     * @param imageData
     *            Image data as byte array
     * @return Data URL string for use in img src attribute
     */
    public static String createDataUrl(final byte[] imageData) {
        // LOGGER.debug("CImageUtils.createDataUrl called with {} bytes", imageData !=
        // null ? imageData.length : 0);

        if ((imageData == null) || (imageData.length == 0)) {
            LOGGER.debug("Image data is null or empty, returning null");
            return null;
        }
        final String mimeType = detectImageMimeType(imageData);
        // LOGGER.debug("Detected MIME type: {}", mimeType);
        // Convert byte array to base64
        final String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
        final String dataUrl = "data:" + mimeType + ";base64," + base64Image;
        // LOGGER.debug("Created data URL with length: {}", dataUrl.length());
        return dataUrl;
    }

    /**
     * Detects the MIME type of image data based on content headers.
     * 
     * @param imageData
     *            Image data as byte array
     * @return MIME type string (e.g., "image/jpeg", "image/png", "image/svg+xml")
     */
    private static String detectImageMimeType(final byte[] imageData) {

        if ((imageData == null) || (imageData.length < 4)) {
            return "image/jpeg"; // Default fallback
        }
        // Check for SVG (starts with "<svg" or "<?xml")
        final String start = new String(imageData, 0, Math.min(20, imageData.length)).toLowerCase();

        if (start.contains("<svg") || start.contains("<?xml")) {
            return "image/svg+xml";
        }

        // Check for PNG signature (89 50 4E 47)
        if ((imageData.length >= 4) && ((imageData[0] & 0xFF) == 0x89) && ((imageData[1] & 0xFF) == 0x50)
                && ((imageData[2] & 0xFF) == 0x4E) && ((imageData[3] & 0xFF) == 0x47)) {
            return "image/png";
        }

        // Check for JPEG signature (FF D8)
        if ((imageData.length >= 2) && ((imageData[0] & 0xFF) == 0xFF) && ((imageData[1] & 0xFF) == 0xD8)) {
            return "image/jpeg";
        }

        // Check for GIF signature (GIF87a or GIF89a)
        if (imageData.length >= 6) {
            final String gifStart = new String(imageData, 0, 6);

            if (gifStart.startsWith("GIF87a") || gifStart.startsWith("GIF89a")) {
                return "image/gif";
            }
        }

        // Check for WebP signature (RIFF...WEBP)
        if (imageData.length >= 12) {
            final String riffStart = new String(imageData, 0, 4);
            final String webpCheck = new String(imageData, 8, 4);

            if ("RIFF".equals(riffStart) && "WEBP".equals(webpCheck)) {
                return "image/webp";
            }
        }
        // Default fallback to JPEG
        return "image/jpeg";
    }

    /**
     * Gets the default profile picture data URL.
     * 
     * @return Data URL for default user icon
     */
    public static String getDefaultProfilePictureDataUrl() {
        // SVG data for default user icon - same as used in CUserProfileDialog
        return "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PGNpcmNsZSBjeD0iNTAiIGN5PSI1MCIgcj0iNTAiIGZpbGw9IiNmNWY1ZjUiLz48Y2lyY2xlIGN4PSI1MCIgY3k9IjM1IiByPSIxNSIgZmlsbD0iIzk5OTk5OSIvPjxwYXRoIGQ9Im0yNSA3NWMwLTE0IDExLTI1IDI1LTI1czI1IDExIDI1IDI1IiBmaWxsPSIjOTk5OTk5Ii8+PC9zdmc+";
    }

    /**
     * Extracts file extension from filename.
     * 
     * @param fileName
     *            File name to extract extension from
     * @return File extension in lowercase, or empty string if no extension found
     */
    private static String getFileExtension(final String fileName) {

        if ((fileName == null) || fileName.trim().isEmpty()) {
            return "";
        }
        final int lastDotIndex = fileName.lastIndexOf('.');

        if ((lastDotIndex > 0) && (lastDotIndex < (fileName.length() - 1))) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Resizes an image to the specified dimensions with high quality scaling.
     * 
     * @param imageData
     *            Original image data as byte array
     * @param targetWidth
     *            Target width in pixels
     * @param targetHeight
     *            Target height in pixels
     * @return Resized image data as byte array in JPEG format
     * @throws IOException
     *             if image processing fails
     */
    public static byte[] resizeImage(final byte[] imageData, final int targetWidth, final int targetHeight)
            throws IOException {
        LOGGER.info("CImageUtils.resizeImage called with targetWidth={}, targetHeight={}", targetWidth, targetHeight);

        if ((imageData == null) || (imageData.length == 0)) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }

        if ((targetWidth <= 0) || (targetHeight <= 0)) {
            throw new IllegalArgumentException("Target dimensions must be positive");
        }

        try {
            // Read original image
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            final BufferedImage originalImage = ImageIO.read(inputStream);

            if (originalImage == null) {
                throw new IOException("Unable to read image data - unsupported format");
            }
            LOGGER.debug("Original image dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());
            // Create resized image with high quality
            final BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics2D = resizedImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Scale and draw the image
            final Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            graphics2D.drawImage(scaledImage, 0, 0, null);
            graphics2D.dispose();
            // Convert to byte array in JPEG format
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);
            final byte[] resizedImageData = outputStream.toByteArray();
            LOGGER.info("Image resized successfully from {} bytes to {} bytes", imageData.length,
                    resizedImageData.length);
            return resizedImageData;
        } catch (final IOException e) {
            LOGGER.error("Failed to resize image", e);
            throw new IOException("Failed to resize image: " + e.getMessage(), e);
        }
    }

    /**
     * Resizes an image to standard profile picture dimensions.
     * 
     * @param imageData
     *            Original image data as byte array
     * @return Resized image data as byte array in JPEG format
     * @throws IOException
     *             if image processing fails
     */
    public static byte[] resizeToProfilePicture(final byte[] imageData) throws IOException {
        // LOGGER.info("CImageUtils.resizeToProfilePicture called");
        return resizeImage(imageData, PROFILE_PICTURE_WIDTH, PROFILE_PICTURE_HEIGHT);
    }

    /**
     * Validates image data for profile picture usage.
     * 
     * @param imageData
     *            Image data to validate
     * @param fileName
     *            Original file name for format validation
     * @throws IllegalArgumentException
     *             if validation fails
     */
    public static void validateImageData(final byte[] imageData, final String fileName) {
        LOGGER.info("CImageUtils.validateImageData called with fileName={}", fileName);

        if ((imageData == null) || (imageData.length == 0)) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }

        if (imageData.length > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Image size (%d bytes) exceeds maximum allowed size (%d bytes)", imageData.length, MAX_IMAGE_SIZE));
        }

        if ((fileName == null) || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        // Validate file format
        final String fileExtension = getFileExtension(fileName);
        boolean isSupported = false;

        for (final String format : SUPPORTED_FORMATS) {

            if (format.equalsIgnoreCase(fileExtension)) {
                isSupported = true;
                break;
            }
        }

        if (!isSupported) {
            throw new IllegalArgumentException(String.format("Unsupported image format: %s. Supported formats: %s",
                    fileExtension, String.join(", ", SUPPORTED_FORMATS)));
        }

        // Try to read the image to validate it's a valid image file
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            final BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new IllegalArgumentException("Invalid image data - cannot read image");
            }
        } catch (final IOException e) {
            throw new IllegalArgumentException("Invalid image data: " + e.getMessage(), e);
        }
        LOGGER.debug("Image validation successful for file: {}", fileName);
    }

    private CImageUtils() {
        // Utility class - prevent instantiation
    }
}