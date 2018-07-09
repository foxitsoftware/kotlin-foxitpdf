/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit Mobile PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window

import com.foxit.App
import com.foxit.pdfreader.PDFReaderActivity
import com.foxit.pdfreader.fragment.BaseFragment
import com.foxit.uiextensions.controls.toolbar.BaseBar
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.modules.connectpdf.account.AccountModule
import com.foxit.uiextensions.utils.AppFileUtil

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mLicenseValid = false
    internal var filter: String = App.FILTER_DEFAULT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLicenseValid = App.instance().checkLicense()
        if (!mLicenseValid)
            return
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (intent != null) {
            filter = intent.action
        }

        initTabsButton(App.instance().getLocalModule(filter), this)

        if (Build.VERSION.SDK_INT >= 23) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }

        App.instance().copyGuideFiles(App.instance().getLocalModule(filter))
        App.instance().getLocalModule(filter).setFileItemEventListener(IHomeModule.onFileItemEventListener { fileExtra, filePath -> onFileSelected(fileExtra, filePath) })
        AccountModule.getInstance().onCreate(this, savedInstanceState)

        val view = App.instance().getLocalModule(filter).getContentView(this.applicationContext)
        val parent = view.parent as ViewGroup?
        parent?.removeView(view)
        setContentView(view)

        handleIntent(intent)
    }

    override fun onDestroy() {
        if (mLicenseValid) {
            App.instance().unloadLocalModule(filter)
            AccountModule.getInstance().onDestroy(this)
        }

        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val path = AppFileUtil.getFilePath(this, intent, IHomeModule.FILE_EXTRA)
            if (path != null) {
                onFileSelected(IHomeModule.FILE_EXTRA, path)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (mLicenseValid && requestCode == REQUEST_EXTERNAL_STORAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            App.instance().copyGuideFiles(App.instance().getLocalModule(filter))
            App.instance().getLocalModule(filter).updateStoragePermissionGranted()
            initTabsButton(App.instance().getLocalModule(filter), this)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun onFileSelected(fileExtra: String, filePath: String) {
        val intent = Intent()
        intent.putExtra(fileExtra, filePath)
        intent.putExtra("filter", filter)
        intent.setClass(this.applicationContext, PDFReaderActivity::class.java)

        if (App.instance().isMultiTab) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        this.startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && App.instance().isMultiTab) {
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_HOME)
            startActivity(launcherIntent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //    BaseItemImpl mSingleMultiBtn = null;
    private fun initTabsButton(localModule: LocalModule, activity: Activity) {
        var singleMultiBtn: BaseItemImpl? = App.instance().getTabsButton(filter) as BaseItemImpl?
        if (singleMultiBtn != null) {
            if (singleMultiBtn.contentView.parent == null) {
                localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB)
            }
            return
        }
        singleMultiBtn = BaseItemImpl(this.applicationContext)
        App.instance().setTabsButton(filter, singleMultiBtn)

        if (App.instance().isMultiTab) {
            singleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector)
            singleMultiBtn.id = R.id.rd_multi_tab
        } else {
            singleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector)
            singleMultiBtn.id = R.id.rd_single_tab
        }

        val finalSingleMultiBtn = singleMultiBtn
        singleMultiBtn.setOnClickListener { v ->
            val stringBuilder = StringBuilder("Do you want to switch to ").append(if (!App.instance().isMultiTab) "tabs" else "single").append(" reader mode?")
            val title = ""
            val dialog = AlertDialog.Builder(activity).setCancelable(true).setTitle(title)
                    .setMessage(stringBuilder.toString())
                    .setPositiveButton("Yes"
                    ) { dialog, which ->
                        if (v.id == R.id.rd_single_tab) {
                            finalSingleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector)
                            finalSingleMultiBtn.id = R.id.rd_multi_tab
                            App.instance().setMultiTabFlag(true)
                        } else if (v.id == R.id.rd_multi_tab) {
                            finalSingleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector)
                            finalSingleMultiBtn.id = R.id.rd_single_tab
                            App.instance().setMultiTabFlag(false)

                            val mFragmentManager = App.instance().getTabsManager(filter).fragmentManager
                            if (mFragmentManager != null) {
                                val fragmentTransaction = mFragmentManager.beginTransaction()
                                for ((_, value) in App.instance().getTabsManager(filter).fragmentMap) {
                                    fragmentTransaction.remove(value)
                                }
                                fragmentTransaction.commitAllowingStateLoss()
                            }
                            App.instance().getTabsManager(filter).currentFragment = null
                            App.instance().getTabsManager(filter).clearFragment()
                        }
                        App.instance().getTabsManager(filter).fragmentManager = null
                        App.instance().getMultiTabView(filter).resetData()

                        dialog.dismiss()
                    }.setNegativeButton("No"
                    ) { dialog, which -> dialog.dismiss() }.create()
            dialog.show()
        }
        localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB)
    }

    companion object {

        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
