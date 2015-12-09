package com.fastebro.androidrgbtool.print;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
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
import com.fastebro.androidrgbtool.model.PaletteSwatch;
import com.fastebro.androidrgbtool.utils.PaletteUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by danielealtomare on 01/03/15.
 * Project: rgb-tool
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class RGBToolPrintColorDetailsAdapter extends PrintDocumentAdapter {
    private Context context;
    private String message;
    private String filename;
    private int color;
    private int complementaryColor;
    private int contrastColor;

    private PrintedPdfDocument pdfDocument;

    public RGBToolPrintColorDetailsAdapter(@NonNull Context context,
                                           String message,
                                           String filename,
                                           int color,
                                           int complementaryColor,
                                           int contrastColor) {
        this.context = context;
        this.message = message;
        this.filename = filename;
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
                    "rgbtool_" + filename + "_colors.pdf")
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


    private int computePageCount(PrintAttributes printAttributes) {
        return 1;
    }

    private int getPrintItemCount() {
        return 1;
    }

    private void drawPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();
        StringBuilder token;

        // units are in points (1/72 of an inch)
        int titleBaseLine = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText(context.getString(R.string.app_name), leftMargin, titleBaseLine, paint);

        // FIXME: set correct text!!!

        // Color
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_complementary), leftMargin, titleBaseLine + 25, paint);

        paint.setColor(color);
        canvas.drawRect(leftMargin, titleBaseLine + 90, 126, titleBaseLine + 90 + 30, paint);

        // Complementary
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_complementary), leftMargin, titleBaseLine + 25, paint);

        paint.setColor(complementaryColor);
        canvas.drawRect(leftMargin, titleBaseLine + 190, 126, titleBaseLine + 190 + 30, paint);

        // Contrast
        paint.setTextSize(16);
        canvas.drawText(context.getString(R.string.color_details_contrast), leftMargin, titleBaseLine + 25, paint);

        paint.setColor(contrastColor);
        canvas.drawRect(leftMargin, titleBaseLine + 280, 126, titleBaseLine + 280 + 30, paint);

        // User message.
        if (message != null) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(10);
            canvas.drawText(message, leftMargin, titleBaseLine + 360, paint);
        }
    }
}
