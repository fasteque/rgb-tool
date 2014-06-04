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
public class RGBToolPrintDocumentAdapter extends PrintDocumentAdapter
{
    private Context mContext;
    private String mMessage;
    private float mRGBRColor;
    private float mRGBGColor;
    private float mRGBBColor;
    private float mRGBOpacity;

    private PrintedPdfDocument mPdfDocument;


    public RGBToolPrintDocumentAdapter(Context context,
                                       String message,
                                       float rgbRColor,
                                       float rgbGColor,
                                       float rgbBColor,
                                       float rgbOpacity)
    {
        mContext = context;
        mMessage = message;
        mRGBRColor = rgbRColor;
        mRGBGColor = rgbGColor;
        mRGBBColor = rgbBColor;
        mRGBOpacity = rgbOpacity;
    }


    @Override
    public void onStart()
    {
        super.onStart();
    }


    @Override
    public void onFinish()
    {
        super.onFinish();
    }


    @Override
    public void onLayout(PrintAttributes oldAttributes,
                         PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback, Bundle extras)
    {
        // Create a new PdfDocument with the requested page attributes
        mPdfDocument = new PrintedPdfDocument(mContext, newAttributes);

        // Respond to cancellation request
        if (cancellationSignal.isCanceled())
        {
            Toast.makeText(mContext, mContext.getString(R.string.print_job_canceled),
                    Toast.LENGTH_SHORT).show();

            callback.onLayoutCancelled();

            return;
        }

        // Compute the expected number of printed pages
        int pages = computePageCount(newAttributes);

        if (pages > 0)
        {
            // Return print information to print framework
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(
                    "rgbtool_" +
                            String.format("%s%s%s%s",
                                    UColor.RGBToHex(mRGBOpacity),
                                    UColor.RGBToHex(mRGBRColor),
                                    UColor.RGBToHex(mRGBGColor),
                                    UColor.RGBToHex(mRGBBColor)) +
                    ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pages)
                    .build();

            // Content layout reflow is complete
            callback.onLayoutFinished(info, true);
        }
        else
        {
            // Otherwise report an error to the print framework
            callback.onLayoutFailed("Page count calculation failed.");
        }

    }


    @Override
    public void onWrite(PageRange[] pages,
                        ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback callback)
    {
        // Iterate over each page of the document,
        // check if it's in the output range.
        for (int i = 0; i < 1; i++)
        {
            // Check to see if this page is in the output range.
//            if (containsPage(pageRanges, i))
//            {
                // If so, add it to writtenPagesArray. writtenPagesArray.size()
                // is used to compute the next output page index.
//                writtenPagesArray.append(writtenPagesArray.size(), i);
                PdfDocument.Page page = mPdfDocument.startPage(i);

                // check for cancellation
                if (cancellationSignal.isCanceled())
                {
                    Toast.makeText(mContext, mContext.getString(R.string.print_job_canceled),
                            Toast.LENGTH_SHORT).show();

                    callback.onWriteCancelled();
                    mPdfDocument.close();
                    mPdfDocument = null;

                    return;
                }

                // Draw page content for printing
                drawPage(page);

                // Rendering is complete, so page can be finalized.
                mPdfDocument.finishPage(page);
            }
//        }

        // Write PDF document to file
        try
        {
            mPdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
        }
        catch (IOException e)
        {
            Toast.makeText(mContext, mContext.getString(R.string.print_error),
                    Toast.LENGTH_SHORT).show();

            callback.onWriteFailed(e.toString());

            return;
        }
        finally
        {
            mPdfDocument.close();
            mPdfDocument = null;
        }

//        PageRange[] writtenPages = computeWrittenPages();

        // Signal the print framework the document is complete
        callback.onWriteFinished(pages);
    }


    private int computePageCount(PrintAttributes printAttributes)
    {
        return 1;
    }


    private int getPrintItemCount()
    {
        return 1;
    }


    private void drawPage(PdfDocument.Page page)
    {
        Canvas canvas = page.getCanvas();
        StringBuilder token = new StringBuilder();

        // units are in points (1/72 of an inch)
        int titleBaseLine = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText(mContext.getString(R.string.app_name), leftMargin, titleBaseLine, paint);

        // Color description summary.
        paint.setTextSize(14);
        token.append("R: " + UColor.getRGB(mRGBRColor));
        token.append("  G: " + UColor.getRGB(mRGBGColor));
        token.append("  B: " + UColor.getRGB(mRGBBColor));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 25, paint);

        token = new StringBuilder();
        token.append("Opacity: " + UColor.getRGB(mRGBOpacity));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 50, paint);

        token = new StringBuilder();
        float[] hsb = UColor.RGBToHSB(mRGBRColor, mRGBGColor, mRGBBColor);
        token.append("H: " + String.format("%.0f", hsb[0]));
        token.append("  S: " + String.format("%.0f%%", (hsb[1] * 100.0f)));
        token.append("  B: " + String.format("%.0f%%", (hsb[2] * 100.0f)));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 75, paint);

        token = new StringBuilder();
        token.append("HEX - " + String.format("#%s%s%s%s",
                UColor.RGBToHex(mRGBOpacity),
                UColor.RGBToHex(mRGBRColor),
                UColor.RGBToHex(mRGBGColor),
                UColor.RGBToHex(mRGBBColor)));
        canvas.drawText(token.toString(), leftMargin, titleBaseLine + 100, paint);

        paint.setColor(Color.argb((int)mRGBOpacity, (int)mRGBRColor,
                (int)mRGBGColor, (int)mRGBBColor));
        canvas.drawRect(leftMargin, titleBaseLine + 125, 126, 269, paint);

        // User message.
        if(mMessage != null)
        {
            paint.setColor(Color.BLACK);
            paint.setTextSize(11);
            canvas.drawText(mMessage, leftMargin, 294, paint);
        }
    }
}
