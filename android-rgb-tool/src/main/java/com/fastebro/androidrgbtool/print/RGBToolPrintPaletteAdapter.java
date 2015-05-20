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
import android.widget.Toast;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.model.PaletteSwatch;
import com.fastebro.androidrgbtool.utils.UPalette;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by danielealtomare on 01/03/15.
 * Project: rgb-tool
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class RGBToolPrintPaletteAdapter extends PrintDocumentAdapter {
    private Context context;
    private String message;
    private String filename;
    private ArrayList<PaletteSwatch> swatches;

    private PrintedPdfDocument pdfDocument;

    public RGBToolPrintPaletteAdapter(Context context,
                                      String message,
                                      String filename,
                                      ArrayList<PaletteSwatch> swatches) {
        this.context = context;
        this.message = message;
        this.filename = filename;
        this.swatches = swatches;
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
                    "rgbtool_" + filename + "_palette.pdf")
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

        paint.setTextSize(16);
        canvas.drawText(filename + " palette", leftMargin, titleBaseLine + 25, paint);

        // Color description summary.
        paint.setTextSize(14);
        for(int i = 0; i < swatches.size(); i++) {
            paint.setColor(Color.BLACK);
            token = new StringBuilder();
            token.append("Type: ");
            token.append(UPalette.getSwatchDescription(context, swatches.get(i).getType()));
            canvas.drawText(token.toString(), leftMargin, titleBaseLine + (50 + (i * 100)), paint);

            token = new StringBuilder();
            token.append("HEX: ");
            token.append(Integer.toHexString(swatches.get(i).getRgb()).toUpperCase());
            canvas.drawText(token.toString(), leftMargin, titleBaseLine + (75 + (i * 100)), paint);

            paint.setColor(swatches.get(i).getRgb());
            canvas.drawRect(leftMargin,
                    titleBaseLine + (90 + (i * 100)),
                    126,
                    titleBaseLine + (90 + (i * 100)) + 30,
                    paint);
        }

        // User message.
        if (message != null) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(10);
            canvas.drawText(message, leftMargin, titleBaseLine + (50 + (swatches.size() * 100)) + 10, paint);
        }
    }
}
