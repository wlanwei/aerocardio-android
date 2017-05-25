package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.db.DBEcgMark;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.share.ShareSDKUtils;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.EcgMarks;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

import java.util.List;
import java.util.Locale;

/**
 * 历史详情页面
 * Created by wd
 */
public class AeroCardioHistoryDetailActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private DBDetection detection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            goBack();
            return;
        }
        detection = (DBDetection) intent.getSerializableExtra(DBDetection.class.getSimpleName());
        if (detection == null) {
            goBack();
            return;
        }
        setContentView(R.layout.history_detail);
        MobclickEvent.onEvent(this, MobclickEvent.EventId_DetectionHistory);
    }

    @Override
    protected void initViews() {
        findViewById(R.id.history_detail_iv_back).setOnClickListener(this);
        findViewById(R.id.history_detail_iv_share).setOnClickListener(this);

        TextView.class.cast(findViewById(R.id.history_detail_tv_detection_date)).
                setText(detection.getDateStrCN() + "\n" +
                        detection.getStartTimeStrCN() + " - " + detection.getStopTimeStrCN());
        TextView.class.cast(findViewById(R.id.history_detail_tv_detection_time)).
                setText(detection.getDuration());

        if (detection.getMarks().isEmpty()) {
            AppNetTcpComm.getEcgMark().queryAppMarkCounts(
                    User.getUser().getIdString()
                    , detection.getStartTime(), detection.getStopTime(),
                    new AppNetTcpCommListener<List<EcgMarks>>() {
                        @Override
                        public void onResponse(boolean success, List<EcgMarks> response) {
                            L.e("queryAppMarkCounts -> success: " + success);
                            if (success && response != null) {
                                for (EcgMarks ms : response) {
                                    if (ms != null) {
                                        L.e("queryAppMarkCounts -> EcgMarks: " + ms.toString());
                                        for (EcgMark m : ms.getMarks()) {
                                            detection.addMark(new DBEcgMark(m));
                                        }
                                    }
                                }
                                showEcgMarks();
                            } else {
                                Toast.makeText(AeroCardioHistoryDetailActivity.this,
                                        R.string.http_conn_net, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            showEcgMarks();
        }
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return false;
    }

    @Override
    protected boolean enableServiceConnection() {
        return false;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.history_detail_iv_back:
                goBack();
                break;
            case R.id.history_detail_iv_share:
                showShare();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    private void goBack() {
        this.finish();
    }

    private void showEcgMarks() {
        TextView.class.cast(findViewById(R.id.history_detail_tv_detection_signal)).
                setText(detection.getMarkStats().getSQ());
        TextView.class.cast(findViewById(R.id.history_detail_tv_heart_rate)).
                setText(String.valueOf(detection.getMarkStats().getHR()));
        TextView.class.cast(findViewById(R.id.history_detail_tv_respiratory_rate)).
                setText(String.valueOf(detection.getMarkStats().getBR()));
        TextView.class.cast(findViewById(R.id.history_detail_tv_heart_health)).
                setText(String.valueOf(detection.getMarkStats().getHRHealth()));

        if (detection.getMarkStats().getXLBQ() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_arrhythmia)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getXLBQ()));
        } else {
            findViewById(R.id.history_detail_ll_arrhythmia).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getXLGS() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_heart_rate_fast)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getXLGS()));
        } else {
            findViewById(R.id.history_detail_ll_heart_rate_fast).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getXLGH() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_heart_rate_slow)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getXLGH()));
        } else {
            findViewById(R.id.history_detail_ll_heart_rate_slow).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getSXZB() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_ventricular_premature_beat)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getSXZB()));
        } else {
            findViewById(R.id.history_detail_ll_ventricular_premature_beat).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getFXZB() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_room_sex_premature_beat)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getFXZB()));
        } else {
            findViewById(R.id.history_detail_ll_room_sex_premature_beat).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getSC() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_ventricular_fibrillation)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getSC()));
        } else {
            findViewById(R.id.history_detail_ll_ventricular_fibrillation).setVisibility(View.GONE);
        }
        if (detection.getMarkStats().getFC() > 0) {
            TextView.class.cast(findViewById(R.id.history_detail_tv_atrial_fibrillation)).
                    setText(String.format(Locale.getDefault(), "%s 次", detection.getMarkStats().getFC()));
        } else {
            findViewById(R.id.history_detail_ll_atrial_fibrillation).setVisibility(View.GONE);
        }

        TextView.class.cast(findViewById(R.id.history_detail_tv_detection_conclusion)).
                setText(detection.getMarkStats().getConclusion());

        if (detection.getMarkStats().getHealthHRLevel() != 0) {
            showShare();
        }
    }

    private void showShare() {
        ShareSDKUtils.shareContent(this,
                "平均心律:" +
                        TextView.class.cast(findViewById(R.id.history_detail_tv_heart_rate)).getText() +
                        "\n正常心律范围" +
                        TextView.class.cast(findViewById(R.id.history_detail_tv_heart_health)).getText() +
                        "%");
    }

}
