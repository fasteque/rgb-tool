package com.fastebro.androidrgbtool.print;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ColorUtils;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by danielealtomare on 01/03/15.
 * Project: rgb-tool
 */
public class RGBToolPrintColorDetailsAdapter extends PrintDocumentAdapter {
    private final Context context;
    private final String message;
    private final int color;
    private final int complementaryColor;
    private final int contrastColor;

    private PrintedPdfDocument pdfDocument;

    public RGBToolPrintColorDetailsAdapter(@NonNull Context context,
                                           String message,
                                           int color,
                                           int complementaryColor,
                                           int contrastColor) {
        this.context = context;
        this.message = message;
        this.color = color;
        this.complementaryColor = complementaryColor;
        this.contrastColor = contrastColor;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes,
                         PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, Bundle extras) {
        // Create a new PdfDocument with the requested page attributes
        pdfDocument = new PrintedPdfDocument(context, newAttributes);

        // Respond to cancellation request
        if (cancellationSignal.isCanceled()) {
            Toast.makeText(context, context.getString(R.string.print_job_canceled),
                    Toast.LENGTH_SHORT).show();

            callback.onLayoutCancelled();

            return;
        }

        // Compute the expected number of printed pages
        int pages = computePageCount(newAttributes);

        if (pages > 0) {
            // Return print information to print framework
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(
                    "rgbtool_" + ColorUtils.RGBToHex(color) + "_color_details.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pages)
                    .build();

            // Content layout reflow is complete
            callback.onLayoutFinished(info, true);
        } else {
            // Otherwise report an error to the print framework
            callback.onLayoutFailed("Page count calculation failed.");
        }
    }

    @Override
    public void onWrite(PageRange[] pages,
                        ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback callback) {
        PdfDocument.Page page = pdfDocument.startPage(0);

        // check for cancellation
        if (cancellationSignal.isCanceled()) {
            Toast.makeText(context, context.getString(R.string.print_job_canceled),
                    Toast.LENGTH_SHORT).show();

            callback.onWriteCancelled();
            pdfDocument.close();
            pdfDocument = null;

            return;
        }

        // Draw page content for printing
        drawPage(page);

        // Rendering is complete, so page can be finalized.
        pdfDocument.finishPage(page);

        // Write PDF document to file
        try {
            pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.print_error),
                    Toast.LENGTH_SHORT).show();

            callback.onWriteFailed(e.toString());

            return;
        } finally {
            pdfDocument.close();
            pdfDocument = null;
        }

        // Signal the print framework the document is complete
        callback.onWriteFinished(pages);
    }


    @SuppressWarnings("SameReturnValue")
    private int computePageCount(PrintAttributes printAttributes) {
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private int getPrintItemCount() {
        return 1;
    }

    private void drawPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();

        // units are in points (1/72 of an inch)
        int itemYCoordinate = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText(context.getString(R.string.app_name), leftMargin, itemYCoordinate, paint);

        itemYCoordinate += 25;
        // Color
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_color, ColorUtils.RGBToHex(color)), leftMargin, itemYCoordinate, paint);

        paint.setColor(color);
        canvas.drawRect(leftMargin, itemYCoordinate + 20, leftMargin + 50, itemYCoordinate + 70, paint);

        itemYCoordinate += 110;
        // Complementary
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_complementary, ColorUtils.RGBToHex
                (complementaryColor)), leftMargin,
                itemYCoordinate,
                paint);

        paint.setColor(complementaryColor);
        canvas.drawRect(leftMargin, itemYCoordinate + 20, leftMargin + 50, itemYCoordinate + 70, paint);

        itemYCoordinate += 110;
        // Contrast
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_contrast, ColorUtils.RGBToHex(contrastColor)),
                leftMargin,
                itemYCoordinate, paint);

        paint.setColor(contrastColor);
        canvas.drawRect(leftMargin, itemYCoordinate + 20, leftMargin + 50, itemYCoordinate + 70, paint);

        // User message.
        if (message != null) {
            itemYCoordinate += 110;
            paint.setColor(Color.BLACK);
            paint.setTextSize(10);
            canvas.drawText(message, leftMargin, itemYCoordinate, paint);
        }
    }
}
