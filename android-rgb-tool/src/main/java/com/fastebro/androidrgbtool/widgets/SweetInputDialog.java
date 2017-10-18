package com.fastebro.androidrgbtool.widgets;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fastebro.androidrgbtool.R;
import com.fastebro.androidrgbtool.utils.ThemeWrapper;

/**
 * Created by Snow Volf on 20.08.2017, 12:54
 */

public class SweetInputDialog extends BottomSheetDialog {
    private Context mContext;
    private TextView mContentView;
    private TextInputLayout mInputContainer;
    private TextInputEditText editText;
    private Button mPositive, mNegative;

    public SweetInputDialog(@NonNull Context context) {
        super(context, ThemeWrapper.getDialogTheme());
        mContext = context;
        initContentView();
    }

    private void initContentView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_sweet_input, null);
        mContentView = view.findViewById(R.id.title);
        mInputContainer = view.findViewById(R.id.field_container);
        editText = view.findViewById(R.id.field_edit);
        mPositive = view.findViewById(R.id.positive);
        mNegative = view.findViewById(R.id.negative);
        setContentView(view);
    }

    /**
     * Set Dialog title
     * */
    @Override
    public void setTitle(CharSequence title){
        mContentView.setText(title);
    }

    /**
     * Set positive button text & listener
     * listener attached to View (Button)
     * */
    public void setPositiveButton(int text, View.OnClickListener listener){
        mPositive.setText(text);
        mPositive.setOnClickListener(listener);
    }

    /**
     * Set negative button text & listener
     * listener attached to View (Button)
     * */
    public void setNegativeButton(int text, View.OnClickListener listener){
        mNegative.setText(text);
        mNegative.setOnClickListener(listener);
    }

    /**
     * Get text from input filed
     * @return input string
     * */
    public String getInputString(){
        return editText.getText().toString();
    }

    /**
     * Set hint for TextInputLayout
     * @see TextInputLayout#setHint(CharSequence)
     * */
    public void setHint(@StringRes int hint){
        mInputContainer.setHint(mContext.getString(hint));
    }

    /**
     * Set default text
     * selection sets automatically
     * */
    public void setInputString(String inputString) {
        editText.setText(inputString);
        editText.setSelection(editText.getText().length());
    }

    /**
     * Set maximum number of lines for EditText
     * */
    public void setMaxLines(int linesCount){
        editText.setLines(linesCount);
    }

    /**
     * Sets suggested characters.
     * If specified, user can type only this characters
     * */
    public void setCharactersToUse(String charactersToUse){
       editText.setKeyListener(DigitsKeyListener.getInstance(charactersToUse));
    }

    /**
     * @return editText length
     * */
    public int length(){
        if (editText.getText() != null) {
            return editText.getText().length();
        }
        return 0;
    }

    /**
     * Set custom selection.
     * by default, selection sets to the end of the text
     * */
    public void setSelection(int selection){
        editText.setSelection(selection);
    }
}
