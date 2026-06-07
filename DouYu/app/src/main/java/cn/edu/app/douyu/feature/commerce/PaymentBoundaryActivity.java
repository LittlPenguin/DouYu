package cn.edu.app.douyu.feature.commerce;

import cn.edu.app.douyu.R;
import cn.edu.app.douyu.ui.XmlPageActivity;

public class PaymentBoundaryActivity extends XmlPageActivity {
    @Override
    protected int layoutRes() {
        return R.layout.activity_payment_boundary;
    }

    @Override
    protected String title() {
        return "支付边界";
    }
}
