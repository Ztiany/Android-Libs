package com.android.base.adapter.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * A simple way to build a simple list. If you want to build a multi type list, perhaps you need to use [MultiTypeAdapter].
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-01-15 11:41
 */
abstract class SimpleRecyclerAdapter<T, VB : ViewBinding>(
        context: Context,
        data: List<T> = mutableListOf()
) : RecyclerAdapter<T, BindingViewHolder<VB>>(context, data) {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<VB> {
        return BindingViewHolder(provideViewBinding(parent, inflater))
    }

    protected open fun onViewHolderCreated(viewHolder: BindingViewHolder<VB>) = Unit

    /**provide a layout id or a View*/
    abstract fun provideViewBinding(parent: ViewGroup, inflater: LayoutInflater): VB

    override fun onBindViewHolder(viewHolder: BindingViewHolder<VB>, position: Int) {
        val item = getItem(position)
        if (item != null) {
            bind(viewHolder, item)
        }
    }

    protected open fun bind(viewHolder: BindingViewHolder<VB>, item: T) {

    }

}