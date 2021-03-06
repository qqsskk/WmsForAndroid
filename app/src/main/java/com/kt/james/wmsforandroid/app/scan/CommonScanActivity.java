package com.kt.james.wmsforandroid.app.scan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.kt.james.wmsforandroid.R;
import com.kt.james.wmsforandroid.app.scan.dto.CheckLocBean;
import com.kt.james.wmsforandroid.app.scan.dto.ItemBarcodeBean;
import com.kt.james.wmsforandroid.base.BaseActivity;
import com.kt.james.wmsforandroid.databinding.ActivityCommonScanBinding;
import com.kt.james.wmsforandroid.utils.ResourceUtil;
import com.kt.james.wmsforandroid.utils.ToastUtil;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

@Route(path = "/scan/common_scan_activity", name = "扫码页面")
public class CommonScanActivity extends BaseActivity<CommonScanViewModel, ActivityCommonScanBinding> {
    private static final String TAG = "CommonScanActivity";
    public static final int ITEM = 0x00;
    public static final int LOC = 0x01;

    public static final int STRING = 0x02;

    public static final String RESULT_TAG = "result";

    @Autowired
    public int from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_scan);
        ARouter.getInstance().inject(this);
        CaptureFragment captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_scan_barcode);
        captureFragment.setAnalyzeCallback(codeCallBack);
        getSupportFragmentManager().beginTransaction().replace(R.id.scan_container, captureFragment).commit();
        setTitle(ResourceUtil.getString(R.string.scan_title));
        viewModel.which.set(from);
        bindingView.setViewModel(viewModel);
        showContentView();
    }

    CodeUtils.AnalyzeCallback codeCallBack = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Log.e(TAG, "onAnalyzeSuccess: " + result);
            if (from == ITEM) {
                viewModel.requestItemCode(result).observe(CommonScanActivity.this, CommonScanActivity.this::itemCallback);
            } else if (from == LOC){
                viewModel.requestLocCode(result).observe(CommonScanActivity.this, CommonScanActivity.this::locCallback);
            } else {
                bindingView.tvString.setText(result);
            }
        }

        @Override
        public void onAnalyzeFailed() {
            ToastUtil.showToast("该条码无效！");
        }
    };

    private void itemCallback(ItemBarcodeBean result) {
        if (result != null) {
            bindingView.tvItemName.setText(result.getName());
            bindingView.tvItemSpec.setText(result.getSpec());
            bindingView.tvItemBrand.setText(result.getFactory());
        }
    }

    private void locCallback(CheckLocBean result) {
        if (result != null && !TextUtils.isEmpty(result.getName())) {
            bindingView.tvLocCode.setText(result.getName());
        }
    }

    public void onConfirm(View view) {
        if (!viewModel.isReady()) {
            return;
        }
        Intent result = new Intent();
        if (from == ITEM) {
            result.putExtra(RESULT_TAG, viewModel.getItemResult());
        } else if (from == LOC){
            result.putExtra(RESULT_TAG, viewModel.getLocResult());
        } else {
            result.putExtra(RESULT_TAG, viewModel.getStringResult());
        }
        setResult(RESULT_OK, result);
        finish();
    }

}
