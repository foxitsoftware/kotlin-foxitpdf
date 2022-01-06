/**
 * Copyright (C) 2003-2022, Foxit Software Inc..
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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.foxit.App
import com.foxit.home.HomeFragment.Companion.newInstance
import com.foxit.pdfreader.fragment.PDFReaderTabsFragment
import com.foxit.pdfscan.IPDFScanManagerListener
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.home.IHomeModule.onFileItemEventListener
import com.foxit.uiextensions.home.local.LocalModule.ICompareListener
import com.foxit.uiextensions.theme.ThemeConfig
import com.foxit.uiextensions.utils.*

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback, onFileItemEventListener, ICompareListener, IPDFScanManagerListener {
    private var mReaderState = READER_STATE_HOME
    private var mLicenseValid = false
    private var filter: String? = App.FILTER_DEFAULT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLicenseValid = App.instance().checkLicense()
        if (!mLicenseValid) {
            return
        }
        if (!AppDevice.isChromeOs(this))
            AppTheme.setThemeFullScreen(this)
        AppTheme.setThemeNeedMenuKey(this)
        setContentView(R.layout.activity_reader)
        if (Build.VERSION.SDK_INT >= 30 && !AppFileUtil.isExternalStorageLegacy()) {
            val storageManager = AppStorageManager.getInstance(this)
            val needPermission = storageManager.needManageExternalStoragePermission()
            if (!AppStorageManager.isExternalStorageManager() && needPermission) {
                storageManager.requestExternalStorageManager(this, REQUEST_EXTERNAL_STORAGE_MANAGER)
            } else if (!needPermission) {
                checkStorageState()
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            checkStorageState()
        }
        val intent = intent
        if (intent != null) {
            filter = intent.action
        }
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        var homeFragment = getHomeFragment(fm)
        var readerFragment = getReaderFragment(fm)
        if (homeFragment == null) {
            homeFragment = newInstance(filter)
            AppStorageManager.setOpenTreeRequestCode(HomeFragment.REQUEST_OPEN_DOCUMENT_TREE)
            ft.add(R.id.reader_container, homeFragment, HomeFragment.FRAGMENT_NAME)
        }
        if (readerFragment == null) {
            readerFragment = PDFReaderTabsFragment.newInstance(filter)
            ft.add(R.id.reader_container, readerFragment, PDFReaderTabsFragment.FRAGMENT_NAME)
        }
        if (mReaderState == READER_STATE_HOME) {
            ft.hide(readerFragment!!)
            ft.show(homeFragment)
        } else {
            ft.hide(homeFragment)
            ft.show(readerFragment!!)
        }
        ft.commit()
        SystemUiHelper.getInstance()
            .setStatusBarColor(window, ThemeConfig.getInstance(this).primaryColor)
    }

    fun checkStorageState() {
        val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        } else {
            App.instance().selectDefaultFolderOrNot(this)
        }
    }

    private fun getHomeFragment(fm: FragmentManager): HomeFragment? {
        val fragment = fm.findFragmentByTag(HomeFragment.FRAGMENT_NAME)
        return if (fragment != null) {
            fragment as HomeFragment?
        } else null
    }

    private fun getReaderFragment(fm: FragmentManager): PDFReaderTabsFragment? {
        val fragment = fm.findFragmentByTag(PDFReaderTabsFragment.FRAGMENT_NAME)
        return if (fragment != null) {
            fragment as PDFReaderTabsFragment?
        } else null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!mLicenseValid) {
            return
        }
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val path = AppFileUtil.getFilePath(App.instance().applicationContext, intent, IHomeModule.FILE_EXTRA)
            if (path != null) {
                onFileItemClicked(IHomeModule.FILE_EXTRA, path)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (mLicenseValid && requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (verifyPermissions(grantResults)) {
                val fragment = supportFragmentManager.findFragmentByTag(HomeFragment.FRAGMENT_NAME)
                (fragment as? HomeFragment)?.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun verifyPermissions(grantResults: IntArray): Boolean {
        if (grantResults.size < 1) {
            return false
        }
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onFileItemClicked(fileExtra: String, filePath: String) {
        mReaderState = READER_STATE_READ
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val homeFragment = getHomeFragment(fm)
        val readerFragment = getReaderFragment(fm)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        val intent = Intent()
        intent.putExtra(fileExtra, filePath)
        intent.putExtra(HomeFragment.BUNDLE_KEY_FILTER, filter)
        if (homeFragment != null) {
            ft.hide(homeFragment)
        }
        if (readerFragment != null) {
            readerFragment.openDocument(intent)
            ft.show(readerFragment)
        }
        ft.commitAllowingStateLoss()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val fm = supportFragmentManager
        if (mReaderState == READER_STATE_HOME) {
            val homeFragment = getHomeFragment(fm)
            if (homeFragment != null && homeFragment.onKeyDown(this, keyCode, event)) {
                return true
            }
        } else {
            val readerFragment = getReaderFragment(fm)
            if (readerFragment != null && readerFragment.onKeyDown(this, keyCode, event)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun changeReaderState(state: Int) {
        showSystemUI()
        mReaderState = state
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val homeFragment = getHomeFragment(fm)
        val readerFragment = getReaderFragment(fm)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        if (mReaderState == READER_STATE_HOME) {
            if (readerFragment != null) {
                ft.hide(readerFragment)
            }
            if (homeFragment != null) {
                ft.show(homeFragment)
            }
        } else {
            if (homeFragment != null) {
                ft.hide(homeFragment)
            }
            if (readerFragment != null) {
                ft.show(readerFragment)
            }
        }
        ft.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fm = supportFragmentManager
        val fragment = getHomeFragment(fm)
        fragment?.handleActivityResult(requestCode, resultCode, data)
        val readerFragment = getReaderFragment(fm)
        readerFragment?.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onCompareClicked(state: Int, filePath: String) {
        if (state == ICompareListener.STATE_SUCCESS) {
            onFileItemClicked(IHomeModule.FILE_EXTRA, filePath)
        }
    }

    override fun onDocumentAdded(errorCode: Int, path: String) {
        if (errorCode == IPDFScanManagerListener.e_ErrSuccess) {
            onFileItemClicked(IHomeModule.FILE_EXTRA, path)
        }
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }

    companion object {
        const val REQUEST_EXTERNAL_STORAGE_MANAGER = 111
        const val REQUEST_EXTERNAL_STORAGE = 222
        const val READER_STATE_HOME = 1
        const val READER_STATE_READ = 2
        private val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}