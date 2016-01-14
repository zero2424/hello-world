package com.example.kaizhang.kaiapplication;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kaizhang.kaiapplication.view.MultiPartSwitcher;

/**
 * Created by kai zhang on 2015/12/21.
 */
public class ScrollTextActivity extends Activity implements MultiPartSwitcher.SwitherListener {
    private MultiPartSwitcher switcher;
    private float smallTextSizeSp = 25, bigTextSizeSp = 45;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_text);
        switcher = (MultiPartSwitcher) findViewById(R.id.switcher);
        switcher.setItemLayoutId(R.layout.main_home_short_info_item);
        switcher.setSwitherListener(this);
        initShortInfoViews(new String[]{"1", "2", "3", "4"}, new String[]{"已体验天数", "呵呵", "1895", "23.2"});
        Button eb = (Button) findViewById(R.id.es_btn);
        Button sm = (Button) findViewById(R.id.sm_btn);
        Button ec_btn = (Button) findViewById(R.id.ec_btn);
        eb.setOnClickListener(new View.OnClickListener() {
            int i = 1;

            @Override
            public void onClick(View v) {
                switcher.enableScroll(i % 2 == 0);
                i++;
            }
        });
        sm.setOnClickListener(new View.OnClickListener() {
            int i = 0;

            @Override
            public void onClick(View v) {
                View view = switcher.getOne(2);
                TextView tv1 = (TextView) view.findViewById(R.id.sit_tv1);
                tv1.setText("i=" + i);
                i++;
            }
        });
        ec_btn.setOnClickListener(new View.OnClickListener() {
            int i = 1;

            @Override
            public void onClick(View v) {
                switcher.setIsCyclic(i % 2 == 0);
                i++;
            }
        });
    }

    private void initShortInfoViews(String[] titles, String[] subtitles) {
        for (int i = 0; i < titles.length; i++) {
            View view = switcher.addOne();
            TextView tv1 = (TextView) view.findViewById(R.id.sit_tv1);
            if ((titles.length > 2 && i == 1) || titles.length == 1 || (titles.length == 2 && i == 0)) {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, bigTextSizeSp);
            } else {
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSizeSp);
            }
            TextView tv2 = (TextView) view.findViewById(R.id.sit_tv2);
            tv1.setText(titles[i]);
            tv2.setText(subtitles[i]);
        }
    }

    private void changeSize(final TextView view, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (Float) valueAnimator.getAnimatedValue();
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, val);
            }
        });
        anim.setDuration(switcher.getDuration());
        anim.start();
    }

    @Override
    public void cloneView(View cloneView, View originalView) {
        TextView otv1 = (TextView) originalView.findViewById(R.id.sit_tv1);
        TextView otv2 = (TextView) originalView.findViewById(R.id.sit_tv2);
        TextView ctv1 = (TextView) cloneView.findViewById(R.id.sit_tv1);
        TextView ctv2 = (TextView) cloneView.findViewById(R.id.sit_tv2);
        ctv1.setText(otv1.getText());
        ctv2.setText(otv2.getText());
        ctv1.setTextSize(smallTextSizeSp);
    }

    @Override
    public void onStartScrollView(View viewToScroll, int curPosition, MultiPartSwitcher.Direction direction) {
        if (curPosition == 1) {
            changeSize((TextView) viewToScroll.findViewById(R.id.sit_tv1), bigTextSizeSp, smallTextSizeSp);
        } else if ((curPosition == 0 && direction == MultiPartSwitcher.Direction.NEXT) ||
                (curPosition == 2 && direction == MultiPartSwitcher.Direction.PREVIOUS)) {
            changeSize((TextView) viewToScroll.findViewById(R.id.sit_tv1), smallTextSizeSp, bigTextSizeSp);
        }
    }
}
