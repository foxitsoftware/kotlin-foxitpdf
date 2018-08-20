/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
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

import com.foxit.sdk.PDFException
import com.foxit.sdk.common.DateTime
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.TimeZone
import java.util.UUID

object Common {

    val pdf2textModuleName = "pdf2text"
    val outlineModuleName = "outline"
    val docInfoModuleName = "docInfo"
    val renderModuleName = "render"
    val annotationModuleName = "annotation"
    val signatureModuleName = "signature"

    val testInputFile = "FoxitBigPreview.pdf"
    val outlineInputFile = "Outline.pdf"
    val anotationInputFile = "Annotation.pdf"
    val signatureInputFile = "Sample.pdf"
    val signatureCertification = "foxit_all.pfx"

    val runSuccesssInfo = "Successfully! The generated file was saved to "

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
            externalPath = sddir.toString()
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

    fun getOutputFilesFolder(moduleName: String): String? {
        //Combine the current external path, outputting files path (fixed) and example module name together
        var outputPath = externalPath
        outputPath += "/output_files/"
        outputPath += "$moduleName/"
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
            if (doc == null) {
                Toast.makeText(context, String.format("The path %s does not exist!", path), Toast.LENGTH_LONG).show()
                return null
            }

            doc.load(password)
            return doc
        } catch (e: PDFException) {
            Toast.makeText(context, "Load document error. " + e.message, Toast.LENGTH_LONG).show()
        }

        return null
    }

    fun loadPage(context: Context, doc: PDFDoc?, index: Int, parseFlag: Int): PDFPage? {
        var page: PDFPage? = null
        if (doc == null) {
            Toast.makeText(context, "The document is null!", Toast.LENGTH_LONG).show()
            return page
        }

        try {
            page = doc.getPage(index)
            if (page == null) {
                Toast.makeText(context, "Get Page error", Toast.LENGTH_LONG).show()
                return page
            }

            if (!page.isParsed) {
                val progressive = page.startParse(parseFlag, null, false)

                var state = Progressive.e_ToBeContinued
                while (state == Progressive.e_ToBeContinued) {
                    state = progressive.resume()
                }

                if (state == Progressive.e_Error) {
                    Toast.makeText(context, "Parse Page error!", Toast.LENGTH_LONG).show()
                    return null
                }
            }

        } catch (e: PDFException) {
            Toast.makeText(context, "Load Page error. " + e.message, Toast.LENGTH_LONG).show()
        }

        return page
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
        return file != null && file.exists()
    }

    private fun mergeFiles(context: Context, outDir: String?, files: Array<String>): Boolean {
        var success = false
        var os: OutputStream? = null
        try {

            val buffer = ByteArray(1 shl 13)
            for (f in files) {
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

    fun copyTestFiles(context: Context) {
        val testFiles = arrayOf(anotationInputFile, testInputFile, outlineInputFile, signatureInputFile, signatureCertification)
        if (Common.isSDAvailable) {
            val file = File(sdPath + File.separator + "input_files")
            if (!file.exists())
                file.mkdirs()
            mergeFiles(context, getFixFolder(), testFiles)
        }
    }
}

