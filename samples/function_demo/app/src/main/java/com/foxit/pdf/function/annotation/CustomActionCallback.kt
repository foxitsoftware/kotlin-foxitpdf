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
package com.foxit.pdf.function.annotation

import android.content.Context
import android.content.pm.PackageManager
import com.foxit.sdk.ActionCallback
import com.foxit.sdk.ButtonItem
import com.foxit.sdk.DialogDescriptionConfig
import com.foxit.sdk.IdentityProperties
import com.foxit.sdk.MediaPlayerCallback
import com.foxit.sdk.MenuItemConfig
import com.foxit.sdk.MenuItemEx
import com.foxit.sdk.MenuItemExArray
import com.foxit.sdk.MenuListArray
import com.foxit.sdk.PlayerArgs
import com.foxit.sdk.PrintParams
import com.foxit.sdk.SOAPRequestProperties
import com.foxit.sdk.SOAPResponseInfo
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Range
import com.foxit.sdk.common.WStringArray
import com.foxit.sdk.common.fxcrt.PointF
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.Signature
import com.foxit.sdk.pdf.actions.Destination

internal class CustomActionCallback(context: Context) : ActionCallback() {
    private var mVersion: String? = null

    init {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            mVersion = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun release() {
    }

    override fun invalidateRect(document: PDFDoc, page_index: Int, pdf_rect: RectF): Boolean {
        return false
    }

    override fun getCurrentPage(document: PDFDoc): Int {
        return 0
    }

    override fun setCurrentPage(document: PDFDoc, page_index: Int) {
    }

    override fun setCurrentPage(document: PDFDoc, destination: Destination) {
    }

    override fun getPageRotation(document: PDFDoc, page_index: Int): Int {
        return Constants.e_Rotation0
    }

    override fun setPageRotation(document: PDFDoc, page_index: Int, rotation: Int): Boolean {
        return false
    }

    override fun executeNamedAction(document: PDFDoc, named_action: String): Boolean {
        return false
    }

    override fun setDocChangeMark(document: PDFDoc, change_mark: Boolean): Boolean {
        return false
    }

    override fun getDocChangeMark(document: PDFDoc): Boolean {
        return false
    }

    override fun getOpenedDocCount(): Int {
        return 1
    }

    override fun getOpenedDoc(index: Int): PDFDoc? {
        return null
    }

    override fun getCurrentDoc(): PDFDoc? {
        return null
    }

    override fun createBlankDoc(): PDFDoc? {
        return null
    }

    override fun closeDoc(document: PDFDoc, is_prompt_to_save: Boolean) {
    }

    override fun openDoc(file_path: String, password: String): PDFDoc? {
        return null
    }

    override fun beep(type: Int): Boolean {
        return false
    }

    override fun response(
        question: String,
        title: String,
        default_value: String,
        label: String,
        is_password: Boolean
    ): String? {
        return null
    }

    override fun getFilePath(document: PDFDoc): String? {
        return null
    }

    override fun isLocalFile(document: PDFDoc): Boolean {
        return false
    }

    override fun getAttachmentsFilePath(pdf_doc: PDFDoc, name: String): String {
        return ""
    }

    override fun getExtractedEmbeddedFilePath(pdf_doc: PDFDoc, name: String): String {
        return ""
    }

    override fun print(
        document: PDFDoc,
        is_ui: Boolean,
        page_range: Range,
        is_silent: Boolean,
        is_shrunk_to_fit: Boolean,
        is_printed_as_image: Boolean,
        is_reversed: Boolean,
        is_to_print_annots: Boolean
    ): Boolean {
        return false
    }

    override fun print(document: PDFDoc, print_params: PrintParams): Boolean {
        return false
    }

    override fun submitForm(
        document: PDFDoc,
        form_data: ByteArray,
        url: String,
        file_format_type: Int
    ): Boolean {
        return false
    }

    override fun launchURL(url: String): Boolean {
        return false
    }

    override fun browseFile(): String? {
        return null
    }

    override fun browseFile(
        is_open_dialog: Boolean,
        file_format: String,
        file_filter: String
    ): String? {
        return null
    }

    override fun getLanguage(): Int {
        return e_LanguageCHS
    }

    override fun alert(msg: String, title: String, type: Int, icon: Int): Int {
        return 0
    }

    override fun getIdentityProperties(): IdentityProperties {
        return IdentityProperties(
            "foxitsoftware",
            "simple_demo@foxitsoftware.cn",
            "simple demo",
            "Simple",
            "",
            "",
            "",
            ""
        )
    }

    override fun setIdentityProperties(identity_properties: IdentityProperties): Boolean {
        return false
    }

    override fun popupMenu(menus: MenuListArray, is_selected_item: Boolean): String? {
        return null
    }

    override fun popupMenuEx(menus: MenuItemExArray, is_selected_item: Boolean): MenuItemEx? {
        return null
    }

    override fun getAppInfo(type: Int): String {
        var info = ""
        when (type) {
            e_AppInfoTypeFormsVersion -> info = "7.3"
            e_AppInfoTypeViewerType -> info = "Exchange-Pro"
            e_AppInfoTypeViewerVariation -> info = "Full"
            e_AppInfoTypeViewerVersion -> info = "11.007"
            e_AppInfoTypeAppVersion -> info = mVersion!!
            else -> {}
        }
        return info
    }

    override fun mailData(
        data: Any,
        is_ui: Boolean,
        to: String,
        subject: String,
        cc: String,
        bcc: String,
        message: String
    ): Boolean {
        return false
    }

    override fun verifySignature(document: PDFDoc, pdf_signature: Signature): Int {
        return Signature.e_StateUnknown
    }

    override fun getUntitledBookmarkName(): String {
        return ""
    }

    override fun getPrinterNameList(): WStringArray {
        return WStringArray()
    }

    override fun addToolButton(button_item: ButtonItem): Boolean {
        return false
    }

    override fun removeToolButtom(button_name: String): Boolean {
        return false
    }

    override fun getMenuItemNameList(): MenuListArray {
        return MenuListArray()
    }

    override fun addMenuItem(menu_item_config: MenuItemConfig, is_prepend: Boolean): Boolean {
        return false
    }

    override fun addSubMenu(menu_item_config: MenuItemConfig): Boolean {
        return false
    }

    override fun showDialog(dlg_config: DialogDescriptionConfig): Boolean {
        return false
    }

    override fun getFullScreen(): Boolean {
        return false
    }

    override fun setFullScreen(is_full_screen: Boolean) {
    }

    override fun onFieldValueChanged(
        field_name: String,
        type: Int,
        value_before_changed: WStringArray,
        value_after_changed: WStringArray
    ) {
    }

    override fun updateLogicalLabel() {
    }

    override fun mailDoc(
        document: PDFDoc,
        to_address: String,
        cc_address: String,
        bcc_address: String,
        subject: String,
        message: String,
        is_ui: Boolean
    ): Int {
        return -1
    }

    override fun getTemporaryFileName(document: PDFDoc, file_suffix_name: String): String {
        return ""
    }

    override fun openMediaPlayer(player_args: PlayerArgs): MediaPlayerCallback? {
        return null
    }

    override fun getTemporaryDirectory(): String {
        return ""
    }

    override fun scroll(point: PointF) {
    }

    override fun selectPageNthWord(
        page_index: Int,
        start_offset: Int,
        end_offset: Int,
        is_show_selection: Boolean
    ) {
    }

    override fun getMousePosition(): PointF {
        return PointF()
    }

    override fun getPageWindowRect(): RectF {
        return RectF()
    }

    override fun getLayoutMode(): Int {
        return e_LayoutModeContinuous
    }

    override fun setLayoutMode(layout_mode: Int, is_cover_mode: Boolean) {
    }

    override fun getPageScale(): Float {
        return 1.0f
    }

    override fun setPageScale(zoom_mode: Int, dest: Destination) {
    }

    override fun getPageZoomMode(): Int {
        return Destination.e_ZoomFitBHorz
    }

    override fun soapRequest(request_params: SOAPRequestProperties): SOAPResponseInfo? {
        return null
    }

    override fun enablePageLoop(is_loop: Boolean) {
    }

    override fun isPageLoop(): Boolean {
        return false
    }

    override fun setDefaultPageTransitionMode(trans_type: String, trans_di: String) {
    }

    override fun isCurrentDocOpenedInBrowser(): Boolean {
        return false
    }

    override fun postMessageToHtml(message: WStringArray) {
    }
}
