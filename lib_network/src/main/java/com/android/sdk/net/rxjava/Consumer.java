package com.android.sdk.net.rxjava;

import androidx.annotation.Nullable;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-05-15 19:04
 */
public interface Consumer<T> {

    void accept(@Nullable T t);

}