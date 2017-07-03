package chenhao.lib.onecode.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import chenhao.lib.onecode.R;
import chenhao.lib.onecode.utils.StringUtils;

public class AlertEdit extends AlertBase {

    public interface OnAlertEditListener {
        void initEditView(EditText et);

        boolean onSubmit(String s);
    }

    private EditText editText;
    private boolean submitLeft;
    private String leftStr, rightStr;
    private OnAlertEditListener editListener;

    public AlertEdit(Context c, OnAlertEditListener listener) {
        super(c);
        this.editListener = listener;
        setCloseClean(true);
    }

    @Override
    public AlertBase create() {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createDialog(R.layout.onecode_alert_edit, params);
        return this;
    }

    public AlertEdit setButton(boolean submitLeft, String leftStr, String rightStr) {
        this.submitLeft = submitLeft;
        this.leftStr = leftStr;
        this.rightStr = rightStr;
        return this;
    }

    @Override
    public void createDialogInit(View layout, Dialog d) {
        super.createDialogInit(layout, d);
        editText = (EditText) layout.findViewById(R.id.alert_content);
        if (null != editListener) {
            editListener.initEditView(editText);
        }
        int btCount = 0;
        if (StringUtils.isEmpty(leftStr)) {
            layout.findViewById(R.id.alert_bt_left).setVisibility(View.GONE);
        } else {
            btCount += 1;
            layout.findViewById(R.id.alert_bt_left).setVisibility(View.VISIBLE);
            ((Button) layout.findViewById(R.id.alert_bt_left)).setText(leftStr);
            layout.findViewById(R.id.alert_bt_left)
                    .setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (submitLeft) {
                                if (null == editListener || editListener.onSubmit(editText.getText().toString())) {
                                    close();
                                }
                            } else {
                                close();
                            }
                        }
                    });
        }
        if (StringUtils.isEmpty(rightStr)) {
            layout.findViewById(R.id.alert_bt_right).setVisibility(View.GONE);
        } else {
            btCount += 1;
            layout.findViewById(R.id.alert_bt_right).setVisibility(View.VISIBLE);
            ((Button) layout.findViewById(R.id.alert_bt_right)).setText(rightStr);
            layout.findViewById(R.id.alert_bt_right)
                    .setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (submitLeft) {
                                close();
                            } else if (null == editListener || editListener.onSubmit(editText.getText().toString())) {
                                close();
                            }
                        }
                    });
        }
        if (btCount == 2) {
            layout.findViewById(R.id.alert_bt_line).setVisibility(View.VISIBLE);
        } else {
            layout.findViewById(R.id.alert_bt_line).setVisibility(View.GONE);
        }
    }

}