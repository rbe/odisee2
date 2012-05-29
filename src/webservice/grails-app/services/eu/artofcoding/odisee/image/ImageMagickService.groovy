/*
 * Odisee(R)
 * Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
 * All rights reserved. Use is subject to license terms.
 */
package eu.artofcoding.odisee.image

import eu.artofcoding.flux.grails.helper.FileHelper

/**
 * Service for editing images with ImageMagick.
 * @author rbe
 */
class ImageMagickService {

    /**
     * Installation directory of ImageMagick.
     */
    File imageMagickHome

    /**
     * Check if two file have the same format/extension or not.
     * @param file1 Left/first file for check.
     * @param file2 Right/second file for check.
     * @return boolean
     */
    public boolean isDifferentFileFormat(File file1, File file2) {
        // Decompose both filenames
        Map<String, String> decomp1 = FileHelper.decomposeFilename(file1)
        Map<String, String> decomp2 = FileHelper.decomposeFilename(file2)
        // Extensions different?
        boolean differentFormat = decomp1.ext.toLowerCase() != decomp2.ext.toLowerCase()
        if (differentFormat) {
            // Check special cases
            def cc = { decomp, list ->
                if (decomp.ext.toLowerCase() in list)
                    differentFormat = false
            }
            switch (decomp1.ext.toLowerCase()) {
            // JPEG
                case { it in ['jpg', 'jpeg']}:
                    cc decomp2, ['jpg', 'jpeg']
                    break
            // TIFF
                case { it in ['tif', 'tiff']}:
                    cc decomp2, ['tif', 'tiff']
                    break
            }
        }
        differentFormat
    }

    /**
     * Convert an image into another.
     * @param image java.io.File, object of image to resize.
     * @param converted java.io.File, File object to save converted file to.
     * @return java.io.File The converted image. ATTENTION: Use this File reference, as the filename may have changed.
     */
    public File convert(File image, File converted) {
        if (!image || !converted) {
            throw new IllegalStateException('No input or output file')
        }
        // Where to save?
        File _converted = converted
        use(ImageMagickCategory) {
            // Convert
            Process process = image.init(imageMagickHome).convert(converted)
            // Error while resizing
            if (process.exitValue() != 0) {
                // No converted file
                _converted = null
                // Show Process' error and input stream
                ['errorStream', 'inputStream'].each { log.info "${it}: ${new String(process[it].bytes)}" }
            }
        }
        // Return converted file object
        return _converted
    }

    /**
     * Convert an image to a certain size.
     * @param image java.io.File, object of image to resize.
     * @param height String, new height (optional, can be null or empty string).
     * @param width String, new width (optional, can be null or empty string).
     * @param converted java.io.File, File object to save converted file to.
     * @return java.io.File The converted image. ATTENTION: Use this File reference, as the filename may have changed.
     */
    public File resize(File image, String width, String height, File converted = null) {
        // Generate value for convert's -resize option
        String resizeOption = "${width}x${height}"
        // Decomponse original and converted filename
        Map decomposedImageFilename = FileHelper.decomposeFilename(image)
        Map decomposedConvertedFilename = converted ? FileHelper.decomposeFilename(converted) : null
        // Where to save?
        File resized = null
        if (converted) {
            resized = new File(image.parentFile, "${decomposedConvertedFilename.name}_${resizeOption}.${decomposedConvertedFilename.ext}")
        } else {
            resized = new File(image.parentFile, "${decomposedImageFilename.name}_${resizeOption}.${decomposedImageFilename.ext}")
        }
        // If resized files does not exist, create it
        // otherwise return File object of existing file
        if (!resized.exists()) {
            log.info "ImageMagickService.resize: Resizing image ${image} to ${resized} with width=${width} and height=${height}"
            use(ImageMagickCategory) {
                // Convert
                Process process =
                    image.init(imageMagickHome)
                            .option('-resize', resizeOption.toString())
                            .convert(resized)
                // Error while resizing
                if (process.exitValue() != 0) {
                    // No resized file
                    resized = null
                    // Show Process' error and input stream
                    ['errorStream', 'inputStream'].each { log.info "ImageMagickService.resize: ${it}: ${new String(process[it].bytes)}" }
                }
            }
        } else {
            log.info "ImageMagickService.resize: Using existing file ${resized}"
        }
        return resized
    }

    /**
     * Convert an image to a thumbnail size.
     * @param image java.io.File, object of image to resize.
     * @param height String, new height (optional, can be null or empty string).
     * @param width String, new width (optional, can be null or empty string).
     * @param converted java.io.File, File object to save converted file to.
     * @return java.io.File The converted image. ATTENTION: Use this File reference, as the filename may have changed.
     */
    public File thumbnail(File image, String width, String height, File converted = null) {
        // Generate value for convert's -resize option
        String resizeOption = "${width}x${height}"
        // Decomponse original and converted filename
        Map decomposedImageFilename = FileHelper.decomposeFilename(image)
        Map decomposedConvertedFilename = converted ? FileHelper.decomposeFilename(converted) : null
        // Where to save?
        File resized = null
        if (converted) {
            resized = new File(image.parentFile, "${decomposedConvertedFilename.name}_${resizeOption}.${decomposedConvertedFilename.ext}")
        } else {
            resized = new File(image.parentFile, "${decomposedImageFilename.name}_${resizeOption}.${decomposedImageFilename.ext}")
        }
        // If resized files does not exist, create it
        // otherwise return File object of existing file
        if (!resized.exists()) {
            log.info "ImageMagickService.thumbnail: Resizing image ${image} to ${resized.absolutePath} with width=${width} and height=${height}"
            use(ImageMagickCategory) {
                // Convert to PNG (used as intermediate working format)?
                File pngFile = null
                if (decomposedImageFilename.ext != 'png') {
                    pngFile = new File(image.parentFile, "${decomposedImageFilename.name}.png")
                    image.init(imageMagickHome).convert(pngFile)
                } else {
                    pngFile = image
                }
                // Create thumbnail
                Process process = null
                try {
                    process = pngFile.init(imageMagickHome)
                            .option('-thumbnail', "${resizeOption}^")
                            .option('-unsharp', '0x.05')
                            .option('-gravity', 'center')
                            .option('-extent', resizeOption.toString())
                            .convert(resized)
                    // Add rounded corners
                    if (decomposedConvertedFilename.ext == 'gif') {
                        process = resized.init(imageMagickHome)
                                .option('\\(')
                                .option('+clone')
                                .option('-alpha', 'extract')
                                .option('-draw', "'fill black polygon 0,0 0,15 15,0 fill white circle 15,15 15,0'")
                                .option('\\(')
                                .option('+clone', '-flip')
                                .option('\\)')
                                .option('-compose', 'Multiply')
                                .option('-composite')
                                .option('\\(')
                                .option('+clone', '-flop')
                                .option('\\)')
                                .option('-compose', 'Multiply')
                                .option('-composite')
                                .option('\\)')
                                .option('-alpha', 'off')
                                .option('-compose', 'CopyOpacity')
                                .option('-composite')
                                .convert(resized)
                    }
                } catch (e) {
                    log.error e
                } finally {
                    // Error while resizing
                    if (process?.exitValue() != 0) {
                        // No resized file
                        resized = null
                        // Show Process' error and input stream
                        ['errorStream', 'inputStream'].each { log.info "ImageMagickService.thumbnail: ${it}: ${new String(process[it].bytes)}" }
                    }
                }
            }
        } else {
            log.info "ImageMagickService.thumbnail: Using existing file ${resized}"
        }
        return resized
    }

    /**
     *
     * @return {@link java.io.File} Maybe different File object than given through parameter 'converted', as target file format must support transparency.
     */
    public File roundedCorners(File image, File converted = null) {
        // Decomponse original and converted filename
        Map<String, String> decomposedImageFilename = FileHelper.decomposeFilename(image)
        Map<String, String> decomposedConvertedFilename = converted ? FileHelper.decomposeFilename(converted) : null
        // Where to save?
        File roundedCorner = null
        if (converted) {
            roundedCorner = new File(image.parentFile, "${decomposedConvertedFilename.name}_${resizeOption}.${decomposedConvertedFilename.ext}")
        } else {
            roundedCorner = new File(image.parentFile, "${decomposedImageFilename.name}_${resizeOption}.${decomposedImageFilename.ext}")
        }
        // If resized files does not exist, create it
        // otherwise return File object of existing file
        if (!roundedCorner.exists()) {
            Process process = null
            try {
                use(ImageMagickCategory) {
                    // Does the target file format support transparency?
                    // If not, set file format of target to GIF
                    if (!roundedCorner.supportsTransparency()) {
                        Map<String, String> decomp = FileHelper.decomposeFilename(roundedCorner)
                        roundedCorner = new File(roundedCorner.parentFile, "${decomp.name}.gif")
                    }
                    // Add rounded corners
                    process = roundedCorner.init(imageMagickHome)
                            .option('\\(')
                            .option('+clone')
                            .option('-alpha', 'extract')
                            .option('-draw', "'fill black polygon 0,0 0,15 15,0 fill white circle 15,15 15,0'")
                            .option('\\(')
                            .option('+clone', '-flip')
                            .option('\\)')
                            .option('-compose', 'Multiply')
                            .option('-composite')
                            .option('\\(')
                            .option('+clone', '-flop')
                            .option('\\)')
                            .option('-compose', 'Multiply')
                            .option('-composite')
                            .option('\\)')
                            .option('-alpha', 'off')
                            .option('-compose', 'CopyOpacity')
                            .option('-composite')
                            .convert(roundedCorner)
                }
            } catch (e) {
                log.error e
            } finally {
                // Error while resizing
                if (process?.exitValue() != 0) {
                    // No resized file
                    roundedCorner = null
                    // Show Process' error and input stream
                    ['errorStream', 'inputStream'].each { log.info "ImageMagickService.roundedCorners: ${it}: ${new String(process[it].bytes)}" }
                }
            }
        }
        // Return image
        return roundedCorner
    }

    /**
     *
     * @param image
     * @return
     */
    public Map identify(File image) {
        use(ImageMagickCategory) {
            image.init(configService.fflConfig.imageMagickHome).option('-verbose').identify()
        }
    }

}
