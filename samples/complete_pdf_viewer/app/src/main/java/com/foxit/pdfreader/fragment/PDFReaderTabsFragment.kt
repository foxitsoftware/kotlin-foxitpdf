/**
 * Copyright (C) 2003-2023, Foxit Software Inc..
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
package com.foxit.pdfreader.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.foxit.App
import com.foxit.home.HomeFragment
import com.foxit.home.MainActivity
import com.foxit.home.R
import com.foxit.pdfreader.MultiTabView
import com.foxit.pdfreader.MultiTabView.ITabEventListener
import com.foxit.pdfreader.fragment.BaseFragment.IFragmentEvent
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.UIExtensionsManager.OnFinishListener
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.modules.signature.SignatureToolHandler
import com.foxit.uiextensions.pdfreader.config.ReadStateConfig
import com.foxit.uiextensions.utils.AppFileUtil

class PDFReaderTabsFragment : Fragment(), OnFinishListener {
    private var mFragmentManager: FragmentManager? = null
    private var filter: String? = App.FILTER_DEFAULT
    private var filePath: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_reader, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null) {
            filter = requireArguments().getString(HomeFragment.BUNDLE_KEY_FILTER)
        }
        mFragmentManager = requireActivity().supportFragmentManager
        App.instance().getTabsManager(filter!!).fragmentManager = mFragmentManager
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        handleIntent(requireActivity().intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val path = AppFileUtil.getFilePath(
                App.instance().applicationContext,
                intent,
                IHomeModule.FILE_EXTRA
            )
            if (path != null) {
                changeReaderState(MainActivity.READER_STATE_READ)
                openDocument(intent)
            }
        }
    }

    fun onKeyDown(activity: Activity?, keyCode: Int, event: KeyEvent?): Boolean {
        val currentFrag = App.instance().getTabsManager(filter!!).currentFragment
        return if (App.instance().isMultiTab) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                val uiExtensionsManager = currentFrag!!.mUiExtensionsManager
                if (uiExtensionsManager != null
                    && uiExtensionsManager.backToNormalState()
                ) {
                    true
                } else {
                    if (uiExtensionsManager != null && uiExtensionsManager.state == ReadStateConfig.STATE_COMPARE) {
                        val tabView = App.instance().getMultiTabView(filter!!)
                        tabView.removeTab(currentFrag.path)
                        return true
                    }
                    hideFragment(currentFrag)
                    changeReaderState(MainActivity.READER_STATE_HOME)
                    true
                }
            } else {
                false
            }
        } else currentFrag != null && currentFrag.mUiExtensionsManager != null && currentFrag.mUiExtensionsManager!!.onKeyDown(
            getActivity(),
            keyCode,
            event
        )
    }

    override fun onFinish() {
        val currentFrag = App.instance().getTabsManager(filter!!).currentFragment
        if (App.instance().isMultiTab) {
            if (currentFrag != null && !currentFrag.isOpenSuccess) {
                hideFragment(currentFrag)
                changeReaderState(MainActivity.READER_STATE_HOME)
            }
        } else {
            hideFragment(currentFrag)
            App.instance().getTabsManager(filter!!).filePath = null
            changeReaderState(MainActivity.READER_STATE_HOME)
        }
    }

    private fun changeReaderState(state: Int) {
        if (activity != null) (activity as MainActivity?)!!.changeReaderState(state)
    }

    private fun hideFragment(fragment: Fragment?) {
        if (fragment!!.isVisible) {
            val fragmentTransaction = mFragmentManager!!.beginTransaction()
            fragmentTransaction.hide(fragment).commit()
        }
    }

    private fun removeFragment(fragment: BaseFragment?) {
        if (fragment != null) {
            val fragmentTransaction = mFragmentManager!!.beginTransaction()
            fragmentTransaction.remove(fragment).commitAllowingStateLoss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (App.instance().isMultiTab) {
            App.instance().getMultiTabView(filter!!).unregisterTabEventListener(mTabEventListener!!)
        }
        App.instance().onBack()
        mTabEventListener = null
    }

    fun openDocument(intent: Intent?) {
        if (!App.instance().checkLicense()) {
            openEmptyView()
            return
        }
        val oldPath = App.instance().getTabsManager(filter!!).filePath
        filePath = AppFileUtil.getFilePath(
            App.instance().applicationContext,
            intent,
            IHomeModule.FILE_EXTRA
        )
        App.instance().getTabsManager(filter!!).filePath = filePath
        val fragment =
            App.instance().getTabsManager(filter!!).fragmentMap[filePath] as PDFReaderFragment?
        val needReset =
            oldPath != null && filePath != oldPath && fragment != null && fragment.isOpenSuccess
        if (App.instance().isMultiTab) {
            openMultiDocument(false)
        } else {
            openSingleDocument()
        }
        if (needReset) {
            resetTabView(true)
        }
    }

    private fun openSingleDocument() {
        filePath = App.instance().getTabsManager(filter!!).filePath
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        var fragment = mFragmentManager!!.findFragmentByTag(SINGLE_DOC_TAG) as PDFReaderFragment?
        if (fragment == null) {
            fragment = PDFReaderFragment()
            fragment.path = filePath
            fragment.onFinishListener = this
            fragment.filter = filter
        } else {
            fragment.path = filePath
            fragment.onFinishListener = this
            fragment.filter = filter
            fragment.openDocument()
        }
        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.reader_container, fragment, SINGLE_DOC_TAG)
        }
        fragmentTransaction.show(fragment).commitAllowingStateLoss()
        App.instance().getTabsManager(filter!!).addFragment(filePath!!, fragment)
        App.instance().getTabsManager(filter!!).currentFragment = fragment
    }

    private fun openMultiDocument(isRemoveCurFragment: Boolean) {
        App.instance().getMultiTabView(filter!!).registerTabEventListener(mTabEventListener!!)
        filePath = App.instance().getTabsManager(filter!!).filePath
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        var fragment =
            App.instance().getTabsManager(filter!!).fragmentMap[filePath] as PDFReaderFragment?
        if (fragment == null) {
            fragment = PDFReaderFragment()
            App.instance().getTabsManager(filter!!).addFragment(filePath!!, fragment)
            fragment.path = filePath
            fragment.onFinishListener = this
            fragment.filter = filter
        } else {
            if (!fragment.isOpenSuccess) {
                App.instance().getTabsManager(filter!!).removeFragment(filePath!!)
                fragmentTransaction.remove(fragment)
                fragment = PDFReaderFragment()
                App.instance().getTabsManager(filter!!).addFragment(filePath!!, fragment)
                fragment.path = filePath
                fragment.onFinishListener = this
                fragment.filter = filter
            }
        }
        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.reader_container, fragment)
        }
        val currentFragment: Fragment? = App.instance().getTabsManager(filter!!).currentFragment
        if (currentFragment != null && currentFragment != fragment) {
            if (isRemoveCurFragment) {
                fragmentTransaction.remove(currentFragment)
            } else {
                fragmentTransaction.hide(currentFragment)
            }
        }
        fragmentTransaction.show(fragment).commitAllowingStateLoss()
        App.instance().getTabsManager(filter!!).currentFragment = fragment
    }

    /**
     * when App license is valid, it should open a empty view use Fragment also.
     */
    private fun openEmptyView() {
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.reader_container, EmptyViewFragment())
        fragmentTransaction.commitAllowingStateLoss()
    }

    private var mTabEventListener: ITabEventListener? = object : ITabEventListener {
        override fun onTabChanged(
            oldTabInfo: MultiTabView.TabInfo?,
            newTabInfo: MultiTabView.TabInfo?
        ) {
            val fragment = App.instance()
                .getTabsManager(filter!!).fragmentMap[oldTabInfo!!.tabTarget] as PDFReaderFragment?
            changeViewerState(fragment)
            filePath = newTabInfo!!.tabTarget
            App.instance().getTabsManager(filter!!).filePath = filePath
            val newfragment =
                App.instance().getTabsManager(filter!!).fragmentMap[filePath] as PDFReaderFragment?
            newfragment!!.mUiExtensionsManager!!.documentManager.resetActionCallback()
            openMultiDocument(false)
            resetTabView(false)
        }

        override fun onTabRemoved(
            removedTab: MultiTabView.TabInfo?,
            showTab: MultiTabView.TabInfo?
        ) {
            val fragment = App.instance()
                .getTabsManager(filter!!).fragmentMap[removedTab!!.tabTarget] as PDFReaderFragment?
            if (removedTab.tabTarget == App.instance().getTabsManager(filter!!).filePath) {
                changeViewerState(fragment)
                fragment!!.doClose(object : IFragmentEvent {
                    override fun onRemove() {
                        App.instance().getMultiTabView(filter!!).removeTab(removedTab)
                        if (showTab != null) {
                            filePath = showTab.tabTarget
                            App.instance().getTabsManager(filter!!).filePath = filePath
                            val newfragment = App.instance()
                                .getTabsManager(filter!!).fragmentMap[filePath] as PDFReaderFragment?
                            newfragment!!.mUiExtensionsManager!!.documentManager.resetActionCallback()
                            openMultiDocument(true)
                            resetTabView(false)
                            App.instance()
                                .getTabsManager(filter!!).fragmentMap.remove(removedTab.tabTarget)
                            App.instance().getMultiTabView(filter!!)
                                .refreshTopBar(showTab.tabTarget)
                        } else {
                            App.instance().getTabsManager(filter!!).filePath = null

                            // only one tab
                            removeFragment(fragment)
                            App.instance().getTabsManager(filter!!).currentFragment = null
                            App.instance().getTabsManager(filter!!).clearFragment()
                            changeReaderState(MainActivity.READER_STATE_HOME)
                        }
                    }
                })
            } else {
                fragment!!.doClose(object : IFragmentEvent {
                    override fun onRemove() {
                        App.instance().getMultiTabView(filter!!).removeTab(removedTab)
                        removeFragment(fragment)
                        App.instance()
                            .getTabsManager(filter!!).fragmentMap.remove(removedTab.tabTarget)
                        App.instance().getMultiTabView(filter!!).refreshTopBar(showTab!!.tabTarget)
                    }
                })
            }
        }
    }

    private fun resetTabView(needRefresh: Boolean) {
        if (needRefresh) {
            App.instance().getMultiTabView(filter!!)
                .refreshTopBar(App.instance().getTabsManager(filter!!).filePath)
        }
        val fragment = App.instance().getTabsManager(filter!!).fragmentMap[App.instance()
            .getTabsManager(filter!!).filePath] as PDFReaderFragment?
        val h = fragment!!.mUiExtensionsManager!!.mainFrame.topToolbar.contentView.height
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2 * h / 3)
        params.topMargin = -10
        val parent = App.instance().getMultiTabView(filter!!).tabView!!.parent as ViewGroup
        parent?.removeView(App.instance().getMultiTabView(filter!!).tabView)
        fragment.mUiExtensionsManager!!.mainFrame.topActionView.addView(
            App.instance().getMultiTabView(filter!!).tabView, params
        )
    }

    private fun changeViewerState(fragment: PDFReaderFragment?) {
        val uiExtensionsManager =
            fragment!!.pdfViewCtrl!!.uiExtensionsManager as UIExtensionsManager
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val activity = activity ?: return
        val currentFrag = App.instance().getTabsManager(filter!!).currentFragment
        if (App.instance().isMultiTab) {
            if (currentFrag != null && currentFrag.isOpenSuccess) {
                App.instance().getMultiTabView(filter!!).refreshTopBar(currentFrag.path)
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentFrag = App.instance().getTabsManager(filter!!).currentFragment ?: return
        currentFrag.handleActivityResult(requestCode, resultCode, data)
    }

    fun handleRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currentFrag = App.instance().getTabsManager(filter!!).currentFragment ?: return
        currentFrag.handleRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val FRAGMENT_NAME = "READER_FRAGMENT"
        const val SINGLE_DOC_TAG = "SINGLE_DOC_TAG"
        fun newInstance(filter: String?): PDFReaderTabsFragment {
            val fragment = PDFReaderTabsFragment()
            val args = Bundle()
            args.putString(HomeFragment.BUNDLE_KEY_FILTER, filter)
            fragment.arguments = args
            return fragment
        }
    }
}