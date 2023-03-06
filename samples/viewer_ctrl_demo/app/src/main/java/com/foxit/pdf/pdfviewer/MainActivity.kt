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
package com.foxit.pdf.pdfviewer

import android.Manifest
import androidx.fragment.app.FragmentActivity
import com.foxit.sdk.PDFViewCtrl
import com.foxit.uiextensions.modules.SearchModule
import com.foxit.uiextensions.modules.doc.docinfo.DocInfoModule
import com.foxit.uiextensions.annots.note.NoteModule
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule
import com.foxit.uiextensions.modules.PageNavigationModule
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule
import com.foxit.uiextensions.modules.panel.annot.AnnotPanelModule
import com.foxit.uiextensions.modules.panel.outline.OutlineModule
import com.foxit.uiextensions.modules.thumbnail.ThumbnailModule
import android.os.Bundle
import com.foxit.sdk.common.Library
import com.foxit.pdf.pdfviewer.MainActivity
import com.foxit.pdf.pdfviewer.R
import com.foxit.uiextensions.utils.UIToast
import com.foxit.sdk.PDFViewCtrl.IDoubleTapEventListener
import com.foxit.uiextensions.pdfreader.config.ReadStateConfig
import com.foxit.uiextensions.Module
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.StrictMode.VmPolicy
import android.os.StrictMode
import com.foxit.uiextensions.utils.thread.AppThreadManager
import java.lang.Runnable
import com.foxit.uiextensions.controls.panel.PanelSpec
import com.foxit.uiextensions.modules.doc.docinfo.DocInfoView
import android.content.Intent
import com.foxit.uiextensions.pdfreader.IStateChangeListener
import com.foxit.uiextensions.UIExtensionsManager.MenuEventListener
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import android.widget.RelativeLayout
import com.foxit.sdk.common.Constants
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.utils.AppDisplay

class MainActivity : FragmentActivity() {
    private var pdfViewCtrl: PDFViewCtrl? = null
    private var uiExtensionsManager: UIExtensionsManager? = null
    private var searchModule: SearchModule? = null
    private var docInfoModule: DocInfoModule? = null
    private var noteModule: NoteModule? = null
    private var highlightModule: HighlightModule? = null
    private var pageNavigationModule: PageNavigationModule? = null
    private var underlineModule: UnderlineModule? = null
    private var strikeoutModule: StrikeoutModule? = null
    private var squigglyModule: SquigglyModule? = null
    private var annotPanelModule: AnnotPanelModule? = null
    private var outlineModule: OutlineModule? = null
    private var thumbnailModule: ThumbnailModule? = null
    private var isUnlock = false
    private var mContext: Context? = null
    private var mActionMode: ActionMode? = null
    private val storageDirectory: String?
        private get() {
            var path: String? = null
            val sdExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (sdExist) {
                path = Environment.getExternalStorageDirectory().absolutePath + "/"
            }
            return path
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val errorCode = Library.initialize(sn, key)
        isUnlock = true
        if (errorCode != Constants.e_ErrSuccess) {
            isUnlock = false
            val errorMsg =
                if (errorCode == Constants.e_ErrInvalidLicense) getString(R.string.fx_the_license_is_invalid) else getString(
                    R.string.fx_failed_to_initialize_the_library
                )
            UIToast.getInstance(applicationContext).show(errorMsg)
            return
        }
        pdfViewCtrl = PDFViewCtrl(this.applicationContext)
        pdfViewCtrl!!.attachedActivity = this
        pdfViewCtrl!!.registerDoubleTapEventListener(object : IDoubleTapEventListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (uiExtensionsManager!!.state != ReadStateConfig.STATE_NORMAL) {
                    return false
                }
                if (pageNavigationModule == null) pageNavigationModule =
                    uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_PAGENAV) as PageNavigationModule
                if (mActionMode == null) {
                    createActionMode()
                    if (pageNavigationModule != null)
                        pageNavigationModule!!.changPageNumberState(true)
                } else {
                    mActionMode!!.finish()
                    if (pageNavigationModule != null)
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
        uiExtensionsManager = UIExtensionsManager(this.applicationContext, pdfViewCtrl)
        uiExtensionsManager!!.enableBottomToolbar(false)
        uiExtensionsManager!!.enableTopToolbar(false)
        uiExtensionsManager!!.config.uiSettings.fullscreen = false
        uiExtensionsManager!!.isContinueAddAnnot = false
        uiExtensionsManager!!.attachedActivity = this
        uiExtensionsManager!!.onCreate(this, pdfViewCtrl, savedInstanceState)
        pdfViewCtrl!!.uiExtensionsManager = uiExtensionsManager
        uiExtensionsManager!!.registerMenuEventListener(mMenuEventListener)
        uiExtensionsManager!!.registerStateChangeListener(mStateChangeListener)

        thumbnailModule =
            uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_THUMBNAIL) as ThumbnailModule
        if (thumbnailModule == null) {
            thumbnailModule = ThumbnailModule(mContext, pdfViewCtrl, uiExtensionsManager)
            thumbnailModule!!.loadModule()
        }
        if (Build.VERSION.SDK_INT >= 24) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
        val panelView = uiExtensionsManager!!.rootView.findViewById<View>(R.id.read_panel_view_ly)
        if (AppDisplay.isPad() && panelView != null && panelView.layoutParams is RelativeLayout.LayoutParams) {
            (panelView.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.BELOW)
            panelView.layoutParams = panelView.layoutParams
        }
        setContentView(uiExtensionsManager!!.contentView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                startActivityForResult(intent, REQUEST_ALL_FILES_ACCESS_PERMISSION)
                return
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
                return
            }
        }
        openDocument()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openDocument()
            } else {
                UIToast.getInstance(applicationContext)
                    .show(getString(R.string.fx_permission_denied))
                finish()
            }
        }
    }

    private fun openDocument() {
        // Note: Here, filePath will be set with the total path of file.
        AppThreadManager.getInstance().runOnUiThread {
            val sdcardPath = storageDirectory
            val filePath = sdcardPath + "FoxitSDK/Sample.pdf"
            uiExtensionsManager!!.openDocument(filePath, null)
        }
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
            uiExtensionsManager!!.unregisterMenuEventListener(mMenuEventListener)
            uiExtensionsManager!!.unregisterStateChangeListener(mStateChangeListener)
            uiExtensionsManager!!.onDestroy(this)
        }
        freeMemory()
        super.onDestroy()
    }

    private fun freeMemory() {
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (uiExtensionsManager != null) {
            if (mActionMode == null) {
                pdfViewCtrl!!.offsetScrollBoundary(0, 0, 0, 0)
                pdfViewCtrl!!.postPageContainer()
            } else {
                val tv = TypedValue()
                val actionBarHeight: Int
                if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight =
                        TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                    pdfViewCtrl!!.offsetScrollBoundary(0, actionBarHeight, 0, 0)
                    pdfViewCtrl!!.postPageContainer()
                }
            }
            uiExtensionsManager!!.onConfigurationChanged(this, newConfig)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (uiExtensionsManager != null && uiExtensionsManager!!.onKeyDown(
                this,
                keyCode,
                event
            )
        ) true else super.onKeyDown(
            keyCode,
            event
        )
    }

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
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
                UIToast.getInstance(applicationContext)
                    .show(getString(R.string.fx_unlock_library_failed))
                return false
            }
            showSystemUI()
            uiExtensionsManager!!.triggerDismissMenuEvent()
            val itemId = item.itemId
            if (itemId == R.id.Note || itemId == R.id.Highlight || itemId == R.id.Squiggly || itemId == R.id.Underline || itemId == R.id.StrikeOut) {
                if (!uiExtensionsManager!!.canAddAnnot()) {
                    UIToast.getInstance(applicationContext)
                        .show(getString(R.string.fx_the_document_cannot_modify))
                    return false
                }
            }
            if (itemId == R.id.Outline) {
                if (outlineModule == null) {
                    outlineModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_OUTLINE) as OutlineModule?
                }
                if (outlineModule == null) {
                    outlineModule = OutlineModule(
                        mContext,
                        uiExtensionsManager!!.rootView,
                        pdfViewCtrl,
                        uiExtensionsManager
                    )
                    outlineModule!!.loadModule()
                }

                if (outlineModule != null) uiExtensionsManager!!.panelManager.showPanel(PanelSpec.OUTLINE)
            } else if (itemId == R.id.ChangeLayout) {
                pdfViewCtrl!!.isContinuous = !pdfViewCtrl!!.isContinuous
                pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                val pageNavigationModule =
                    uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_PAGENAV) as PageNavigationModule
                pageNavigationModule?.resetJumpView()
            } else if (itemId == R.id.Search) {
                if (searchModule == null) {
                    searchModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SEARCH) as SearchModule
                    if (searchModule == null) {
                        searchModule = SearchModule(
                            mContext,
                            uiExtensionsManager!!.rootView,
                            pdfViewCtrl,
                            uiExtensionsManager
                        )
                        searchModule!!.loadModule()
                    }
                }
                val searchView = searchModule!!.searchView
                searchView.show()
            } else if (itemId == R.id.Note) {
                if (noteModule == null) {
                    noteModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_NOTE) as NoteModule
                }
                uiExtensionsManager!!.currentToolHandler = noteModule!!.toolHandler
            } else if (itemId == R.id.DocInfo) {
                if (docInfoModule == null) {
                    docInfoModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_DOCINFO) as DocInfoModule
                }
                val docInfoView = docInfoModule!!.view
                docInfoView?.show()
            } else if (itemId == R.id.Highlight) {
                if (highlightModule == null) highlightModule =
                    uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_HIGHLIGHT) as HighlightModule
                uiExtensionsManager!!.currentToolHandler = highlightModule!!.toolHandler
            } else if (itemId == R.id.Underline) {
                if (underlineModule == null) {
                    underlineModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_UNDERLINE) as UnderlineModule
                }
                uiExtensionsManager!!.currentToolHandler = underlineModule!!.toolHandler
            } else if (itemId == R.id.StrikeOut) {
                if (strikeoutModule == null) {
                    strikeoutModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_STRIKEOUT) as StrikeoutModule
                }
                uiExtensionsManager!!.currentToolHandler = strikeoutModule!!.toolHandler
            } else if (itemId == R.id.Squiggly) {
                if (squigglyModule == null) {
                    squigglyModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SQUIGGLY) as SquigglyModule
                }
                uiExtensionsManager!!.currentToolHandler = squigglyModule!!.toolHandler
            } else if (itemId == R.id.Annotations) {
                if (annotPanelModule == null) {
                    annotPanelModule =
                        uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_ANNOTPANEL) as AnnotPanelModule
                }

                if (annotPanelModule == null) {
                    annotPanelModule = AnnotPanelModule(mContext, pdfViewCtrl, uiExtensionsManager)
                    annotPanelModule!!.loadModule()
                }

                if (annotPanelModule != null) uiExtensionsManager!!.panelManager.showPanel(PanelSpec.ANNOTATIONS)
            } else if (itemId == R.id.Thumbnail) {
                if (thumbnailModule != null) {
                    thumbnailModule!!.show()
                }
            }
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            if (mActionMode != null) mActionMode = null
            pdfViewCtrl!!.offsetScrollBoundary(0, 0, 0, 0)
            pdfViewCtrl!!.postPageContainer()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ALL_FILES_ACCESS_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    openDocument()
                }
            }
        } else {
            if (pdfViewCtrl != null) {
                pdfViewCtrl!!.handleActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private val mStateChangeListener = IStateChangeListener { oldState, newState ->
        val readState =
            newState == ReadStateConfig.STATE_REFLOW || newState == ReadStateConfig.STATE_PANZOOM || newState == ReadStateConfig.STATE_SEARCH || newState == ReadStateConfig.STATE_TTS || newState == ReadStateConfig.STATE_AUTOFLIP || newState == ReadStateConfig.STATE_REDACT
        if (readState) {
            if (mActionMode != null) {
                mActionMode!!.finish()
            }
        } else {
            if (oldState != newState) {
                if (mActionMode == null) {
                    createActionMode()
                }
            }
        }
    }
    private val mMenuEventListener =
        MenuEventListener { if (mActionMode != null) mActionMode!!.finish() }

    private fun createActionMode() {
        val tv = TypedValue()
        val actionBarHeight: Int
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            pdfViewCtrl!!.offsetScrollBoundary(0, actionBarHeight, 0, 0)
            pdfViewCtrl!!.postPageContainer()
        }
        mActionMode = (mContext as Activity?)!!.startActionMode(mActionModeCallback)
        val doneButtonId =
            Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android")
        val closeButton = findViewById<View>(doneButtonId)
        closeButton?.setOnClickListener {
            uiExtensionsManager!!.triggerDismissMenuEvent()
            uiExtensionsManager!!.backToPrevActivity()
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    companion object {
        private val sn = "l5uLRkyIIDIyKJQZBChK2tXW/BikAnJozYEi1ApEyOR7i8W3U0ZlKQ=="
        private val key =
            "ezJvjl8mvB539NviuavWvpsxZwdMWZ2hvkmJNQZ8S/CwnxmS4c9F6U69I385uOe2wT4Fg2fJksQtXtnFsJ6lZR6RmsquC9T+GuC1YcZfAx/DRZivPTAkOaYoOwHQhGkkeTytiGg4KlolOVjyyRy5ZjzBBuwgODp1AcJAdTSvFlZnl+iCoYbPEKxUo/2+grZrhLICAXhrEioM4AwgIp1FxhQGlTLdv6OmuczqP0jt4IAEEJ1VhL5rh8X1fTGpx8fR8i0o0Ez/X307CCLaHBYLVXWMaZRn0XCsA1cOtcnD7XME1T4rHm4e4F+leLLPeylUoAMA/x1LwHj2yky9b2IclJxYcXRVdZOjCZsNPLpUDZS/UvAdTNrbkDl8fS/Vx75QOW+2z8//pjK4UR23WMi9yuvhXpfyi5Etv0aZDe969Pmc1vt2zK2Ddz2EAO5BslqcPDw2eBfCMBQL+iz3p9xg0XI9pAI6DnRDuqHkqHh6EVZ6zN20BupuDOdTg+PemU739fedBXY7TQz7ORE6BtzvPIlpyG1mNKC7A3bOIzyTDbVfSq3bPj5qoas7brtGTce1j0EHfzF3rzyFsKbvxcTcBRKzV+bAvtNofD4qPtqz7edHNbJKVcugoARzikVFW3dD7d14p7QUV+d6QkQf12KvzocGRfY1cHC/+Cey25k0+UtFQ/KdhaU/EVOfprWqeJLUyqX/GV9WX7I3A3OF8nTqeh7UpaOin8pA3T5k3tzAcnzFf9jFXjZeT1cRhClLSbWR4fGn+rxeLr2lwTOa9kBR1BY/iwItyY7uxCj1LcxtLKNC+BFRK4tXTsFlCjQPJOreF0oBxAhSp8dTmeXsdb/QVJMlR1iuJwqIWoxfg9+zHBNPUHpK33weRQ/j2gRPGBV2eW3+Wqcx+5VyB3PtCxaheJ3jMgXD2/1UBh24JVUVwgL0oQ3fi7EhleoALwQaulCWP5TTCOioPJFjVGBMo5BfH4o4rU1JNDse/QIauw1EkQQHlzfazCpU9gHnP4nBNKAgn+fNc+hwDBEP0dmlIEeHvy4kGEQQCwtMuV6Ezam1BAUwjKp8Lw5d2B/8d65mUCj1kZl2cXLEAnrwFCyZ8+RHe4XK+DZGCbwjzcyzJdQ+3qUrVgf9iseJm9XpOZp1azqo5nfOThl4lJAcEty7lsbXRpldNiFb8VE/hMkm/cFR9PNj40N4Zq+EvdiSO1ZwaEyM67OHwgo6i0QtGhp1SNA6Enq6OVNEy9J0QF5e2XT4UoZNN7roRKkP1ADvQA=="
        const val REQUEST_EXTERNAL_STORAGE = 111
        const val REQUEST_ALL_FILES_ACCESS_PERMISSION = 222
        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}