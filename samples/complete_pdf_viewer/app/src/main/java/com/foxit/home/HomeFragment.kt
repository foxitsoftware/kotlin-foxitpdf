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
package com.foxit.home


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import com.foxit.App
import com.foxit.pdfscan.IPDFScanManagerListener
import com.foxit.pdfscan.PDFScanManager
import com.foxit.uiextensions.controls.dialog.AppDialogManager
import com.foxit.uiextensions.controls.toolbar.BaseBar
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.home.IHomeModule.onFileItemEventListener
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.utils.AppResource
import com.foxit.uiextensions.utils.AppUtil
import com.foxit.uiextensions.utils.UIToast


class HomeFragment : Fragment() {

    private var mRootView: ViewGroup? = null
    private var mOnFileItemEventListener: IHomeModule.onFileItemEventListener? = null
    private var mScanListener: IPDFScanManagerListener? = null

    private var filter: String? = App.FILTER_DEFAULT

    internal var mFinishEditListener: LocalModule.IFinishEditListener = LocalModule.IFinishEditListener {
        if (filter?.let { App.instance().getLocalModule(it) } != null) {
            initTabsButton(App.instance().getLocalModule(filter!!), activity)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is onFileItemEventListener) mOnFileItemEventListener = context
        if (context is IPDFScanManagerListener) mScanListener = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments != null) {
            filter = arguments!!.getString(BUNDLE_KEY_FILTER)
        }
        App.instance().getLocalModule(filter!!).setAttachedActivity(activity)
        App.instance().copyGuideFiles(App.instance().getLocalModule(filter!!))
        App.instance().getLocalModule(filter!!).setFileItemEventListener { fileExtra, filePath ->
            if (mOnFileItemEventListener != null) {
                mOnFileItemEventListener!!.onFileItemClicked(fileExtra, filePath)
            }
        }

        App.instance().getLocalModule(filter!!).setCompareListener { state, filePath ->
            val compareListener = activity as LocalModule.ICompareListener?
            compareListener?.onCompareClicked(state, filePath)
        }

        val view = App.instance().getLocalModule(filter!!).getContentView(App.instance().applicationContext)
        val parent = view.parent as ViewGroup?
        parent?.removeView(view)
        mRootView = RelativeLayout(App.instance().applicationContext)
        mRootView!!.addView(view, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.MATCH_PARENT))

        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        val ivScan = ImageView(getContext())
        ivScan.setImageResource(R.drawable.fx_floatbutton_scan);
        ivScan.setOnClickListener{
            if (!PDFScanManager.isInitializeScanner()) {
                val framework1: Long = 0
                val framework2: Long = 0
                PDFScanManager.initializeScanner(activity!!.application, framework1, framework2)
            }
            if (!PDFScanManager.isInitializeCompression()) {
                val compression1: Long = 0
                val compression2: Long = 0
                PDFScanManager.initializeCompression(activity!!.application, compression1, compression2)
            }
            if (PDFScanManager.isInitializeScanner() && PDFScanManager.isInitializeCompression()) {
                showScannerList();
            } else {
                UIToast.getInstance(App.instance().applicationContext)
                        .show(AppResource.getString(App.instance().applicationContext,R.string.rv_invalid_license));
            }

        }
        PDFScanManager.registerManagerListener(mManagerListener);

        layoutParams.bottomMargin = 80;
        layoutParams.rightMargin = 50;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivScan.setLayoutParams(layoutParams);
        mRootView!!.addView(ivScan);

        return mRootView
    }

    private var mScannerList: DialogFragment? = null
    private fun showScannerList() {
        val fragmentManager = activity!!.supportFragmentManager
        mScannerList = fragmentManager.findFragmentByTag("ScannerList") as DialogFragment?
        if (mScannerList == null) mScannerList = PDFScanManager.createScannerFragment(null)
        AppDialogManager.getInstance().showAllowManager(mScannerList, fragmentManager, "ScannerList", null)
    }

    private fun dismissScannerList() {
        AppDialogManager.getInstance().dismiss(mScannerList)
    }

    private val mManagerListener = IPDFScanManagerListener { errorCode, path ->
        if (mScanListener != null) {
            dismissScannerList()
            updateThumbnail(path)
            mScanListener!!.onDocumentAdded(errorCode, path)
        }
    }

    private fun updateThumbnail(path: String) {
        if (!AppUtil.isEmpty(path)) {
            App.instance().getLocalModule(filter!!).updateThumbnail(path)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_EXTERNAL_STORAGE) {
            App.instance().copyGuideFiles(App.instance().getLocalModule(filter!!))
            App.instance().getLocalModule(filter!!).updateStoragePermissionGranted()
            initTabsButton(App.instance().getLocalModule(filter!!), activity)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.instance().setTabsButton(filter!!, null)
        initTabsButton(App.instance().getLocalModule(filter!!), activity)
        App.instance().getLocalModule(filter!!).registerFinishEditListener(mFinishEditListener)
    }

    override fun onDestroy() {
        App.instance().getLocalModule(filter!!).unregisterFinishEditListener(mFinishEditListener)
        App.instance().unloadLocalModule(filter!!)
        PDFScanManager.unregisterManagerListener(mManagerListener)
        super.onDestroy()
    }

    private fun initTabsButton(localModule: LocalModule, activity: Activity?) {
        var singleMultiBtn: BaseItemImpl? = App.instance().getTabsButton(filter!!) as BaseItemImpl?
        if (singleMultiBtn != null) {
            if (singleMultiBtn.contentView.parent == null) {
                localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB)
            }
            return
        }
        singleMultiBtn = BaseItemImpl(App.instance().applicationContext)
        App.instance().setTabsButton(filter!!, singleMultiBtn)

        if (App.instance().isMultiTab) {
            singleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector)
            singleMultiBtn.id = R.id.rd_multi_tab
        } else {
            singleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector)
            singleMultiBtn.id = R.id.rd_single_tab
        }

        val finalSingleMultiBtn = singleMultiBtn
        singleMultiBtn.setOnClickListener { v ->
            val readerMode = if (!App.instance().isMultiTab) getString(R.string.fx_tabs_reader_mode) else getString(R.string.fx_single_reader_mode)
            val msg = getString(R.string.fx_swith_reader_mode_toast, readerMode)
            val title = ""
            val dialog = AlertDialog.Builder(activity).setCancelable(true).setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(getString(R.string.fx_string_yes),
                            DialogInterface.OnClickListener { dialog, which ->
                                if (v.id == R.id.rd_single_tab) {
                                    finalSingleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector)
                                    finalSingleMultiBtn.id = R.id.rd_multi_tab
                                    App.instance().setMultiTabFlag(true)
                                } else if (v.id == R.id.rd_multi_tab) {
                                    finalSingleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector)
                                    finalSingleMultiBtn.id = R.id.rd_single_tab
                                    App.instance().setMultiTabFlag(false)
                                }
                                val mFragmentManager = App.instance().getTabsManager(filter!!).fragmentManager
                                if (mFragmentManager != null) {
                                    val fragmentTransaction = mFragmentManager.beginTransaction()
                                    for ((_, value) in App.instance().getTabsManager(filter!!).fragmentMap) {
                                        fragmentTransaction.remove(value)
                                    }
                                    fragmentTransaction.commitAllowingStateLoss()
                                }

                                App.instance().getTabsManager(filter!!).currentFragment = (null)
                                App.instance().getTabsManager(filter!!).clearFragment()
                                App.instance().getMultiTabView(filter!!).resetData()

                                dialog.dismiss()
                            }).setNegativeButton(getString(R.string.fx_string_no),
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }).create()
            dialog.show()
        }
        localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        var animation: TranslateAnimation? = null
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            } else {
                animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            }
        } else if (FragmentTransaction.TRANSIT_FRAGMENT_CLOSE == transit) {
            if (enter) {
                animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            } else {
                animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            }
        }
        if (animation == null) {
            animation = TranslateAnimation(0f, 0f, 0f, 0f)
        }
        animation.duration = 300
        return animation
    }

    fun onKeyDown(activity: Activity, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && App.instance().isMultiTab) {
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_HOME)
            startActivity(launcherIntent)
            return true
        }
        App.instance().onBack()
        return false
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        App.instance().getLocalModule(filter!!).onConfigurationChanged(newConfig)
    }

    companion object {
        val FRAGMENT_NAME = "HOME_FRAGMENT"
        val BUNDLE_KEY_FILTER = "key_filter"

        fun newInstance(filter: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(BUNDLE_KEY_FILTER, filter)
            fragment.arguments = args
            return fragment
        }
    }
}
