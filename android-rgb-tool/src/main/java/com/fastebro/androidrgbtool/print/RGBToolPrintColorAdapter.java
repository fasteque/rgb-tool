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
import com.fastebro.androidrgbtool.utils.UColor;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by danielealtomare on 25/03/14.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class RGBToolPrintColorAdapter extends PrintDocumentAdapter {
    private Context context;
    private String message;
    private float RGBRColor;
    private float RGBGColor;
    private float RGBBColor;
    private float RGBOpacity;

    private PrintedPdfDocument pdfDocument;

    public RGBToolPrintColorAdapter(Context context,
                                    String message,
                                    float rgbRColor,
                                    float rgbGColor,
                                    float rgbBColor,
                                    float rgbOpacity) {
        this.context = context;
        this.message = message;
        RGBRColor = rgbRColor;
        RGBGColor = rgbGColor;
        RGBBColor = rgbBColor;
        RGBOpacity = rgbOpacity;
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
                    "rgbtool_" +
                            String.format("%s%s%s%s",
                                    UColor.RGBToHex(RGBOpacity),
                                    UColor.RGBToHex(RGBRColor),
                                    UColor.RGBToHex(RGBGColor),
                                    UColor.RGBToHex(RGBBColor)) +
                            ".pdf"
            )
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
        StringBuilder token = new StringBuilder();

        // units are in points (1/72 of an inch)
        int titleBaseLine = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText(context.getString(R.string.app_name), leftMargin, titleBaseLine, paint);

        // Color description summary.
        paint.setTextSize(14);
        token.append("R: " + UColor.getRGB(RGBRColor));
        token.append("  G: " + UColor.getRGB(RGBGColor));
        token.append("  B: " + UColor.getRGB(RGBBColor));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 25, paint);

        token = new StringBuilder();
        token.append("Opacity: " + UColor.getRGB(RGBOpacity));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 50, paint);

        token = new StringBuilder();
        float[] hsb = UColor.RGBToHSB(RGBRColor, RGBGColor, RGBBColor);
        token.append("H: " + String.format("%.0f", hsb[0]));
        token.append("  S: " + String.format("%.0f%%", (hsb[1] * 100.0f)));
        token.append("  B: " + String.format("%.0f%%", (hsb[2] * 100.0f)));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 75, paint);

        token = new StringBuilder();
        token.append("HEX - " + String.format("#%s%s%s%s",
                UColor.RGBToHex(RGBOpacity),
                UColor.RGBToHex(RGBRColor),
                UColor.RGBToHex(RGBGColor),
                UColor.RGBToHex(RGBBColor)));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 100, paint);

        paint.setColor(Color.argb((int) RGBOpacity, (int) RGBRColor,
                (int) RGBGColor, (int) RGBBColor));
        canvas.drawRect(leftMargin, titleBaseLine + 125, 126, 269, paint);

        // User message.
        if (message != null) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(11);
            canvas.drawText(message, leftMargin, 294, paint);
        }
    }
}
