/**
 * Copyright (C) 2003-2025, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.pdf.main

import android.content.Context
import com.foxit.pdf.main.FunctionAdapter.FunctionItemBean
import androidx.recyclerview.widget.RecyclerView
import com.foxit.pdf.main.FunctionAdapter.FunctionViewHolder
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import com.foxit.pdf.function_demo.R
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.widget.TextView
import android.widget.Button

internal class FunctionAdapter(
    private val mContext: Context,
    private val mDatas: List<FunctionItemBean>?
) : RecyclerView.Adapter<FunctionViewHolder>() {
    private var mItemClickListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunctionViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.main_item, parent, false)
        return FunctionViewHolder(view)
    }

    override fun onBindViewHolder(holder: FunctionViewHolder, position: Int) {
        holder.bind(mDatas!![position])
    }

    override fun getItemCount(): Int {
        return mDatas?.size ?: 0
    }

    internal inner class FunctionViewHolder(itemView: View) : ViewHolder(itemView) {
        private val mTvDesc: TextView
        private val mBtnName: Button
        fun bind(itemBean: FunctionItemBean) {
            mTvDesc.text = itemBean.desc
            mBtnName.text = itemBean.name
        }

        init {
            mTvDesc = itemView.findViewById(R.id.tv_desc)
            mBtnName = itemView.findViewById(R.id.btn_name)
            mBtnName.setOnClickListener {
                if (mItemClickListener != null) {
                    val position = adapterPosition
                    mItemClickListener!!.onItemClick(position, mDatas!![position])
                }
            }
        }
    }

    internal class FunctionItemBean(var type: Int, var name: String, var desc: String)

    fun setOnItemClickListener(itemClickListener: OnItemClickListener?) {
        mItemClickListener = itemClickListener
    }

    internal interface OnItemClickListener {
        fun onItemClick(positon: Int, itemBean: FunctionItemBean?)
    }
}