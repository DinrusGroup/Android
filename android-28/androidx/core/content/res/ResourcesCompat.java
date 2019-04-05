/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.core.content.res;

import static android.os.Build.VERSION.SDK_INT;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.content.res.FontResourcesParserCompat.FamilyResourceEntry;
import androidx.core.graphics.TypefaceCompat;
import androidx.core.provider.FontsContractCompat.FontRequestCallback;
import androidx.core.provider.FontsContractCompat.FontRequestCallback.FontRequestFailReason;
import androidx.core.util.Preconditions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Helper for accessing features in {@link android.content.res.Resources}.
 */
public final class ResourcesCompat {
    private static final String TAG = "ResourcesCompat";

    /**
     * Return a drawable object associated with a particular resource ID and
     * styled for the specified theme. Various types of objects will be
     * returned depending on the underlying resource -- for example, a solid
     * color, PNG image, scalable image, etc.
     * <p>
     * Prior to API level 21, the theme will not be applied and this method
     * simply calls through to {@link Resources#getDrawable(int)}.
     *
     * @param id The desired resource identifier, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource
     *           entry. The value 0 is an invalid identifier.
     * @param theme The theme used to style the drawable attributes, may be
     *              {@code null}.
     * @return Drawable An object that can be used to draw this resource.
     * @throws NotFoundException Throws NotFoundException if the given ID does
     *         not exist.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(@NonNull Resources res, @DrawableRes int id,
            @Nullable Theme theme) throws NotFoundException {
        if (SDK_INT >= 21) {
            return res.getDrawable(id, theme);
        } else {
            return res.getDrawable(id);
        }
    }


    /**
     * Return a drawable object associated with a particular resource ID for
     * the given screen density in DPI and styled for the specified theme.
     * <p>
     * Prior to API level 15, the theme and density will not be applied and
     * this method simply calls through to {@link Resources#getDrawable(int)}.
     * <p>
     * Prior to API level 21, the theme will not be applied and this method
     * calls through to Resources#getDrawableForDensity(int, int).
     *
     * @param id The desired resource identifier, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource
     *           entry. The value 0 is an invalid identifier.
     * @param density The desired screen density indicated by the resource as
     *                found in {@link android.util.DisplayMetrics}.
     * @param theme The theme used to style the drawable attributes, may be
     *              {@code null}.
     * @return Drawable An object that can be used to draw this resource.
     * @throws NotFoundException Throws NotFoundException if the given ID does
     *         not exist.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDrawableForDensity(@NonNull Resources res, @DrawableRes int id,
            int density, @Nullable Theme theme) throws NotFoundException {
        if (SDK_INT >= 21) {
            return res.getDrawableForDensity(id, density, theme);
        } else if (SDK_INT >= 15) {
            return res.getDrawableForDensity(id, density);
        } else {
            return res.getDrawable(id);
        }
    }

    /**
     * Returns a themed color integer associated with a particular resource ID.
     * If the resource holds a complex {@link ColorStateList}, then the default
     * color from the set is returned.
     * <p>
     * Prior to API level 23, the theme will not be applied and this method
     * calls through to {@link Resources#getColor(int)}.
     *
     * @param id The desired resource identifier, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource
     *           entry. The value 0 is an invalid identifier.
     * @param theme The theme used to style the color attributes, may be
     *              {@code null}.
     * @return A single color value in the form {@code 0xAARRGGBB}.
     * @throws NotFoundException Throws NotFoundException if the given ID does
     *         not exist.
     */
    @ColorInt
    @SuppressWarnings("deprecation")
    public static int getColor(@NonNull Resources res, @ColorRes int id, @Nullable Theme theme)
            throws NotFoundException {
        if (SDK_INT >= 23) {
            return res.getColor(id, theme);
        } else {
            return res.getColor(id);
        }
    }

    /**
     * Returns a themed color state list associated with a particular resource
     * ID. The resource may contain either a single raw color value or a
     * complex {@link ColorStateList} holding multiple possible colors.
     * <p>
     * Prior to API level 23, the theme will not be applied and this method
     * calls through to {@link Resources#getColorStateList(int)}.
     *
     * @param id The desired resource identifier of a {@link ColorStateList},
     *           as generated by the aapt tool. This integer encodes the
     *           package, type, and resource entry. The value 0 is an invalid
     *           identifier.
     * @param theme The theme used to style the color attributes, may be
     *              {@code null}.
     * @return A themed ColorStateList object containing either a single solid
     *         color or multiple colors that can be selected based on a state.
     * @throws NotFoundException Throws NotFoundException if the given ID does
     *         not exist.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public static ColorStateList getColorStateList(@NonNull Resources res, @ColorRes int id,
            @Nullable Theme theme) throws NotFoundException {
        if (SDK_INT >= 23) {
            return res.getColorStateList(id, theme);
        } else {
            return res.getColorStateList(id);
        }
    }

    /**
     * Returns a font Typeface associated with a particular resource ID.
     * <p>
     * This method will block the calling thread to retrieve the requested font, including if it
     * is from a font provider. If you wish to not have this behavior, use
     * {@link #getFont(Context, int, FontCallback, Handler)} instead.
     * <p>
     * Prior to API level 23, font resources with more than one font in a family will only load the
     * font closest to a regular weight typeface.
     *
     * @param context A context to retrieve the Resources from.
     * @param id The desired resource identifier of a {@link Typeface},
     *           as generated by the aapt tool. This integer encodes the
     *           package, type, and resource entry. The value 0 is an invalid
     *           identifier.
     * @return A font Typeface object.
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     *
     * @see #getFont(Context, int, FontCallback, Handler)
     */
    @Nullable
    public static Typeface getFont(@NonNull Context context, @FontRes int id)
            throws NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, new TypedValue(), Typeface.NORMAL, null /* callback */,
                null /* handler */, false /* isXmlRequest */);
    }

    /**
     * Interface used to receive asynchronous font fetching events.
     */
    public abstract static class FontCallback {

        /**
         * Called when an asynchronous font was finished loading.
         *
         * @param typeface The font that was loaded.
         */
        public abstract void onFontRetrieved(@NonNull Typeface typeface);

        /**
         * Called when an asynchronous font failed to load.
         *
         * @param reason The reason the font failed to load. One of
         *      {@link FontRequestFailReason#FAIL_REASON_PROVIDER_NOT_FOUND},
         *      {@link FontRequestFailReason#FAIL_REASON_WRONG_CERTIFICATES},
         *      {@link FontRequestFailReason#FAIL_REASON_FONT_LOAD_ERROR},
         *      {@link FontRequestFailReason#FAIL_REASON_SECURITY_VIOLATION},
         *      {@link FontRequestFailReason#FAIL_REASON_FONT_NOT_FOUND},
         *      {@link FontRequestFailReason#FAIL_REASON_FONT_UNAVAILABLE} or
         *      {@link FontRequestFailReason#FAIL_REASON_MALFORMED_QUERY}.
         */
        public abstract void onFontRetrievalFailed(@FontRequestFailReason int reason);

        /**
         * Call {@link #onFontRetrieved(Typeface)} on the handler given, or the Ui Thread if it is
         * null.
         * @hide
         */
        @RestrictTo(LIBRARY_GROUP)
        public final void callbackSuccessAsync(final Typeface typeface, @Nullable Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onFontRetrieved(typeface);
                }
            });
        }

        /**
         * Call {@link #onFontRetrievalFailed(int)} on the handler given, or the Ui Thread if it is
         * null.
         * @hide
         */
        @RestrictTo(LIBRARY_GROUP)
        public final void callbackFailAsync(
                @FontRequestFailReason final int reason, @Nullable Handler handler) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onFontRetrievalFailed(reason);
                }
            });
        }
    }

    /**
     * Returns a font Typeface associated with a particular resource ID asynchronously.
     * <p>
     * Prior to API level 23, font resources with more than one font in a family will only load the
     * font closest to a regular weight typeface.
     * </p>
     *
     * @param context A context to retrieve the Resources from.
     * @param id The desired resource identifier of a {@link Typeface}, as generated by the aapt
     *           tool. This integer encodes the package, type, and resource entry. The value 0 is an
     *           invalid identifier.
     * @param fontCallback A callback to receive async fetching of this font. The callback will be
     *           triggered on the UI thread.
     * @param handler A handler for the thread the callback should be called on. If null, the
     *           callback will be called on the UI thread.
     * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
     */
    public static void getFont(@NonNull Context context, @FontRes int id,
            @NonNull FontCallback fontCallback, @Nullable Handler handler)
            throws NotFoundException {
        Preconditions.checkNotNull(fontCallback);
        if (context.isRestricted()) {
            fontCallback.callbackFailAsync(
                    FontRequestCallback.FAIL_REASON_SECURITY_VIOLATION, handler);
            return;
        }
        loadFont(context, id, new TypedValue(), Typeface.NORMAL, fontCallback, handler,
                false /* isXmlRequest */);
    }

    /**
     * Used by TintTypedArray.
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    public static Typeface getFont(@NonNull Context context, @FontRes int id, TypedValue value,
            int style, @Nullable FontCallback fontCallback) throws NotFoundException {
        if (context.isRestricted()) {
            return null;
        }
        return loadFont(context, id, value, style, fontCallback, null /* handler */,
                true /* isXmlRequest */);
    }

    /**
     *
     * @param context The Context to get Resources from
     * @param id The Resource id to load
     * @param value A TypedValue to use in the fetching
     * @param style The font style to load
     * @param fontCallback A callback to trigger when the font is fetched or an error occurs
     * @param handler A handler to the thread the callback should be called on
     * @param isRequestFromLayoutInflator Whether this request originated from XML. This is used to
     *                     determine if we use or ignore the fontProviderFetchStrategy attribute in
     *                     font provider XML fonts.
     * @return
     */
    private static Typeface loadFont(@NonNull Context context, int id, TypedValue value,
            int style, @Nullable FontCallback fontCallback, @Nullable Handler handler,
            boolean isRequestFromLayoutInflator) {
        final Resources resources = context.getResources();
        resources.getValue(id, value, true);
        Typeface typeface = loadFont(context, resources, value, id, style, fontCallback, handler,
                isRequestFromLayoutInflator);
        if (typeface == null && fontCallback == null) {
            throw new NotFoundException("Font resource ID #0x"
                    + Integer.toHexString(id) + " could not be retrieved.");
        }
        return typeface;
    }

    /**
     * Load the given font. This method will always return null for asynchronous requests, which
     * provide a fontCallback, as there is no immediate result. When the callback is not provided,
     * the request is treated as synchronous and fails if async loading is required.
     */
    private static Typeface loadFont(
            @NonNull Context context, Resources wrapper, TypedValue value, int id, int style,
            @Nullable FontCallback fontCallback, @Nullable Handler handler,
            boolean isRequestFromLayoutInflator) {
        if (value.string == null) {
            throw new NotFoundException("Resource \"" + wrapper.getResourceName(id) + "\" ("
                    + Integer.toHexString(id) + ") is not a Font: " + value);
        }

        final String file = value.string.toString();
        if (!file.startsWith("res/")) {
            // Early exit if the specified string is unlikely to be a resource path.
            if (fontCallback != null) {
                fontCallback.callbackFailAsync(
                        FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR, handler);
            }
            return null;
        }
        Typeface typeface = TypefaceCompat.findFromCache(wrapper, id, style);

        if (typeface != null) {
            if (fontCallback != null) {
                fontCallback.callbackSuccessAsync(typeface, handler);
            }
            return typeface;
        }

        try {
            if (file.toLowerCase().endsWith(".xml")) {
                final XmlResourceParser rp = wrapper.getXml(id);
                final FamilyResourceEntry familyEntry =
                        FontResourcesParserCompat.parse(rp, wrapper);
                if (familyEntry == null) {
                    Log.e(TAG, "Failed to find font-family tag");
                    if (fontCallback != null) {
                        fontCallback.callbackFailAsync(
                                FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR, handler);
                    }
                    return null;
                }
                return TypefaceCompat.createFromResourcesFamilyXml(context, familyEntry, wrapper,
                        id, style, fontCallback, handler, isRequestFromLayoutInflator);
            }
            typeface = TypefaceCompat.createFromResourcesFontFile(
                    context, wrapper, id, file, style);
            if (fontCallback != null) {
                if (typeface != null) {
                    fontCallback.callbackSuccessAsync(typeface, handler);
                } else {
                    fontCallback.callbackFailAsync(
                            FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR, handler);
                }
            }
            return typeface;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Failed to parse xml resource " + file, e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read xml resource " + file, e);
        }
        if (fontCallback != null) {
            fontCallback.callbackFailAsync(
                    FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR, handler);
        }
        return null;
    }

    private ResourcesCompat() {}
}
