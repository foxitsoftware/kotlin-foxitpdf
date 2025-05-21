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
import android.widget.Toast
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.json.*
import com.foxit.pdf.function_demo.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Font
import com.foxit.sdk.common.Library
import com.foxit.sdk.common.Path
import com.foxit.sdk.common.fxcrt.FloatArray
import com.foxit.sdk.common.fxcrt.PointF
import com.foxit.sdk.common.fxcrt.PointFArray
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.FileSpec
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.actions.Action
import com.foxit.sdk.pdf.actions.URIAction
import com.foxit.sdk.pdf.annots.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException

class Annotation(private val mContext: Context) {
    private val inputFile: String
    private val outputFile: String
    private val outputJsonFile: String
    fun addAnnotation() {
        val doc = Common.loadPDFDoc(mContext, inputFile, null) ?: return
        addAnnotation(doc)
        Common.saveDFDoc(mContext, doc, outputFile)
        exportToJSON(doc)
    }

    private fun addAnnotation(doc: PDFDoc) {
        try {
            val page = doc.getPage(0)
            // Add line annotation 
            // No special intent, as a common line.
            var annot = page.addAnnot(Annot.e_Line, RectF(0f, 650f, 100f, 750f))
            var line = Line(annot)
            line.startPoint = PointF(20f, 650f)
            line.endPoint = PointF(100f, 740f)
            // Intent, as line arrow.
            line.content = "A line arrow annotation"
            line.intent = "LineArrow"
            line.subject = "Arrow"
            line.title = "Foxit SDK"
            line.creationDateTime = Common.currentDateTime
            line.modifiedDateTime = Common.currentDateTime
            line.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            line.resetAppearanceStream()
            line = Line(page.addAnnot(Annot.e_Line, RectF(0f, 650f, 100f, 760f)))
            // Set foxit RGB color
            line.borderColor = 0x00FF00
            line.startPoint = PointF(10f, 650f)
            line.endPoint = PointF(100f, 750f)
            line.content = "A common line."
            line.lineStartStyle = Markup.e_EndingStyleSquare
            line.lineEndStyle = Markup.e_EndingStyleOpenArrow
            // Show text in line
            line.enableCaption(true)
            line.captionOffset = PointF(0f, 5f)
            line.subject = "Line"
            line.title = "Foxit SDK"
            line.creationDateTime = Common.currentDateTime
            line.modifiedDateTime = Common.currentDateTime
            line.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            line.resetAppearanceStream()

            // Add circle annotation
            annot = page.addAnnot(Annot.e_Circle, RectF(100f, 650f, 200f, 750f))
            val circle = Circle(annot)
            circle.innerRect = RectF(120f, 660f, 160f, 740f)
            circle.subject = "Circle"
            circle.title = "Foxit SDK"
            circle.creationDateTime = Common.currentDateTime
            circle.modifiedDateTime = Common.currentDateTime
            circle.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            circle.resetAppearanceStream()

            // Add square annotation
            annot = page.addAnnot(Annot.e_Square, RectF(200f, 650f, 300f, 750f))
            val square = Square(annot)
            square.fillColor = 0x00FF00
            square.innerRect = RectF(220f, 660f, 260f, 740f)
            square.subject = "Square"
            square.title = "Foxit SDK"
            square.creationDateTime = Common.currentDateTime
            square.modifiedDateTime = Common.currentDateTime
            square.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            square.resetAppearanceStream()

            // Add polygon annotation, as cloud.
            annot = page.addAnnot(Annot.e_Polygon, RectF(300f, 650f, 500f, 750f))
            var polygon = Polygon(annot)
            polygon.intent = "PolygonCloud"
            polygon.fillColor = 0x0000FF
            var vertexe_array = PointFArray()
            vertexe_array.add(PointF(335f, 665f))
            vertexe_array.add(PointF(365f, 665f))
            vertexe_array.add(PointF(385f, 705f))
            vertexe_array.add(PointF(365f, 740f))
            vertexe_array.add(PointF(335f, 740f))
            vertexe_array.add(PointF(315f, 705f))
            polygon.vertexes = vertexe_array
            polygon.subject = "Cloud"
            polygon.title = "Foxit SDK"
            polygon.creationDateTime = Common.currentDateTime
            polygon.modifiedDateTime = Common.currentDateTime
            polygon.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            polygon.resetAppearanceStream()

            // Add polygon annotation, with dashed border.
            val borderinfo = BorderInfo()
            borderinfo.cloud_intensity = 2.0f
            borderinfo.width = 2.0f
            borderinfo.style = BorderInfo.e_Dashed
            borderinfo.dash_phase = 3.0f
            val floatArray = FloatArray()
            floatArray.add(2.0f)
            floatArray.add(2.0f)
            borderinfo.dashes = floatArray
            annot = page.addAnnot(Annot.e_Polygon, RectF(400f, 650f, 500f, 750f))
            polygon = Polygon(annot)
            polygon.fillColor = 0x0000FF
            polygon.borderInfo = borderinfo
            vertexe_array = PointFArray()
            vertexe_array.add(PointF(435f, 665f))
            vertexe_array.add(PointF(465f, 665f))
            vertexe_array.add(PointF(485f, 705f))
            vertexe_array.add(PointF(465f, 740f))
            vertexe_array.add(PointF(435f, 740f))
            vertexe_array.add(PointF(415f, 705f))
            polygon.vertexes = vertexe_array
            polygon.subject = "Polygon"
            polygon.title = "Foxit SDK"
            polygon.creationDateTime = Common.currentDateTime
            polygon.modifiedDateTime = Common.currentDateTime
            polygon.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            polygon.resetAppearanceStream()

            // Add polyline annotation 
            annot = page.addAnnot(Annot.e_PolyLine, RectF(500f, 650f, 600f, 700f))
            val polyline = PolyLine(annot)
            vertexe_array = PointFArray()
            vertexe_array.add(PointF(515f, 705f))
            vertexe_array.add(PointF(535f, 740f))
            vertexe_array.add(PointF(565f, 740f))
            vertexe_array.add(PointF(585f, 705f))
            vertexe_array.add(PointF(565f, 665f))
            vertexe_array.add(PointF(535f, 665f))
            polyline.vertexes = vertexe_array
            polyline.subject = "PolyLine"
            polyline.title = "Foxit SDK"
            polyline.creationDateTime = Common.currentDateTime
            polyline.modifiedDateTime = Common.currentDateTime
            polyline.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            polyline.resetAppearanceStream()

            // Add freetext annotation, as type writer
            annot = page.addAnnot(Annot.e_FreeText, RectF(10f, 550f, 200f, 600f))
            var freetext = FreeText(annot)
            // Set default appearance
            var default_ap = DefaultAppearance()
            default_ap.flags =
                DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
            default_ap.font = Font(Font.e_StdIDHelvetica)
            default_ap.text_size = 12.0f
            default_ap.text_color = 0x000000
            // Set default appearance for form.
            freetext.defaultAppearance = default_ap
            freetext.alignment = Constants.e_AlignmentLeft
            freetext.intent = "FreeTextTypewriter"
            freetext.content = "A typewriter annotation"
            freetext.subject = "FreeTextTypewriter"
            freetext.title = "Foxit SDK"
            freetext.creationDateTime = Common.currentDateTime
            freetext.modifiedDateTime = Common.currentDateTime
            freetext.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add freetext annotation, as call-out
            annot = page.addAnnot(Annot.e_FreeText, RectF(300f, 550f, 400f, 600f))
            freetext = FreeText(annot)
            // Set default appearance
            default_ap = DefaultAppearance()
            default_ap.flags =
                DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
            default_ap.font = Font(Font.e_StdIDHelveticaB)
            default_ap.text_size = 12.0f
            default_ap.text_color = 0x000000
            // Set default appearance for form.
            freetext.defaultAppearance = default_ap
            freetext.alignment = Constants.e_AlignmentCenter
            freetext.intent = "FreeTextCallout"
            val callout_points = PointFArray()
            callout_points.add(PointF(250f, 540f))
            callout_points.add(PointF(280f, 570f))
            callout_points.add(PointF(300f, 570f))
            freetext.calloutLinePoints = callout_points
            freetext.calloutLineEndingStyle = Markup.e_EndingStyleOpenArrow
            freetext.content = "A callout annotation."
            freetext.subject = "FreeTextCallout"
            freetext.title = "Foxit SDK"
            freetext.creationDateTime = Common.currentDateTime
            freetext.modifiedDateTime = Common.currentDateTime
            freetext.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add freetext annotation, as text box
            annot = page.addAnnot(Annot.e_FreeText, RectF(450f, 550f, 550f, 600f))
            freetext = FreeText(annot)
            // Set default appearance
            default_ap = DefaultAppearance()
            default_ap.flags =
                DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
            default_ap.font = Font(Font.e_StdIDHelveticaI)
            default_ap.text_size = 12.0f
            default_ap.text_color = 0x000000
            // Set default appearance for form.
            freetext.defaultAppearance = default_ap
            freetext.alignment = Constants.e_AlignmentCenter
            freetext.content = "A text box annotation."
            freetext.subject = "Textbox"
            freetext.title = "Foxit SDK"
            freetext.creationDateTime = Common.currentDateTime
            freetext.modifiedDateTime = Common.currentDateTime
            freetext.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add highlight annotation
            val highlight = Highlight(page.addAnnot(Annot.e_Highlight, RectF(10f, 450f, 100f, 550f)))
            highlight.content = "Highlight"
            var quad_points = QuadPoints()
            quad_points.first = PointF(10f, 500f)
            quad_points.second = PointF(90f, 500f)
            quad_points.third = PointF(10f, 480f)
            quad_points.fourth = PointF(90f, 480f)
            var quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            highlight.quadPoints = quad_points_array
            highlight.subject = "Highlight"
            highlight.title = "Foxit SDK"
            highlight.creationDateTime = Common.currentDateTime
            highlight.modifiedDateTime = Common.currentDateTime
            highlight.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            highlight.resetAppearanceStream()

            // Add underline annotation
            val underline = Underline(page.addAnnot(Annot.e_Underline, RectF(100f, 450f, 200f, 550f)))
            quad_points = QuadPoints()
            quad_points.first = PointF(110f, 500f)
            quad_points.second = PointF(190f, 500f)
            quad_points.third = PointF(110f, 480f)
            quad_points.fourth = PointF(190f, 480f)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            underline.quadPoints = quad_points_array
            underline.subject = "Underline"
            underline.title = "Foxit SDK"
            underline.creationDateTime = Common.currentDateTime
            underline.modifiedDateTime = Common.currentDateTime
            underline.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            underline.resetAppearanceStream()

            // Add squiggly annotation
            val squiggly = Squiggly(page.addAnnot(Annot.e_Squiggly, RectF(200f, 450f, 300f, 550f)))
            squiggly.intent = "Squiggly"
            quad_points = QuadPoints()
            quad_points.first = PointF(210f, 500f)
            quad_points.second = PointF(290f, 500f)
            quad_points.third = PointF(210f, 480f)
            quad_points.fourth = PointF(290f, 480f)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            squiggly.quadPoints = quad_points_array
            squiggly.subject = "Squiggly"
            squiggly.title = "Foxit SDK"
            squiggly.creationDateTime = Common.currentDateTime
            squiggly.modifiedDateTime = Common.currentDateTime
            squiggly.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            squiggly.resetAppearanceStream()

            // Add strikeout annotation
            val strikeout = StrikeOut(page.addAnnot(Annot.e_StrikeOut, RectF(300f, 450f, 400f, 550f)))
            quad_points = QuadPoints()
            quad_points.first = PointF(310f, 500f)
            quad_points.second = PointF(390f, 500f)
            quad_points.third = PointF(310f, 480f)
            quad_points.fourth = PointF(390f, 480f)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            strikeout.quadPoints = quad_points_array
            strikeout.subject = "StrikeOut"
            strikeout.title = "Foxit SDK"
            strikeout.creationDateTime = Common.currentDateTime
            strikeout.modifiedDateTime = Common.currentDateTime
            strikeout.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            strikeout.resetAppearanceStream()

            // Add caret annotation
            val caret = Caret(page.addAnnot(Annot.e_Caret, RectF(400f, 450f, 420f, 470f)))
            caret.innerRect = RectF(410f, 450f, 430f, 470f)
            caret.content = "Caret annotation"
            caret.subject = "Caret"
            caret.title = "Foxit SDK"
            caret.creationDateTime = Common.currentDateTime
            caret.modifiedDateTime = Common.currentDateTime
            caret.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            caret.resetAppearanceStream()

            // Add note annotation
            val note = Note(page.addAnnot(Annot.e_Note, RectF(10f, 350f, 50f, 400f)))
            note.iconName = "Comment"
            note.subject = "Note"
            note.title = "Foxit SDK"
            note.content = "Note annotation."
            note.creationDateTime = Common.currentDateTime
            note.modifiedDateTime = Common.currentDateTime
            note.uniqueID = Common.randomUUID(null)
            // Add popup to note annotation
            val popup = Popup(page.addAnnot(Annot.e_Popup, RectF(300f, 450f, 500f, 550f)))
            popup.borderColor = 0x00FF00
            popup.openStatus = false
            popup.modifiedDateTime = Common.currentDateTime
            note.popup = popup

            // Add reply annotation to note annotation
            val reply = note.addReply()
            reply.content = "reply"
            reply.modifiedDateTime = Common.currentDateTime
            reply.title = "Foxit SDK"
            reply.uniqueID = Common.randomUUID(null)
            reply.resetAppearanceStream()

            // Add state annotation to note annotation
            val state = Note(
                note.addStateAnnot(
                    "Foxit SDK",
                    Markup.e_StateModelReview,
                    Markup.e_StateAccepted
                )
            )
            state.content = "Accepted set by Foxit SDK"
            state.uniqueID = Common.randomUUID(null)
            state.resetAppearanceStream()
            // Appearance should be reset.
            note.resetAppearanceStream()

            // Add ink annotation
            val ink = Ink(page.addAnnot(Annot.e_Ink, RectF(100f, 350f, 200f, 450f)))
            val inklist = Path()
            val width = 100f
            val height = 100f
            val out_width = Math.min(width, height) * 2 / 3f
            val inner_width =
                (out_width * Math.sin((18f / 180f * 3.14f).toDouble()) / Math.sin((36f / 180f * 3.14f).toDouble())).toFloat()
            val center: PointF = PointF(150f, 400f)
            var x = out_width
            var y = 0f
            inklist.moveTo(PointF(center.x + x, center.y + y))
            for (i in 0..4) {
                x = (out_width * Math.cos((72f * i / 180f * 3.14f).toDouble())).toFloat()
                y = (out_width * Math.sin((72f * i / 180f * 3.14f).toDouble())).toFloat()
                inklist.lineTo(PointF(center.x + x, center.y + y))
                x = (inner_width * Math.cos(((72f * i + 36) / 180f * 3.14f).toDouble())).toFloat()
                y = (inner_width * Math.sin(((72f * i + 36) / 180f * 3.14f).toDouble())).toFloat()
                inklist.lineTo(PointF(center.x + x, center.y + y))
            }
            inklist.lineTo(PointF(center.x + out_width, center.y + 0))
            inklist.closeFigure()
            ink.inkList = inklist
            ink.subject = "Ink"
            ink.title = "Foxit SDK"
            ink.content = "Note annotation."
            ink.creationDateTime = Common.currentDateTime
            ink.modifiedDateTime = Common.currentDateTime
            ink.uniqueID = Common.randomUUID(null)
            // Appearance should be reset.
            ink.resetAppearanceStream()

            // Add file attachment annotation
            val attachment_file = Common.fixFolder + "AboutFoxit.pdf"
            val file_attachment = FileAttachment(
                page.addAnnot(Annot.e_FileAttachment, RectF(280f, 350f, 300f, 380f))
            )
            file_attachment.iconName = "Graph"
            val file_spec = FileSpec(page.document)
            file_spec.fileName = "attachment.pdf"
            file_spec.creationDateTime = Common.currentDateTime
            file_spec.description = "The original file"
            file_spec.modifiedDateTime = Common.currentDateTime
            file_spec.embed(attachment_file)
            file_attachment.fileSpec = file_spec
            file_attachment.subject = "File Attachment"
            file_attachment.title = "Foxit SDK"
            // Appearance should be reset.
            file_attachment.resetAppearanceStream()

            // Add link annotation
            val link = Link(page.addAnnot(Annot.e_Link, RectF(350f, 350f, 380f, 400f)))
            link.highlightingMode = Annot.e_HighlightingToggle

            // Add action for link annotation
            val action = Action.create(page.document, Action.e_TypeURI)
            val uriAction = URIAction(action)
            uriAction.setTrackPositionFlag(true)
            uriAction.uri = "www.foxitsoftware.com"
            link.action = uriAction
            // Appearance should be reset.
            link.resetAppearanceStream()


            // Set icon provider for annotation to Foxit PDF SDK.
            val icon_provider = CustomIconProvider(mContext)
            Library.setAnnotIconProviderCallback(icon_provider)

            // Add common stamp annotation.
            Library.setActionCallback(null)
            icon_provider.setUseDynamicStamp(false)
            annot = page.addAnnot(Annot.e_Stamp, RectF(110f, 150f, 200f, 250f))
            val static_stamp = Stamp(annot)
            static_stamp.iconName = "Approved"
            // Appearance should be reset.
            static_stamp.resetAppearanceStream()

            // Add dynamic stamp annotation.
            val action_callback = CustomActionCallback(mContext)
            Library.setActionCallback(action_callback)
            icon_provider.setUseDynamicStamp(true)
            annot = page.addAnnot(Annot.e_Stamp, RectF(10f, 150f, 100f, 250f))
            val dynamic_stamp = Stamp(annot)
            dynamic_stamp.iconName = "Approved"
            // Appearance should be reset.
            dynamic_stamp.resetAppearanceStream()
        } catch (e: PDFException) {
            e.printStackTrace()
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_add_annot_error, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun exportToJSON(doc: PDFDoc?) {
        if (doc == null) return
        try {
            val pageCount = doc.pageCount
            val jsonArray = JSONArray()
            for (i in 0 until pageCount) {
                val page = doc.getPage(i)
                val annotCount = page.annotCount
                for (j in 0 until annotCount) {
                    val annot = page.getAnnot(j)
                    if (annot == null || annot.isEmpty) continue
                    val jsonObject = exportAnnotToJSON(doc, annot)
                    if (jsonObject != null) {
                        jsonArray.put(jsonObject)
                    }
                }
            }
            val annotJson = jsonArray.toString().replace("\\/", "/")
            val txtFile = File(outputJsonFile)
            val fileWriter = FileWriter(txtFile)
            fileWriter.write(annotJson)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: PDFException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun exportAnnotToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        try {
            val type = annot.type
            return if (type == Annot.e_Widget || type == Annot.e_Popup || type == Annot.e_Watermark) null else when (type) {
                Annot.e_Caret -> JSCaret.exportToJSON(doc, annot)
                Annot.e_Circle -> JSCircle.exportToJSON(doc, annot)
                Annot.e_FileAttachment -> JSFileAttachment.exportToJSON(doc, annot)
                Annot.e_FreeText -> JSFreeText.exportToJSON(doc, annot)
                Annot.e_Ink -> JSInk.exportToJSON(doc, annot)
                Annot.e_Line -> JSLine.exportToJSON(doc, annot)
                Annot.e_Note -> JSNote.exportToJSON(doc, annot)
                Annot.e_Polygon -> JSPolygon.exportToJSON(doc, annot)
                Annot.e_PolyLine -> JSPolyLine.exportToJSON(doc, annot)
                Annot.e_Redact -> JSRedact.exportToJSON(doc, annot)
                Annot.e_Sound -> JSSound.exportToJSON(doc, annot)
                Annot.e_Square -> JSSquare.exportToJSON(doc, annot)
                Annot.e_Stamp -> JSStamp.exportToJSON(doc, annot)
                Annot.e_Highlight, Annot.e_Underline, Annot.e_StrikeOut, Annot.e_Squiggly -> JSTextMarkupAnnot.exportToJSON(doc, annot)
                else -> if (annot.isMarkup) JSMarkupAnnot.exportToJSON(doc, annot) else JSAnnot.exportToJSON(annot)
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return null
    }

    init {
        inputFile = Common.fixFolder + "annotation_input.pdf"
        outputFile = Common.getOutputFilesFolder(Common.ANNOTATION) + "annotation_add.pdf"
        outputJsonFile = Common.getOutputFilesFolder(Common.ANNOTATION) + "annotation_add.json"
    }
}