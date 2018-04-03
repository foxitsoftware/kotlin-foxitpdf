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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window

import com.foxit.App
import com.foxit.pdfreader.PDFReaderActivity
import com.foxit.uiextensions.home.IHomeModule

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mLicenseValid = false
    private var mOnFileItemEventListener: IHomeModule.onFileItemEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLicenseValid = App.instance().checkLicense()
        if (!mLicenseValid)
            return
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (Build.VERSION.SDK_INT >= 23) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }

        App.instance().localModule?.setFileItemEventListener(IHomeModule.onFileItemEventListener { fileExtra, filePath -> onFileSelected(fileExtra, filePath) })


        setContentView(App.instance().localModule?.getContentView(this.applicationContext))
    }

    override fun onDestroy() {
        if (mLicenseValid) {
            mOnFileItemEventListener = null
            App.instance().onDestroy()
        }

        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (mLicenseValid && requestCode == REQUEST_EXTERNAL_STORAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            App.instance().localModule?.updateStoragePermissionGranted()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun onFileSelected(fileExtra: String, filePath: String) {
        val intent = Intent()
        intent.putExtra(fileExtra, filePath)
        intent.setClass(this.applicationContext, PDFReaderActivity::class.java!!)
        this.startActivity(intent)
    }

    companion object {

        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
