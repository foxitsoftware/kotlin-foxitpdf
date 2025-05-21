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
package com.foxit.pdfreader

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.foxit.App
import com.foxit.home.R
import com.foxit.uiextensions.utils.AppResource
import java.util.*

class MultiTabView {
    inner class TabInfo {
        var tabIndex = 0
        var tabTitle: String? = null
        var tabTarget: String? = null //        public boolean isDel;
    }

    interface ITabEventListener {
        fun onTabChanged(oldTabInfo: TabInfo?, newTabInfo: TabInfo?)
        fun onTabRemoved(removedTab: TabInfo?, showTab: TabInfo?)
    }

    private val mTabEventListeners = ArrayList<ITabEventListener>()
    fun registerTabEventListener(listener: ITabEventListener) {
        if (!mTabEventListeners.contains(listener)) {
            mTabEventListeners.add(listener)
        }
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

    private var mContext: Context? = null
    var tabView: View? = null
        private set
    private var mRelativeLayout: RelativeLayout? = null
    private var mLinearLayout: LinearLayout? = null
    val historyFileNames = ArrayList<String?>()
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

    fun removeTab(path: String?) {
        if (historyFileNames.size == 1) {
            val tabInfo: TabInfo = TabInfo()
            tabInfo.tabTarget = path
            onTabRemoved(tabInfo, null)
            return
        }
        var curTabIndex = 0
        for (i in historyFileNames.indices) {
            val tabName = historyFileNames[i]
            if (tabName == path) {
                curTabIndex = i
                break
            }
        }
        var newPath: String? = ""
        newPath = if (curTabIndex == 0 || curTabIndex < historyFileNames.size - 1) {
            historyFileNames[curTabIndex + 1] // get the next tab
        } else {
            historyFileNames[curTabIndex - 1] // get the previous tab
        }
        val oldTabInfo = TabInfo()
        oldTabInfo.tabTarget = path
        val newTabInfo = TabInfo()
        newTabInfo.tabTarget = newPath
        onTabRemoved(oldTabInfo, newTabInfo)
    }

    fun removeTab(tabInfo: TabInfo) {
        historyFileNames.remove(tabInfo.tabTarget)
    }

    fun refreshTopBar(docPath: String?) {
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
            val changeColorDocName = docPath
            if (name == changeColorDocName && count > 1) {
                mTabCloseImageView.setImageDrawable(AppResource.getDrawable(mContext, R.drawable._feature_rd_multiple_tab_close))
                //                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#C3C3C3"));
                val selectColor = AppResource.getColor(mContext, com.foxit.uiextensions.R.color.ux_color_select_tab)
                mTabNameTextView.setBackgroundColor(selectColor)
                mTabCloseImageView.setBackgroundColor(selectColor)
                mTabCloseRelativeLayout.setBackgroundColor(selectColor)
                mTextView.setBackgroundColor(selectColor)
                mTextView1.setBackgroundColor(selectColor)
                mTextView2.setBackgroundColor(selectColor)
            }
            if (count == 1) {
//                mTabCloseImageView.setImageDrawable(null);
                mTabCloseImageView.setImageDrawable(AppResource.getDrawable(mContext, R.drawable._feature_rd_multiple_tab_close))
                //                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#FAFAFA"));
                val unSelectColor = AppResource.getColor(mContext, com.foxit.uiextensions.R.color.ux_color_select_tab)
                mTabNameTextView.setBackgroundColor(unSelectColor)
                mTabCloseImageView.setBackgroundColor(unSelectColor)
                mTabCloseRelativeLayout.setBackgroundColor(unSelectColor)
                mTextView.setBackgroundColor(unSelectColor)
                mTextView1.setBackgroundColor(unSelectColor)
                mTextView2.setBackgroundColor(unSelectColor)
            }
            if (j == 0 /*(count - 1)*/) {
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
                var path: String? = ""
                path = if (curTabIndex == 0 || curTabIndex < historyFileNames.size - 1) {
                    historyFileNames[curTabIndex + 1] // get the previous tab
                } else {
                    historyFileNames[curTabIndex - 1] // get the previous tab
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
                var path: String? = ""
                path = if (curTabIndex == 0 || curTabIndex < historyFileNames.size - 1) {
                    historyFileNames[curTabIndex + 1] // get the previous tab
                } else {
                    historyFileNames[curTabIndex - 1] // get the previous tab
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
            mTabNameTextView.text = name!!.substring(name.lastIndexOf("/") + 1, name.length)
        }
    }

}