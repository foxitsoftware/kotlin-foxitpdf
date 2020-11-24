/**
 * Copyright (C) 2003-2020, Foxit Software Inc..
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
package com.foxit.pdf.function

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.text.format.Time
import android.widget.Toast
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.DateTime
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage
import java.io.*
import java.util.*


object Common {

    const val ANNOTATION = 0
    const val OUTLINE = 1
    const val DOCINFO = 2
    const val PDF_TO_TXT = 3
    const val PDF_TO_IMAGE = 4
    const val IMAGE_TO_PDF = 5
    const val SIGNATURE = 6
    const val WATERMARK = 7
    const val SEARCH = 8
    const val GRAPHICS_OBJECTS = 9

    private const val inputFiles = "input_files.txt"

//    val pdf2textModuleName = "pdf2text"
//    val outlineModuleName = "outline"
//    val docInfoModuleName = "docInfo"
//    val renderModuleName = "render"
//    val annotationModuleName = "annotation"
//    val signatureModuleName = "signature"
//
//    val testInputFile = "FoxitBigPreview.pdf"
//    val outlineInputFile = "Outline.pdf"
//    val anotationInputFile = "Annotation.pdf"
//    val signatureInputFile = "Sample.pdf"
//    val signatureCertification = "foxit_all.pfx"

    fun getSuccessInfo(context: Context, path: String): String {
        return context.getString(R.string.fx_file_saved_successd, path)
    }

    var externalPath: String? = null

    val currentDateTime: DateTime
        get() {
            val now = Time()
            now.setToNow()

            var dateTime: DateTime? = null

            val year = now.year
            val month = now.month + 1
            val date = now.monthDay
            val hour = now.hour
            val minute = now.minute
            val second = now.second
            val timezone = TimeZone.getDefault().rawOffset
            val localHour = timezone / 3600000
            val localMinute = timezone % 3600000 / 60

            dateTime = DateTime()
            dateTime.set(year, month, date, hour, minute, second, 0, localHour.toShort(), localMinute)

            return dateTime
        }

    //Check whether the SD is available.
    val isSDAvailable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    val sdPath: String
        get() = Environment.getExternalStorageDirectory().path

    fun checkSD(): Boolean {
        val sdExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (sdExist) {
            val sddir = Environment.getExternalStorageDirectory()
            externalPath = sddir.path
        } else {
            externalPath = null
        }
        return sdExist
    }

    fun getFixFolder(): String? {
        var path = externalPath
        path += "/input_files/"
        return path
    }

    fun createFolder(folderPath: String?): Boolean {
        try {
            val myFilePath = File(folderPath)
            if (!myFilePath.exists()) {
                myFilePath.mkdirs()
            }
        } catch (e: Exception) {
        }

        return true
    }

    fun getOutputFilesFolder(type: Int): String? {
        //Combine the current external path, outputting files path (fixed) and example module name together
        var outputPath = externalPath
        outputPath += "/output_files/"
        val moduleName = getModuleName(type)
        if (moduleName != null && moduleName.trim { it <= ' ' }.length > 1) outputPath += "$moduleName/"
        createFolder(outputPath)
        return outputPath
    }

    fun saveImageFile(bitmap: Bitmap, picFormat: Bitmap.CompressFormat, fileName: String): Boolean {
        val file = File(fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(picFormat, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun loadPDFDoc(context: Context, path: String, password: ByteArray?): PDFDoc? {
        try {
            val doc = PDFDoc(path)
            if (doc.isEmpty) {
                Toast.makeText(context, context.getString(R.string.fx_the_path_not_exist_error, path), Toast.LENGTH_LONG).show()
                return null
            }

            doc.load(password)
            return doc
        } catch (e: PDFException) {
            Toast.makeText(context, context.getString(R.string.fx_load_document_error, e.message), Toast.LENGTH_LONG).show()
        }

        return null
    }

    fun loadPage(context: Context, doc: PDFDoc?, index: Int, parseFlag: Int): PDFPage? {
        var page: PDFPage? = null
        if (doc == null || doc.isEmpty) {
            Toast.makeText(context, context.getString(R.string.fx_the_document_is_null), Toast.LENGTH_LONG).show()
            return page
        }

        try {
            page = doc.getPage(index)
            if (page == null || page.isEmpty) {
                Toast.makeText(context, context.getString(R.string.fx_the_page_is_null), Toast.LENGTH_LONG).show()
                return page
            }

            if (!page.isParsed) {
                val progressive = page.startParse(parseFlag, null, false)

                var state = Progressive.e_ToBeContinued
                while (state == Progressive.e_ToBeContinued) {
                    state = progressive.resume()
                }

                if (state == Progressive.e_Error) {
                    Toast.makeText(context, context.getString(R.string.fx_parse_page_error), Toast.LENGTH_LONG).show()
                    return null
                }
            }

        } catch (e: PDFException) {
            Toast.makeText(context, context.getString(R.string.fx_load_page_error, e.message), Toast.LENGTH_LONG).show()
        }

        return page
    }

    fun saveDFDoc(context: Context, doc: PDFDoc, save_path: String?): Boolean {
        try {
            val ret = doc.saveAs(save_path, PDFDoc.e_SaveFlagNoOriginal)
            if (ret) {
                Toast.makeText(context, getSuccessInfo(context, save_path!!), Toast.LENGTH_LONG).show()
                return true
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        Toast.makeText(context, context.getString(R.string.fx_save_doc_error), Toast.LENGTH_LONG).show()
        return false
    }


    fun randomUUID(separator: String?): String {
        val uuid = UUID.randomUUID().toString()
        if (separator != null) {
            uuid.replace("-", separator)
        }
        return uuid
    }

    private fun exist(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }

    private fun mergeFiles(context: Context, outDir: String?, files: List<String>): Boolean {
        var success = false
        var os: OutputStream? = null
        try {

            val buffer = ByteArray(1 shl 13)
            for (f in files) {
                val outFile = File(outDir + f)
                createParentPath(outFile)
                if (exist(getFixFolder() + f))
                    continue
                os = FileOutputStream(outDir + f)
                val `is` = context.assets.open(f)
                var len = `is`.read(buffer)
                while (len != -1) {
                    os.write(buffer, 0, len)
                    len = `is`.read(buffer)
                }
                `is`.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (os != null) {
                    os.flush()
                    os.close()
                    success = true
                }
            } catch (ignore: IOException) {
            }

        }
        return success
    }

    private fun createParentPath(file: File) {
        val parentFile = file.parentFile
        if (null != parentFile && !parentFile.exists()) {
            parentFile.mkdirs()
            createParentPath(parentFile)
        }
    }

    fun copyTestFiles(context: Context?) {
        if (isSDAvailable) {
            val testFiles = getAssetsList(context!!)
            mergeFiles(context, getFixFolder(), testFiles)
        }
    }

    private fun getAssetsList(context: Context): List<String> {
        val files: MutableList<String> = ArrayList()
        var inputStream: InputStream? = null
        var br: BufferedReader? = null
        try {
            inputStream = context.assets.open(File(inputFiles).path)
            br = BufferedReader(InputStreamReader(inputStream))
            br.forEachLine { files.add(it) }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (inputStream != null) inputStream.close()
                if (br != null) br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return files
    }

    fun getFileNameWithoutExt(filePath: String): String? {
        var index = filePath.lastIndexOf('/')
        var name = filePath.substring(index + 1)
        index = name.lastIndexOf('.')
        if (index > 0) {
            name = name.substring(0, index)
        }
        return name
    }

    private fun getModuleName(type: Int): String? {
        var name: String? = ""
        name = when (type) {
            ANNOTATION -> "annotation"
            OUTLINE -> "outline"
            DOCINFO -> "docInfo"
            PDF_TO_TXT -> "pdf2text"
            PDF_TO_IMAGE -> "render"
            IMAGE_TO_PDF -> "image2pdf"
            SIGNATURE -> "signature"
            WATERMARK -> "watermark"
            SEARCH -> "search"
            GRAPHICS_OBJECTS -> "graphics_objects"
            else -> null
        }
        return name
    }
}

