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
package com.foxit.pdf.pdfviewer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager

import com.foxit.sdk.PDFViewCtrl
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import com.foxit.uiextensions.Module
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.annots.note.NoteModule
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule
import com.foxit.uiextensions.modules.*
import com.foxit.uiextensions.modules.connectpdf.account.AccountModule
import com.foxit.uiextensions.modules.doc.docinfo.DocInfoModule
import com.foxit.uiextensions.modules.panel.annot.AnnotPanelModule
import com.foxit.uiextensions.modules.thumbnail.ThumbnailModule
import com.foxit.uiextensions.utils.UIToast

class MainActivity : FragmentActivity() {

    private var pdfViewCtrl: PDFViewCtrl? = null
    private var uiExtensionsManager: UIExtensionsManager? = null
    private var searchModule: SearchModule? = null
    private var docInfoModule: DocInfoModule? = null
    private var noteModule: NoteModule? = null
    private var highlightModule: HighlightModule? = null
    private var underlineModule: UnderlineModule? = null
    private var strikeoutModule: StrikeoutModule? = null
    private var squigglyModule: SquigglyModule? = null

    private var annotPanelModule: AnnotPanelModule? = null
    private var outlineModule: OutlineModule? = null
    private var thumbnailModule: ThumbnailModule? = null

    private var pageNavigationModule: PageNavigationModule? = null

    private var isUnlock = false
    private var mContext: Context? = null
    private var mActionMode: ActionMode? = null

    private val storageDirectory: String?
        get() {
            var path: String? = null
            val sdExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (sdExist) {
                path = Environment.getExternalStorageDirectory().absolutePath + "/"
            }
            return path
        }

    private val mActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.main, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (!isUnlock) {
                UIToast.getInstance(applicationContext).show(getString(R.string.fx_unlock_library_failed))
                return false
            }

            val itemId = item.itemId
            if (itemId == R.id.Note || itemId == R.id.Highlight
                    || itemId == R.id.Squiggly || itemId == R.id.Underline
                    || itemId == R.id.StrikeOut) {
                if (!uiExtensionsManager!!.canAddAnnot()) {
                    UIToast.getInstance(applicationContext).show(getString(R.string.fx_the_document_cannot_modify))
                    return false
                }
            }

            when (itemId) {
                R.id.Outline -> {
                    if (outlineModule != null)
                        outlineModule!!.show()
                }
                R.id.ChangeLayout -> {
                    pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                    pdfViewCtrl!!.isContinuous = !pdfViewCtrl!!.isContinuous
                }
                R.id.Search -> {
                    if (searchModule == null) {

                        searchModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SEARCH) as SearchModule
                        if (searchModule == null) {
                            searchModule = SearchModule(mContext!!, uiExtensionsManager!!.rootView, pdfViewCtrl!!, uiExtensionsManager)
                            searchModule!!.loadModule()
                        }
                    }
                    val searchView = searchModule!!.searchView
                    searchView.show()
                }
                R.id.Note -> {
                    if (noteModule == null) {
                        noteModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_NOTE) as NoteModule
                    }
                    uiExtensionsManager!!.currentToolHandler = noteModule!!.toolHandler
                }
                R.id.DocInfo -> {
                    if (docInfoModule == null) {
                        docInfoModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_DOCINFO) as DocInfoModule
                    }

                    val docInfoView = docInfoModule!!.view
                    docInfoView?.show()
                }
                R.id.Highlight -> {
                    if (highlightModule == null)
                        highlightModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_HIGHLIGHT) as HighlightModule
                    uiExtensionsManager!!.currentToolHandler = highlightModule!!.toolHandler
                }
                R.id.Underline -> {
                    if (underlineModule == null) {
                        underlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_UNDERLINE) as UnderlineModule
                    }
                    uiExtensionsManager!!.currentToolHandler = underlineModule!!.toolHandler
                }
                R.id.StrikeOut -> {
                    if (strikeoutModule == null) {
                        strikeoutModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_STRIKEOUT) as StrikeoutModule
                    }
                    uiExtensionsManager!!.currentToolHandler = strikeoutModule!!.toolHandler
                }
                R.id.Squiggly -> {
                    if (squigglyModule == null) {
                        squigglyModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SQUIGGLY) as SquigglyModule
                    }
                    uiExtensionsManager!!.currentToolHandler = squigglyModule!!.toolHandler
                }
                R.id.Annotations -> {
                    if (annotPanelModule != null) {
                        annotPanelModule!!.show()
                    }
                }
                R.id.Thumbnail -> {
                    if (thumbnailModule != null) {
                        thumbnailModule!!.show()
                    }
                }
            }
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val errorCode = Library.initialize(sn, key)
        isUnlock = true

        if (errorCode != Constants.e_ErrSuccess) {
            isUnlock = false

            val errorMsg = if (errorCode == Constants.e_ErrInvalidLicense) getString(R.string.fx_the_license_is_invalid) else getString(R.string.fx_failed_to_initialize_the_library)
            UIToast.getInstance(applicationContext).show(errorMsg)
            return
        }

        pdfViewCtrl = PDFViewCtrl(this.applicationContext)
        pdfViewCtrl!!.attachedActivity = this
        pdfViewCtrl!!.registerDoubleTapEventListener(object : PDFViewCtrl.IDoubleTapEventListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (pageNavigationModule == null)
                    pageNavigationModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_PAGENAV) as PageNavigationModule?

                if (mActionMode == null) {
                    mActionMode = (mContext as Activity).startActionMode(mActionModeCallback)
                    pageNavigationModule!!.changPageNumberState(true)
                } else {
                    mActionMode!!.finish()
                    mActionMode = null
                    pageNavigationModule!!.changPageNumberState(false)
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return false
            }
        })

        uiExtensionsManager = UIExtensionsManager(this.applicationContext, pdfViewCtrl!!)
        uiExtensionsManager!!.enableBottomToolbar(false)
        uiExtensionsManager!!.enableTopToolbar(false)
        uiExtensionsManager!!.attachedActivity = this
        uiExtensionsManager!!.onCreate(this, pdfViewCtrl, savedInstanceState)
        pdfViewCtrl!!.uiExtensionsManager = uiExtensionsManager!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            } else {
                openDocument()
            }
        } else {
            openDocument()
        }

        AccountModule.getInstance().onCreate(this, savedInstanceState)

        outlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_OUTLINE) as OutlineModule
        if (outlineModule == null) {
            outlineModule = OutlineModule(this, uiExtensionsManager!!.rootView, pdfViewCtrl, uiExtensionsManager)
            outlineModule!!.loadModule()
        }
        annotPanelModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_ANNOTPANEL) as AnnotPanelModule
        if (annotPanelModule == null) {
            annotPanelModule = AnnotPanelModule(this.applicationContext, pdfViewCtrl!!, uiExtensionsManager)
            annotPanelModule!!.loadModule()
        }
        thumbnailModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_THUMBNAIL) as ThumbnailModule
        if (thumbnailModule == null) {
            thumbnailModule = ThumbnailModule(this.applicationContext, pdfViewCtrl, uiExtensionsManager)
            thumbnailModule!!.loadModule()
        }

        if (Build.VERSION.SDK_INT >= 24) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }

        setContentView(uiExtensionsManager!!.contentView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openDocument()
        } else {
            UIToast.getInstance(applicationContext).show(getString(R.string.fx_permission_denied))
            finish()
        }
    }

    private fun openDocument() {
        // Note: Here, filePath will be set with the total path of file.
        val sdcardPath = storageDirectory
        val filePath = sdcardPath!! + "FoxitSDK/Sample.pdf"
        uiExtensionsManager!!.openDocument(filePath, null)
    }

    public override fun onStart() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onStart(this)
        }
        super.onStart()
    }

    public override fun onStop() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onStop(this)
        }
        super.onStop()
    }

    public override fun onPause() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onPause(this)
        }
        super.onPause()
    }

    public override fun onResume() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onResume(this)
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onDestroy(this)
        }
        AccountModule.getInstance().onDestroy(this)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onConfigurationChanged(this, newConfig)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (uiExtensionsManager != null && uiExtensionsManager!!.onKeyDown(this, keyCode, event)) true else super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pdfViewCtrl!!.handleActivityResult(requestCode, resultCode, data);
    }

    companion object {

        private val sn = "BW3CdOL9RzR/4rAyb2+Ze4s5sP8katpRwE+DOEB3k2rQZtFD5HJG5Q=="
        private val key = "ezJvj93HvBh39LusL0W0ja4g+PQUKROlWYrFaqfVeOP9qnxzhL01J+KHAmohNqVQ+DF+mvMHx0afiHvfYfpythLfEBEraoB6ODghaWXcBpU1+RmsaTiEau5SSVXbEg1tGbao3l6g7DLOSO3p4qazFrs/TvSy39FzXlmnGAGYf5vY3S4eKTqaRBYgzlJ526WedxUbq1BYK8+QZYjA6GPncrqOGH5OAW6Pz0NpDLpoJG+ZHeDTdOR9QP05XiVwBWW6ol+/hO7lHNGNu20rXf1GpMxkDLscPoFG4+N9kLtThf2Z4KCEtOphI7v4Zb92eSOdT0LEoCv/NgCvbXzwHzbSvE6MqM+s6IWYN/KBVXqZQIcfQatk3+KT2EP2RUXb9QBDBD1rDa8b0YD8HJ9QF4Ip/oN7aiu9kaD7Ih9+oVv40WbIllNZVbtreEpw0fBGb9OsS1RFrGl33JbgO6MgPQSTziyTE6VaFvjFjVgsuQUeaRfAF+x51hTKqEpQCxR9RQWF/SL9DcWhpOc5gO7JfWv8ZXYh41TthV8TBmg+2MTtUHY7Jbug+lAsMZd8qTpcviwYGzbAroqbinxaxOprGK7sGnRKHw79JYhKKjpvli6xpaaXw6aggjEaC/DQ633pmWrETK2aWXBRBrfnjHRky7urcjkuzg6TIDjxQ9Gl4mV0Ue8V/I4FXmqSBd3km6p9yn7oMgrcxXtbznW547+uyE11h12exYlwlkGCFf/tHtH39l7LJ6wIJDY67arTMmafyIAEYDKABvje7oYkUZzFUM4Uei7My/Nx4aAjTNFWuIZrJUAfVa0EbSeOjhJfxi8tIDVmF0lDQfj9AKKu686SzzosAw97LO62iA+00fysyxby10xLTITnGb/wuwIElKXo3e+6rN3q+7hfj85iM7csgvD9im2JHTIZj6TV9xN0MsevzHzqEO2VMhdukPkdDp5EiGhDsPn4KS4oLekRL148IhN21oEA03tb/WVvWmkFzMIxsspvpg+HcBiwPYm6ahvcSf83fJjmRb+Gk0LOTMYLCXqRiGKQSOVD12X9Uu2VueKnIKoPRo46it0EJAR/YKlWrZY0DlICsLRsYXDg/lmOS2BFu+nyPRi9V9ND0WxjqbNWieEJmf4wz1TR3VTL1BSBcqBq+SXVXLnuDjbuzulrjtIXS3UG5VPpwAX+/wVN3F5gUklRr5DJ7KiG7MM997QsTQpMwPe++zlVDmucBJ24HzuIhVw="


        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
