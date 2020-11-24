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
package com.foxit.pdf.function.annotation

import android.content.Context
import android.widget.Toast
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.Common.currentDateTime
import com.foxit.pdf.function.Common.getFixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.randomUUID
import com.foxit.pdf.main.R
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

class Annotation(private val context: Context) {
    private val inputFile: String
    private val outputFile: String
    fun addAnnotation() {
        val doc = loadPDFDoc(context, inputFile, null) ?: return
        addAnnotation(doc)
        Common.saveDFDoc(context, doc, outputFile)
    }

    private fun addAnnotation(doc: PDFDoc) {
        try {
            val page = doc.getPage(0)
            // Add line annotation
            // No special intent, as a common line.
            var annot = page.addAnnot(Annot.e_Line, RectF(0F, 650F, 100F, 750F))
            var line = Line(annot)
            line.startPoint = PointF(20F, 650F)
            line.endPoint = PointF(100F, 740F)
            // Intent, as line arrow.
            line.content = "A line arrow annotation"
            line.intent = "LineArrow"
            line.subject = "Arrow"
            line.title = "Foxit SDK"
            line.creationDateTime = currentDateTime
            line.modifiedDateTime = currentDateTime
            line.uniqueID = randomUUID(null)
            // Appearance should be reset.
            line.resetAppearanceStream()
            line = Line(page.addAnnot(Annot.e_Line, RectF(0F, 650F, 100F, 760F)))
            // Set foxit RGB color
            line.borderColor = 0x00FF00
            line.startPoint = PointF(10F, 650F)
            line.endPoint = PointF(100F, 750F)
            line.content = "A common line."
            line.lineStartStyle = Markup.e_EndingStyleSquare
            line.lineEndStyle = Markup.e_EndingStyleOpenArrow
            // Show text in line
            line.enableCaption(true)
            line.captionOffset = PointF(0F, 5F)
            line.subject = "Line"
            line.title = "Foxit SDK"
            line.creationDateTime = currentDateTime
            line.modifiedDateTime = currentDateTime
            line.uniqueID = randomUUID(null)
            // Appearance should be reset.
            line.resetAppearanceStream()

            // Add circle annotation
            annot = page.addAnnot(Annot.e_Circle, RectF(100F, 650F, 200F, 750F))
            val circle = Circle(annot)
            circle.innerRect = RectF(120F, 660F, 160F, 740F)
            circle.subject = "Circle"
            circle.title = "Foxit SDK"
            circle.creationDateTime = currentDateTime
            circle.modifiedDateTime = currentDateTime
            circle.uniqueID = randomUUID(null)
            // Appearance should be reset.
            circle.resetAppearanceStream()

            // Add square annotation
            annot = page.addAnnot(Annot.e_Square, RectF(200F, 650F, 300F, 750F))
            val square = Square(annot)
            square.fillColor = 0x00FF00
            square.innerRect = RectF(220F, 660F, 260F, 740F)
            square.subject = "Square"
            square.title = "Foxit SDK"
            square.creationDateTime = currentDateTime
            square.modifiedDateTime = currentDateTime
            square.uniqueID = randomUUID(null)
            // Appearance should be reset.
            square.resetAppearanceStream()

            // Add polygon annotation, as cloud.
            annot = page.addAnnot(Annot.e_Polygon, RectF(300F, 650F, 500F, 750F))
            var polygon = Polygon(annot)
            polygon.intent = "PolygonCloud"
            polygon.fillColor = 0x0000FF
            var vertexe_array = PointFArray()
            vertexe_array.add(PointF(335F, 665F))
            vertexe_array.add(PointF(365F, 665F))
            vertexe_array.add(PointF(385F, 705F))
            vertexe_array.add(PointF(365F, 740F))
            vertexe_array.add(PointF(335F, 740F))
            vertexe_array.add(PointF(315F, 705F))
            polygon.vertexes = vertexe_array
            polygon.subject = "Cloud"
            polygon.title = "Foxit SDK"
            polygon.creationDateTime = currentDateTime
            polygon.modifiedDateTime = currentDateTime
            polygon.uniqueID = randomUUID(null)
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
            annot = page.addAnnot(Annot.e_Polygon, RectF(400F, 650F, 500F, 750F))
            polygon = Polygon(annot)
            polygon.fillColor = 0x0000FF
            polygon.borderInfo = borderinfo
            vertexe_array = PointFArray()
            vertexe_array.add(PointF(435F, 665F))
            vertexe_array.add(PointF(465F, 665F))
            vertexe_array.add(PointF(485F, 705F))
            vertexe_array.add(PointF(465F, 740F))
            vertexe_array.add(PointF(435F, 740F))
            vertexe_array.add(PointF(415F, 705F))
            polygon.vertexes = vertexe_array
            polygon.subject = "Polygon"
            polygon.title = "Foxit SDK"
            polygon.creationDateTime = currentDateTime
            polygon.modifiedDateTime = currentDateTime
            polygon.uniqueID = randomUUID(null)
            // Appearance should be reset.
            polygon.resetAppearanceStream()

            // Add polyline annotation
            annot = page.addAnnot(Annot.e_PolyLine, RectF(500F, 650F, 600F, 700F))
            val polyline = PolyLine(annot)
            vertexe_array = PointFArray()
            vertexe_array.add(PointF(515F, 705F))
            vertexe_array.add(PointF(535F, 740F))
            vertexe_array.add(PointF(565F, 740F))
            vertexe_array.add(PointF(585F, 705F))
            vertexe_array.add(PointF(565F, 665F))
            vertexe_array.add(PointF(535F, 665F))
            polyline.vertexes = vertexe_array
            polyline.subject = "PolyLine"
            polyline.title = "Foxit SDK"
            polyline.creationDateTime = currentDateTime
            polyline.modifiedDateTime = currentDateTime
            polyline.uniqueID = randomUUID(null)
            // Appearance should be reset.
            polyline.resetAppearanceStream()

            // Add freetext annotation, as type writer
            annot = page.addAnnot(Annot.e_FreeText, RectF(10F, 550F, 200F, 600F))
            var freetext = FreeText(annot)
            // Set default appearance
            var default_ap = DefaultAppearance()
            default_ap.flags = DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
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
            freetext.creationDateTime = currentDateTime
            freetext.modifiedDateTime = currentDateTime
            freetext.uniqueID = randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add freetext annotation, as call-out
            annot = page.addAnnot(Annot.e_FreeText, RectF(300F, 550F, 400F, 600F))
            freetext = FreeText(annot)
            // Set default appearance
            default_ap = DefaultAppearance()
            default_ap.flags = DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
            default_ap.font = Font(Font.e_StdIDHelveticaB)
            default_ap.text_size = 12.0f
            default_ap.text_color = 0x000000
            // Set default appearance for form.
            freetext.defaultAppearance = default_ap
            freetext.alignment = Constants.e_AlignmentCenter
            freetext.intent = "FreeTextCallout"
            val callout_points = PointFArray()
            callout_points.add(PointF(250F, 540F))
            callout_points.add(PointF(280F, 570F))
            callout_points.add(PointF(300F, 570F))
            freetext.calloutLinePoints = callout_points
            freetext.calloutLineEndingStyle = Markup.e_EndingStyleOpenArrow
            freetext.content = "A callout annotation."
            freetext.subject = "FreeTextCallout"
            freetext.title = "Foxit SDK"
            freetext.creationDateTime = currentDateTime
            freetext.modifiedDateTime = currentDateTime
            freetext.uniqueID = randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add freetext annotation, as text box
            annot = page.addAnnot(Annot.e_FreeText, RectF(450F, 550F, 550F, 600F))
            freetext = FreeText(annot)
            // Set default appearance
            default_ap = DefaultAppearance()
            default_ap.flags = DefaultAppearance.e_FlagFont or DefaultAppearance.e_FlagFontSize or DefaultAppearance.e_FlagTextColor
            default_ap.font = Font(Font.e_StdIDHelveticaI)
            default_ap.text_size = 12.0f
            default_ap.text_color = 0x000000
            // Set default appearance for form.
            freetext.defaultAppearance = default_ap
            freetext.alignment = Constants.e_AlignmentCenter
            freetext.content = "A text box annotation."
            freetext.subject = "Textbox"
            freetext.title = "Foxit SDK"
            freetext.creationDateTime = currentDateTime
            freetext.modifiedDateTime = currentDateTime
            freetext.uniqueID = randomUUID(null)
            // Appearance should be reset.
            freetext.resetAppearanceStream()

            // Add highlight annotation
            val highlight = Highlight(page.addAnnot(Annot.e_Highlight, RectF(10F, 450F, 100F, 550F)))
            highlight.content = "Highlight"
            var quad_points = QuadPoints()
            quad_points.first = PointF(10F, 500F)
            quad_points.second = PointF(90F, 500F)
            quad_points.third = PointF(10F, 480F)
            quad_points.fourth = PointF(90F, 480F)
            var quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            highlight.quadPoints = quad_points_array
            highlight.subject = "Highlight"
            highlight.title = "Foxit SDK"
            highlight.creationDateTime = currentDateTime
            highlight.modifiedDateTime = currentDateTime
            highlight.uniqueID = randomUUID(null)
            // Appearance should be reset.
            highlight.resetAppearanceStream()

            // Add underline annotation
            val underline = Underline(page.addAnnot(Annot.e_Underline, RectF(100F, 450F, 200F, 550F)))
            quad_points = QuadPoints()
            quad_points.first = PointF(110F, 500F)
            quad_points.second = PointF(190F, 500F)
            quad_points.third = PointF(110F, 480F)
            quad_points.fourth = PointF(190F, 480F)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            underline.quadPoints = quad_points_array
            underline.subject = "Underline"
            underline.title = "Foxit SDK"
            underline.creationDateTime = currentDateTime
            underline.modifiedDateTime = currentDateTime
            underline.uniqueID = randomUUID(null)
            // Appearance should be reset.
            underline.resetAppearanceStream()

            // Add squiggly annotation
            val squiggly = Squiggly(page.addAnnot(Annot.e_Squiggly, RectF(200F, 450F, 300F, 550F)))
            squiggly.intent = "Squiggly"
            quad_points = QuadPoints()
            quad_points.first = PointF(210F, 500F)
            quad_points.second = PointF(290F, 500F)
            quad_points.third = PointF(210F, 480F)
            quad_points.fourth = PointF(290F, 480F)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            squiggly.quadPoints = quad_points_array
            squiggly.subject = "Squiggly"
            squiggly.title = "Foxit SDK"
            squiggly.creationDateTime = currentDateTime
            squiggly.modifiedDateTime = currentDateTime
            squiggly.uniqueID = randomUUID(null)
            // Appearance should be reset.
            squiggly.resetAppearanceStream()

            // Add strikeout annotation
            val strikeout = StrikeOut(page.addAnnot(Annot.e_StrikeOut, RectF(300F, 450F, 400F, 550F)))
            quad_points = QuadPoints()
            quad_points.first = PointF(310F, 500F)
            quad_points.second = PointF(390F, 500F)
            quad_points.third = PointF(310F, 480F)
            quad_points.fourth = PointF(390F, 480F)
            quad_points_array = QuadPointsArray()
            quad_points_array.add(quad_points)
            strikeout.quadPoints = quad_points_array
            strikeout.subject = "StrikeOut"
            strikeout.title = "Foxit SDK"
            strikeout.creationDateTime = currentDateTime
            strikeout.modifiedDateTime = currentDateTime
            strikeout.uniqueID = randomUUID(null)
            // Appearance should be reset.
            strikeout.resetAppearanceStream()

            // Add caret annotation
            val caret = Caret(page.addAnnot(Annot.e_Caret, RectF(400F, 450F, 420F, 470F)))
            caret.innerRect = RectF(410F, 450F, 430F, 470F)
            caret.content = "Caret annotation"
            caret.subject = "Caret"
            caret.title = "Foxit SDK"
            caret.creationDateTime = currentDateTime
            caret.modifiedDateTime = currentDateTime
            caret.uniqueID = randomUUID(null)
            // Appearance should be reset.
            caret.resetAppearanceStream()

            // Add note annotation
            val note = Note(page.addAnnot(Annot.e_Note, RectF(10F, 350F, 50F, 400F)))
            note.iconName = "Comment"
            note.subject = "Note"
            note.title = "Foxit SDK"
            note.content = "Note annotation."
            note.creationDateTime = currentDateTime
            note.modifiedDateTime = currentDateTime
            note.uniqueID = randomUUID(null)
            // Add popup to note annotation
            val popup = Popup(page.addAnnot(Annot.e_Popup, RectF(300F, 450F, 500F, 550F)))
            popup.borderColor = 0x00FF00
            popup.openStatus = false
            popup.modifiedDateTime = currentDateTime
            note.popup = popup

            // Add reply annotation to note annotation
            val reply = note.addReply()
            reply.content = "reply"
            reply.modifiedDateTime = currentDateTime
            reply.title = "Foxit SDK"
            reply.uniqueID = randomUUID(null)
            reply.resetAppearanceStream()

            // Add state annotation to note annotation
            val state = Note(note.addStateAnnot("Foxit SDK", Markup.e_StateModelReview, Markup.e_StateAccepted))
            state.content = "Accepted set by Foxit SDK"
            state.uniqueID = randomUUID(null)
            state.resetAppearanceStream()
            // Appearance should be reset.
            note.resetAppearanceStream()

            // Add ink annotation
            val ink = Ink(page.addAnnot(Annot.e_Ink, RectF(100F, 350F, 200F, 450F)))
            val inklist = Path()
            val width = 100f
            val height = 100f
            val out_width = Math.min(width, height) * 2 / 3f
            val inner_width = (out_width * Math.sin(18f / 180f * 3.14f.toDouble()) / Math.sin(36f / 180f * 3.14f.toDouble())).toFloat()
            val center = PointF(150F, 400F)
            var x = out_width
            var y = 0f
            inklist.moveTo(PointF(center.x + x, center.y + y))
            for (i in 0..4) {
                x = (out_width * Math.cos(72f * i / 180f * 3.14f.toDouble())).toFloat()
                y = (out_width * Math.sin(72f * i / 180f * 3.14f.toDouble())).toFloat()
                inklist.lineTo(PointF(center.x + x, center.y + y))
                x = (inner_width * Math.cos((72f * i + 36) / 180f * 3.14f.toDouble())).toFloat()
                y = (inner_width * Math.sin((72f * i + 36) / 180f * 3.14f.toDouble())).toFloat()
                inklist.lineTo(PointF(center.x + x, center.y + y))
            }
            inklist.lineTo(PointF(center.x + out_width, center.y + 0))
            inklist.closeFigure()
            ink.inkList = inklist
            ink.subject = "Ink"
            ink.title = "Foxit SDK"
            ink.content = "Note annotation."
            ink.creationDateTime = currentDateTime
            ink.modifiedDateTime = currentDateTime
            ink.uniqueID = randomUUID(null)
            // Appearance should be reset.
            ink.resetAppearanceStream()

            // Add file attachment annotation
            val attachment_file = getFixFolder() + "AboutFoxit.pdf"
            val file_attachment = FileAttachment(
                    page.addAnnot(Annot.e_FileAttachment, RectF(280F, 350F, 300F, 380F)))
            file_attachment.iconName = "Graph"
            val file_spec = FileSpec(page.document)
            file_spec.fileName = "attachment.pdf"
            file_spec.creationDateTime = currentDateTime
            file_spec.description = "The original file"
            file_spec.modifiedDateTime = currentDateTime
            file_spec.embed(attachment_file)
            file_attachment.fileSpec = file_spec
            file_attachment.subject = "File Attachment"
            file_attachment.title = "Foxit SDK"
            // Appearance should be reset.
            file_attachment.resetAppearanceStream()

            // Add link annotation
            val link = Link(page.addAnnot(Annot.e_Link, RectF(350F, 350F, 380F, 400F)))
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
            val icon_provider = CustomIconProvider(context)
            Library.setAnnotIconProviderCallback(icon_provider)

            // Add common stamp annotation.
            Library.setActionCallback(null)
            icon_provider.setUseDynamicStamp(false)
            annot = page.addAnnot(Annot.e_Stamp, RectF(110F, 150F, 200F, 250F))
            val static_stamp = Stamp(annot)
            static_stamp.iconName = "Approved"
            // Appearance should be reset.
            static_stamp.resetAppearanceStream()

            // Add dynamic stamp annotation.
            val action_callback = CustomActionCallback()
            Library.setActionCallback(action_callback)
            icon_provider.setUseDynamicStamp(true)
            annot = page.addAnnot(Annot.e_Stamp, RectF(10F, 150F, 100F, 250F))
            val dynamic_stamp = Stamp(annot)
            dynamic_stamp.iconName = "Approved"
            // Appearance should be reset.
            dynamic_stamp.resetAppearanceStream()
        } catch (e: PDFException) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.fx_add_annot_error, e.message), Toast.LENGTH_LONG).show()
        }
    }

    init {
        inputFile = getFixFolder() + "annotation_input.pdf"
        outputFile = getOutputFilesFolder(Common.ANNOTATION) + "annotation_add.pdf"
    }
}