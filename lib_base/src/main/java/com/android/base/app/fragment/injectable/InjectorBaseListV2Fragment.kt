package com.android.base.app.fragment.injectable

import androidx.lifecycle.ViewModelProvider
import com.android.base.app.dagger.Injectable
import com.android.base.app.fragment.BaseListV2Fragment
import com.android.base.data.ErrorHandler
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-15 12:57
 */
open class InjectorBaseListV2Fragment<T> : BaseListV2Fragment<T>(), Injectable, InjectableEx {

    @Inject override lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject override lateinit var errorHandler: ErrorHandler

}