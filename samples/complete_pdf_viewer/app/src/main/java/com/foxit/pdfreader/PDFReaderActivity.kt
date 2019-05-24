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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout

import com.foxit.App
import com.foxit.home.MainActivity
import com.foxit.home.R
import com.foxit.pdfreader.fragment.BaseFragment
import com.foxit.pdfreader.fragment.EmptyViewFragment
import com.foxit.pdfreader.fragment.PDFReaderFragment
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.modules.signature.SignatureToolHandler
import com.foxit.uiextensions.utils.AppFileUtil
import com.foxit.uiextensions.utils.AppTheme


class PDFReaderActivity : FragmentActivity(), UIExtensionsManager.OnFinishListener {

    private var mFragmentManager: FragmentManager? = null

    private var filePath: String? = null
    internal var filter: String = ""

    private var mTabEventListener: MultiTabView.ITabEventListener? = object : MultiTabView.ITabEventListener {
        override fun onTabChanged(oldTabInfo: MultiTabView.TabInfo, newTabInfo: MultiTabView.TabInfo) {
            val fragment = App.instance().getTabsManager(filter).fragmentMap[oldTabInfo.tabTarget] as PDFReaderFragment?
            changeViewerState(fragment!!)
            filePath = newTabInfo.tabTarget
            App.instance().getTabsManager(filter).filePath = filePath

            val newfragment = App.instance().getTabsManager(filter).fragmentMap[filePath] as PDFReaderFragment?
            newfragment!!.mUiExtensionsManager!!.documentManager.resetActionCallback()

            openDocView(false)
            resetTabView(false)
        }

        override fun onTabRemoved(removedTab: MultiTabView.TabInfo, showTab: MultiTabView.TabInfo?) {
            val fragment = App.instance().getTabsManager(filter).fragmentMap[removedTab.tabTarget] as PDFReaderFragment?
            if (removedTab.tabTarget == App.instance().getTabsManager(filter).filePath) {
                changeViewerState(fragment!!)

                fragment.doClose(object : BaseFragment.IFragmentEvent {
                    override fun onRemove() {
                        App.instance().getMultiTabView(filter).removeTab(removedTab)
                        if (showTab != null) {
                            filePath = showTab.tabTarget
                            App.instance().getTabsManager(filter).filePath = filePath

                            val newfragment = App.instance().getTabsManager(filter).fragmentMap[filePath] as PDFReaderFragment
                            newfragment!!.mUiExtensionsManager!!.documentManager.resetActionCallback()

                            openDocView(true)
                            resetTabView(false)
                            App.instance().getTabsManager(filter).fragmentMap.remove(removedTab.tabTarget)
                            App.instance().getMultiTabView(filter).refreshTopBar(showTab.tabTarget!!)
                        } else {
                            App.instance().getTabsManager(filter).filePath = null

                            // only one tab
                            removeFragment(fragment)
                            App.instance().getTabsManager(filter).currentFragment = null
                            App.instance().getTabsManager(filter).clearFragment()

                            val intent = Intent()
                            intent.setClass(applicationContext, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            startActivity(intent)
                        }
                    }
                })
            } else {
                fragment!!.doClose(object : BaseFragment.IFragmentEvent {
                    override fun onRemove() {
                        App.instance().getMultiTabView(filter).removeTab(removedTab)
                        removeFragment(fragment)
                        App.instance().getTabsManager(filter).fragmentMap.remove(removedTab.tabTarget)
                        App.instance().getMultiTabView(filter).refreshTopBar(showTab!!.tabTarget!!)
                    }
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppTheme.setThemeFullScreen(this)
        AppTheme.setThemeNeedMenuKey(this)
        setContentView(R.layout.activity_reader)
        filter = intent.getStringExtra("filter")
        if (App.instance().getTabsManager(filter).fragmentManager == null || !App.instance().isMultiTab) {
            mFragmentManager = supportFragmentManager
            App.instance().getTabsManager(filter).fragmentManager = mFragmentManager
        }
        if (!App.instance().checkLicense()) {
            openEmptyView()
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (App.instance().isMultiTab) {
                App.instance().getMultiTabView(filter).registerTabEventListener(this.mTabEventListener!!)
            }
            openDocument(intent)
        }
    }

    /**
     * open a Doc View use Fragment
     */
    private fun openDocView(isRemoveCurFragment: Boolean) {
        filePath = App.instance().getTabsManager(filter).filePath
        mFragmentManager = App.instance().getTabsManager(filter).fragmentManager
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        var fragment: PDFReaderFragment? = App.instance().getTabsManager(filter).fragmentMap[filePath] as PDFReaderFragment?
        if (fragment == null) {
            fragment = PDFReaderFragment()
            if (App.instance().isMultiTab) {
                App.instance().getTabsManager(filter).addFragment(filePath!!, fragment)
            }
            fragment.path = filePath
            fragment.onFinishListener = this
            fragment.filter = filter
        } else {
            if (App.instance().isMultiTab) {
                if (!fragment.isOpenSuccess) {
                    App.instance().getTabsManager(filter).removeFragment(filePath!!)
                    fragmentTransaction.remove(fragment)

                    fragment = PDFReaderFragment()
                    App.instance().getTabsManager(filter).addFragment(filePath!!, fragment)
                    fragment.path = filePath
                    fragment.onFinishListener = this
                    fragment.filter = filter
                }
            }
        }

        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.reader_container, fragment)
        }

        if (App.instance().getTabsManager(filter).currentFragment != null && App.instance().getTabsManager(filter).currentFragment != fragment) {
            if (isRemoveCurFragment) {
                fragmentTransaction.remove(App.instance().getTabsManager(filter).currentFragment)
            } else {
                fragmentTransaction.hide(App.instance().getTabsManager(filter).currentFragment)
            }
        }

        fragmentTransaction.show(fragment).commitAllowingStateLoss()

        if (App.instance().isMultiTab) {
            App.instance().getTabsManager(filter).currentFragment = fragment
        }
    }

    /**
     * when App license is valid, it should open a empty view use Fragment also.
     */
    private fun openEmptyView() {
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.reader_container, EmptyViewFragment())
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun backToMainActivity() {
        val intent = Intent()
        intent.setClass(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        var currentFrag: BaseFragment? = supportFragmentManager.findFragmentById(R.id.reader_container) as BaseFragment?

        if (App.instance().isMultiTab) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                currentFrag = App.instance().getTabsManager(filter).currentFragment
                if (currentFrag!!.mUiExtensionsManager != null
                        && currentFrag.mUiExtensionsManager!!.backToNormalState()) {
                    return true
                } else {
                    backToMainActivity()
                    return true
                }
            } else {
                return super.onKeyDown(keyCode, event)
            }
        }

        return if (currentFrag != null && currentFrag!!.mUiExtensionsManager != null && currentFrag!!.mUiExtensionsManager!!.onKeyDown(this, keyCode, event)) true else super.onKeyDown(keyCode, event)
    }

    override fun onFinish() {
        if (App.instance().isMultiTab) {
            val currentFrag = App.instance().getTabsManager(filter).currentFragment
            if (currentFrag != null && !currentFrag.isOpenSuccess) {
                backToMainActivity()
            }
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (App.instance().isMultiTab) {
            App.instance().getMultiTabView(filter).unregisterTabEventListener(mTabEventListener!!)
        }

        mTabEventListener = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        filter = intent.getStringExtra("filter")
        mFragmentManager = App.instance().getTabsManager(filter).fragmentManager
        if (mFragmentManager == null) {
            mFragmentManager = supportFragmentManager
            App.instance().getTabsManager(filter).fragmentManager = mFragmentManager
        }

        setIntent(intent) // import. otherwise, it will get the old intent.

        openDocument(intent)
    }

    private fun openDocument(intent: Intent) {
        filePath = App.instance().getTabsManager(filter).filePath
        val oldPath = filePath
        if (oldPath != null) {
            val oldFragment = App.instance().getTabsManager(filter).fragmentMap[oldPath] as PDFReaderFragment?
            if (oldFragment != null && oldFragment.isOpenSuccess) {
                oldFragment.mUiExtensionsManager!!.stopHideToolbarsTimer()
            }
        }

        filePath = AppFileUtil.getFilePath(this, intent, IHomeModule.FILE_EXTRA)
        App.instance().getTabsManager(filter).filePath = filePath
        val fragment = App.instance().getTabsManager(filter).fragmentMap[filePath!!] as PDFReaderFragment?
        val needReset = oldPath != null && filePath != oldPath && fragment != null && fragment.isOpenSuccess
        if (!App.instance().checkLicense())
            openEmptyView()
        else {
            openDocView(false)
        }

        if (needReset) {
            resetTabView(true)
        }
    }

    private fun resetTabView(needRefresh: Boolean) {
        if (needRefresh) {
            App.instance().getMultiTabView(filter).refreshTopBar(App.instance().getTabsManager(filter).filePath!!)
        }
        val fragment = App.instance().getTabsManager(filter).fragmentMap[App.instance().getTabsManager(filter).filePath] as PDFReaderFragment?
        val h = fragment!!.mUiExtensionsManager!!.mainFrame.topToolbar.contentView.height
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2 * h / 3)
        params.topMargin = -10
        val parent = App.instance().getMultiTabView(filter).tabView!!.parent as ViewGroup?
        parent?.removeView(App.instance().getMultiTabView(filter).tabView)
        fragment.mUiExtensionsManager!!.mainFrame.addSubViewToTopBar(App.instance().getMultiTabView(filter).tabView, 1, params)
    }

    private fun changeViewerState(fragment: PDFReaderFragment) {
        val uiExtensionsManager = fragment.pdfViewCtrl!!.uiExtensionsManager as UIExtensionsManager
        uiExtensionsManager.triggerDismissMenuEvent()
        uiExtensionsManager.documentManager.currentAnnot = null
        uiExtensionsManager.exitPanZoomMode()
        val toolHandler = uiExtensionsManager.currentToolHandler
        if (toolHandler != null) {
            uiExtensionsManager.currentToolHandler = null
            if (toolHandler is SignatureToolHandler) {
                fragment.pdfViewCtrl!!.invalidate()
            }
        }
    }

    fun removeFragment(fragment: BaseFragment) {
        mFragmentManager = App.instance().getTabsManager(filter).fragmentManager
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.remove(fragment).commitAllowingStateLoss()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var currentFrag: BaseFragment? = supportFragmentManager.findFragmentById(R.id.reader_container) as BaseFragment?

        if (App.instance().isMultiTab) {
            currentFrag = App.instance().getTabsManager(filter).currentFragment
            if (currentFrag != null && currentFrag!!.isOpenSuccess) {
                App.instance().getMultiTabView(filter).refreshTopBar(currentFrag.path!!)
            }
        }

        if (currentFrag == null) return
        if (currentFrag!!.mUiExtensionsManager != null) {
            currentFrag.mUiExtensionsManager!!.onConfigurationChanged(this, newConfig)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        var currentFrag: BaseFragment? = supportFragmentManager.findFragmentById(R.id.reader_container) as BaseFragment
        if (App.instance().isMultiTab) {
            currentFrag = App.instance().getTabsManager(filter).currentFragment
        }
        if (currentFrag == null) return
        currentFrag!!.handleActivityResult(requestCode, resultCode, data)
    }
}
