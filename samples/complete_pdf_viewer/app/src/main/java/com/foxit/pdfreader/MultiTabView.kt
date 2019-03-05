/**
 * Copyright (C) 2003-2019, Foxit Software Inc..
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
package com.foxit.pdfreader

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

import com.foxit.App
import com.foxit.home.R

import java.util.ArrayList


class MultiTabView {

    private val mTabEventListeners = ArrayList<ITabEventListener>()


    private var mContext: Context? = null
    var tabView: View? = null
        private set
    private var mRelativeLayout: RelativeLayout? = null
    private var mLinearLayout: LinearLayout? = null

    val historyFileNames = ArrayList<String>()

    inner class TabInfo {
        var tabIndex: Int = 0
        var tabTitle: String? = null
        var tabTarget: String? = null
        //        public boolean isDel;
    }

    interface ITabEventListener {
        fun onTabChanged(oldTabInfo: TabInfo, newTabInfo: TabInfo)
        fun onTabRemoved(removedTab: TabInfo, showTab: TabInfo?)
    }

    fun registerTabEventListener(listener: ITabEventListener) {
        mTabEventListeners.add(listener)
    }

    fun unregisterTabEventListener(listener: ITabEventListener) {
        mTabEventListeners.remove(listener)
    }

    private fun onTabChanged(oldTabInfo: TabInfo, newTabInfo: TabInfo) {
        for (listener in mTabEventListeners) {
            listener.onTabChanged(oldTabInfo, newTabInfo)
        }
    }

    private fun onTabRemoved(removedTab: TabInfo, showTab: TabInfo?) {
        for (listener in mTabEventListeners) {
            listener.onTabRemoved(removedTab, showTab)
        }
    }

    fun initialize(): Boolean {
        mContext = App.instance().applicationContext

        tabView = View.inflate(mContext, R.layout.multiple_tabview_father, null)
        mRelativeLayout = tabView!!.findViewById<View>(R.id._feature_rd_multiple_scroll) as RelativeLayout
        mLinearLayout = mRelativeLayout!!.findViewById<View>(R.id._feature_rd_multiple_rl) as LinearLayout
        //            mTabView = View.inflate(mContext, R.layout.multiple_tabview, null);
        return true
    }


    fun resetData(): Boolean {
        historyFileNames.clear()
        return true
    }

    fun removeTab(tabInfo: TabInfo) {
        historyFileNames.remove(tabInfo.tabTarget)
    }

    fun refreshTopBar(docPath: String) {
        var mTabCloseImageView: ImageView
        var mTabCloseRelativeLayout: RelativeLayout
        var mRelativeLayoutSignalTab: RelativeLayout
        var mRelativeLayoutSignalTab1: RelativeLayout
        var mTabNameTextView: TextView
        var mTextView: TextView
        var mTextView1: TextView
        var mTextView2: TextView

        if (!historyFileNames.contains(docPath)) {
            historyFileNames.add(docPath)
        }

        mLinearLayout!!.removeAllViews()
        val count = historyFileNames.size
        for (j in 0 until count) {
            val name = historyFileNames[j]
            val mView = View.inflate(mContext, R.layout.multiple_tabview, null)
            val tabParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            val rlParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            mView.layoutParams = tabParams
            mTabCloseImageView = mView.findViewById<View>(R.id.multiple_tabview_ig_close) as ImageView
            mTabCloseRelativeLayout = mView.findViewById<View>(R.id.multiple_rl_ig_close) as RelativeLayout
            mTabNameTextView = mView.findViewById<View>(R.id.multiple_tabview_tv_name) as TextView
            mTextView = mView.findViewById<View>(R.id.multiple_tabview_tv) as TextView
            mTextView1 = mView.findViewById<View>(R.id.multiple_tabview_tv1) as TextView
            mTextView2 = mView.findViewById<View>(R.id.multiple_tabview_tv2) as TextView
            mRelativeLayoutSignalTab = mView.findViewById<View>(R.id._feature_rd_multiple_tab_rl) as RelativeLayout
            mRelativeLayoutSignalTab1 = mView.findViewById<View>(R.id._feature_rd_multiple_tab_rl1) as RelativeLayout
            var changeColorDocName = String()
            changeColorDocName = docPath
            if (name == changeColorDocName && count > 1) {
                mTabCloseImageView.setImageDrawable(mContext!!.resources.getDrawable(R.drawable._feature_rd_multiple_tab_close))
                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#C3C3C3"))
                mTabNameTextView.setBackgroundColor(Color.parseColor("#E5E5E5"))
                mTabCloseImageView.setBackgroundColor(Color.parseColor("#E5E5E5"))
                mTabCloseRelativeLayout.setBackgroundColor(Color.parseColor("#E5E5E5"))
                mTextView.setBackgroundColor(Color.parseColor("#E5E5E5"))
                mTextView1.setBackgroundColor(Color.parseColor("#E5E5E5"))
                mTextView2.setBackgroundColor(Color.parseColor("#E5E5E5"))
            }
            if (count == 1) {
                //                mTabCloseImageView.setImageDrawable(null);
                mTabCloseImageView.setImageDrawable(mContext!!.resources.getDrawable(R.drawable._feature_rd_multiple_tab_close))
                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTabNameTextView.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTabCloseImageView.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTabCloseRelativeLayout.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTextView.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTextView1.setBackgroundColor(Color.parseColor("#FAFAFA"))
                mTextView2.setBackgroundColor(Color.parseColor("#FAFAFA"))
            }

            if (j == 0/*(count - 1)*/) {
                rlParams.setMargins(2, 2, 2, 0)
                mRelativeLayoutSignalTab.layoutParams = rlParams
            }
            mLinearLayout!!.addView(mView)
            mTabCloseImageView.isEnabled = true
            mTabCloseRelativeLayout.isEnabled = true
            mTabCloseRelativeLayout.setOnClickListener(View.OnClickListener {
                if (historyFileNames.size == 1) {
                    //                        mHistoryFileNames.remove(name); // do after close doc

                    val tabInfo = TabInfo()
                    tabInfo.tabTarget = name

                    onTabRemoved(tabInfo, null)
                    return@OnClickListener
                }

                if (name != docPath) {
                    //                        mHistoryFileNames.remove(name);// do after close doc

                    val tabInfo = TabInfo()
                    tabInfo.tabTarget = name

                    val showTab = TabInfo()
                    showTab.tabTarget = docPath
                    onTabRemoved(tabInfo, showTab)
                    //                        refreshTopBar(docPath);
                    return@OnClickListener
                }

                // remove the current tab
                var curTabIndex = 0
                for (i in historyFileNames.indices) {
                    val tabName = historyFileNames[i]
                    if (tabName == docPath) {
                        curTabIndex = i
                        break
                    }
                }
                var path = ""
                if (curTabIndex == 0 || curTabIndex < historyFileNames.size - 1) {
                    path = historyFileNames[curTabIndex + 1] // get the previous tab
                } else {
                    path = historyFileNames[curTabIndex - 1] // get the previous tab
                }
                //                    mHistoryFileNames.remove(name); // do after close doc
                val oldTabInfo = TabInfo()
                oldTabInfo.tabTarget = name

                val newTabInfo = TabInfo()
                newTabInfo.tabTarget = path
                onTabRemoved(oldTabInfo, newTabInfo)
                //                    refreshTopBar(path);
            })
            mTabCloseImageView.setOnClickListener(View.OnClickListener {
                if (historyFileNames.size == 1) {
                    //                        mHistoryFileNames.remove(name);// do after close doc

                    val tabInfo = TabInfo()
                    tabInfo.tabTarget = name

                    onTabRemoved(tabInfo, null)
                    return@OnClickListener
                }

                if (name != docPath) {
                    //                        mHistoryFileNames.remove(name);// do after close doc
                    val tabInfo = TabInfo()
                    tabInfo.tabTarget = name

                    val showTab = TabInfo()
                    showTab.tabTarget = docPath
                    onTabRemoved(tabInfo, showTab)
                    //                        refreshTopBar(docPath);
                    return@OnClickListener
                }

                // remove the current tab
                var curTabIndex = 0
                for (i in historyFileNames.indices) {
                    val tabName = historyFileNames[i]
                    if (tabName == docPath) {
                        curTabIndex = i
                        break
                    }
                }
                var path = ""
                if (curTabIndex == 0 || curTabIndex < historyFileNames.size - 1) {
                    path = historyFileNames[curTabIndex + 1] // get the previous tab
                } else {
                    path = historyFileNames[curTabIndex - 1] // get the previous tab
                }
                //                    mHistoryFileNames.remove(name); // do after close doc

                val oldTabInfo = TabInfo()
                oldTabInfo.tabTarget = name

                val newTabInfo = TabInfo()
                newTabInfo.tabTarget = path
                onTabRemoved(oldTabInfo, newTabInfo)

                //                    refreshTopBar(path);
            })

            mTabNameTextView.isEnabled = true
            mTabNameTextView.setOnClickListener(View.OnClickListener {
                if (name == docPath) return@OnClickListener
                val FileItemSize = historyFileNames.size
                for (h in 0 until FileItemSize) {
                    if (name == historyFileNames[h]) {
                        val oldTabInfo = TabInfo()
                        oldTabInfo.tabTarget = docPath

                        val newTabInfo = TabInfo()
                        newTabInfo.tabTarget = name
                        onTabChanged(oldTabInfo, newTabInfo)
                    }
                }

                refreshTopBar(name)
            })
            mTabNameTextView.text = name.substring(name.lastIndexOf("/") + 1, name.length)
        }
    }

    companion object {

        val MAX_NUM_TABS_PHONE = 3
        val MAX_NUM_TABS_PAD = 5
    }
}