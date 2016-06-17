/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.v4.util.Pair;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

/**
 * Provides a mechanism for specifying images from a variety of sources. ImageSource retains the image source and its
 * associated type on behalf of the caller, making this information available to World Wind components that load images
 * on the caller's behalf.
 * <p/>
 * ImageSource supports four source types: <ul> <li>World Wind {@link ImageFactory}</li> <li>Android {@link
 * android.graphics.Bitmap}</li> <li>Android resource identifier</li> <li>File path</li> <li>Uniform Resource Locator
 * (URL)</li> </ul>
 * <p/>
 * ImageSource instances are intended to be used as a key into a cache or other data structure that enables sharing of
 * loaded images. World Wind image factories are compared by reference: two image sources are equivalent if they
 * reference both the same image factory and the same factory source. Android bitmaps are compared by reference as well:
 * two image sources are equivalent if they reference the same bitmap. Android resource identifiers with equivalent IDs
 * are considered equivalent, as are file paths and URLs with the same string representation.
 */
public class ImageSource {

    protected static final int TYPE_IMAGE_FACTORY = 1;

    protected static final int TYPE_BITMAP = 2;

    protected static final int TYPE_RESOURCE = 3;

    protected static final int TYPE_FILE_PATH = 4;

    protected static final int TYPE_URL = 5;

    protected static final int TYPE_UNRECOGNIZED = 6;

    protected int type;

    protected Object source;

    protected ImageSource() {
    }

    /**
     * Constructs an image source with an image factory. The factory must create images with dimensions to no greater
     * than 2048 x 2048.
     *
     * @param factory       the image factory to use as an image source
     * @param factorySource an optional argument to use as the <code>imageSource</code> argument of {@link
     *                      ImageFactory#createBitmap(Object)}.
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the factory is null
     */
    public static ImageSource fromImageFactory(ImageFactory factory, Object factorySource) {
        if (factory == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromImageFactory", "missingFactory"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_IMAGE_FACTORY;
        imageSource.source = Pair.create(factory, factorySource);
        return imageSource;
    }

    /**
     * Constructs an image source with a bitmap. The bitmap's dimensions should be no greater than 2048 x 2048.
     *
     * @param bitmap the bitmap to use as an image source
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the bitmap is null or recycled
     */
    public static ImageSource fromBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromBitmap", "invalidBitmap"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_BITMAP;
        imageSource.source = bitmap;
        return imageSource;
    }

    /**
     * Constructs an image source with an Android resource identifier. The resource must be accessible from the Android
     * Context associated with the World Window, and its dimensions should be no greater than 2048 x 2048.
     *
     * @param id the resource identifier, as generated by the aapt tool
     *
     * @return the new image source
     */
    public static ImageSource fromResource(@DrawableRes int id) {
        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_RESOURCE;
        imageSource.source = id;
        return imageSource;
    }

    /**
     * Constructs an image source with a file path.
     *
     * @param pathName complete path name to the file
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the path name is null
     */
    public static ImageSource fromFilePath(String pathName) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromFilePath", "missingPathName"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_FILE_PATH;
        imageSource.source = pathName;
        return imageSource;
    }

    /**
     * Constructs an image source with a URL string. The image's dimensions should be no greater than 2048 x 2048. The
     * application's manifest must include the permissions that allow network connections.
     *
     * @param urlString complete URL string
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the URL string is null
     */
    public static ImageSource fromUrl(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromUrl", "missingUrl"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_URL;
        imageSource.source = urlString;
        return imageSource;
    }

    /**
     * Constructs an image source with a generic Object instance. The source may be any non-null Object. This is
     * equivalent to calling one of ImageSource's type-specific factory methods when the source is a recognized type:
     * image factory; bitmap; integer resource ID; file path; URL string.
     *
     * @param source the generic source
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the source is null
     */
    public static ImageSource fromObject(Object source) {
        if (source == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromObject", "missingSource"));
        }

        if (source instanceof ImageFactory) {
            return fromImageFactory((ImageFactory) source, null); // no optional factory source argument
        } else if (source instanceof Bitmap) {
            return fromBitmap((Bitmap) source);
        } else if (source instanceof Integer) { // Android resource identifier, as generated by the aapt tool
            return fromResource((Integer) source);
        } else if (source instanceof String && WWUtil.isUrlString((String) source)) {
            return fromUrl((String) source);
        } else if (source instanceof String) {
            return fromFilePath((String) source);
        } else {
            ImageSource imageSource = new ImageSource();
            imageSource.type = TYPE_UNRECOGNIZED;
            imageSource.source = source;
            return imageSource;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        ImageSource that = (ImageSource) o;
        return this.type == that.type && this.source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return this.type + 31 * this.source.hashCode();
    }

    @Override
    public String toString() {
        if (this.isBitmap()) {
            return "Bitmap " + this.source.toString();
        } else if (this.type == TYPE_IMAGE_FACTORY) {
            return "ImageFactory " + ((Pair) this.source).first + " with bitmap source " + ((Pair) this.source).second;
        } else if (this.type == TYPE_RESOURCE) {
            return "Resource " + this.source.toString();
        } else if (this.type == TYPE_FILE_PATH) {
            return this.source.toString();
        } else if (this.type == TYPE_URL) {
            return this.source.toString();
        } else {
            return this.source.toString();
        }
    }

    /**
     * Indicates whether this image source is a Bitmap.
     *
     * @return true if the source is a Bitmap, otherwise false
     */
    public boolean isBitmap() {
        return this.type == TYPE_BITMAP;
    }

    /**
     * Indicates whether this image source is an image factory.
     *
     * @return true if the source is an image factory, otherwise false
     */
    public boolean isImageFactory() {
        return this.type == TYPE_IMAGE_FACTORY;
    }

    /**
     * Indicates whether this image source is an Android resource.
     *
     * @return true if the source is an Android resource, otherwise false
     */
    public boolean isResource() {
        return this.type == TYPE_RESOURCE;
    }

    /**
     * Indicates whether this image source is a file path.
     *
     * @return true if the source is an file path, otherwise false
     */
    public boolean isFilePath() {
        return this.type == TYPE_FILE_PATH;
    }

    /**
     * Indicates whether this image source is a URL string.
     *
     * @return true if the source is a URL string, otherwise false
     */
    public boolean isUrl() {
        return this.type == TYPE_URL;
    }

    /**
     * Returns the source image factory. Call isImageFactory to determine whether or not the source is an image
     * factory.
     *
     * @return the image factory and an optional factory source argument as a Pair, or null if the source is not an
     * image factory
     */
    @SuppressWarnings("unchecked")
    public Pair<ImageFactory, Object> asImageFactory() {
        return (this.type == TYPE_IMAGE_FACTORY) ? (Pair<ImageFactory, Object>) this.source : null;
    }

    /**
     * Returns the source bitmap. Call isBitmap to determine whether or not the source is a bitmap.
     *
     * @return the bitmap, or null if the source is not a bitmap
     */
    public Bitmap asBitmap() {
        return (this.type == TYPE_BITMAP) ? (Bitmap) this.source : null;
    }

    /**
     * Returns the source Android resource identifier. Call isResource to determine whether or not the source is an
     * Android resource.
     *
     * @return the resource identifier as generated by the aapt tool, or null if the source is not an Android resource
     */
    @DrawableRes
    public int asResource() {
        return (this.type == TYPE_RESOURCE) ? (int) this.source : 0;
    }

    /**
     * Returns the source file path name. Call isFilePath to determine whether or not the source is a file path.
     *
     * @return the file path name, or null if the source is not a file path
     */
    public String asFilePath() {
        return (this.type == TYPE_FILE_PATH) ? (String) this.source : null;
    }

    /**
     * Returns the source URL string. Call isUrl to determine whether or not the source is a URL string.
     *
     * @return the URL string, or null if the source is not a URL string
     */
    public String asUrl() {
        return (this.type == TYPE_URL) ? (String) this.source : null;
    }

    /**
     * Returns the image source associated with an unrecognized type.
     *
     * @return the source object
     */
    public Object asObject() {
        return this.source;
    }
}
