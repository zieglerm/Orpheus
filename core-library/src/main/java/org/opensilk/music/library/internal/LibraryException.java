/*
 * Copyright (c) 2014 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.music.library.internal;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Specialized execption capable of being sent across ipc. All exceptions
 * sent back to orpheus *must* be wrapped by this.
 *
 * Created by drew on 11/11/14.
 */
public class LibraryException extends Exception implements Parcelable {

    private static final long serialVersionUID = 3941740512702819161L;

    public interface Kind {
        /**
         * Unknown or non recoverable error.
         */
        int UNKNOWN = -1;
        /**
         * Temporary failure, Orpheus will retry request with exponential backoff
         */
        int RETRY = 1;
        /**
         * Permanent auth failure, Orpheus will relaunch the library picker activity
         */
        int AUTH_FAILURE = 2;
        /**
         * IO or Network error, Orpheus will check connectivity and retry with exponential backoff
         */
        int NETWORK = 3;
        /**
         * Internal use: argument for {@link org.opensilk.music.library.provider.LibraryMethods.Extras#CAUSE}
         * in returned bundle from {@link org.opensilk.music.library.provider.LibraryProvider#call}
         * when invalid method is requested
         */
        int METHOD_NOT_IMPLEMENTED = 4;
        /**
         * Internal use: argument for {@link org.opensilk.music.library.provider.LibraryMethods.Extras#CAUSE}
         * in returned bundle from {@link org.opensilk.music.library.provider.LibraryProvider#call}
         * when requested uri is malformed or unknown
         */
        int ILLEGAL_URI = 5;
        /**
         * Internal use: argument for {@link org.opensilk.music.library.provider.LibraryMethods.Extras#CAUSE}
         * in returned bundle from {@link org.opensilk.music.library.provider.LibraryProvider#call}
         * when {@link org.opensilk.music.library.provider.LibraryMethods.Extras#CALLBACK} is missing from
         * the Extras bundle passed to the call or when the binder is already dead when we received the call
         */
        int BAD_BINDER = 6;
    }

    //Following are deprecated TODO remove once users are converted
    public static final int UNKNOWN = Kind.UNKNOWN;
    public static final int RETRY = Kind.RETRY;
    public static final int AUTH_FAILURE = Kind.AUTH_FAILURE;
    public static final int NETWORK = Kind.NETWORK;

    private int code;

    public LibraryException(Throwable wrappedCause) {
        this(Kind.UNKNOWN, wrappedCause);
    }

    public LibraryException(int code, Throwable wrappedCause) {
        super(wrappedCause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeSerializable(getCause());
    }

    private static LibraryException readParcel(Parcel in) {
        return new LibraryException(
                in.readInt(),
                (Throwable) in.readSerializable()
        );
    }

    public static final Creator<LibraryException> CREATOR = new Creator<LibraryException>() {
        @Override public LibraryException createFromParcel(Parcel source) {
            return readParcel(source);
        }

        @Override public LibraryException[] newArray(int size) {
            return new LibraryException[size];
        }
    };
}
