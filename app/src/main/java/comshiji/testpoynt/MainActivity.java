package comshiji.testpoynt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import co.poynt.api.model.Order;
import co.poynt.api.model.OrderAmounts;
import co.poynt.api.model.OrderItem;
import co.poynt.api.model.OrderItemStatus;
import co.poynt.api.model.OrderStatus;
import co.poynt.api.model.OrderStatuses;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.api.model.UnitOfMeasure;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PaymentStatus;
import co.poynt.os.util.StringUtil;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button auth,compAuth,offAuth,voidAuth,sale,voidsale,refund;
    private Button showAll,saveAll,packUp,testScan;
    private EditText inputIdEd,inputEdCurrecy;
    private TextView scanTv;

    private static final int COLLECT_PAYMENT_REFS_REQUEST = 13134;
    private static final int SCANNER_REQUEST_CODE = 46576;
    private static final int CHECK_TRACKES_CODE=8979;

    private Payment mPayment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        auth=findViewById(R.id.auth_tv);
        compAuth=findViewById(R.id.caputer_tv);
        offAuth = findViewById(R.id.offline_tv);
        voidAuth=findViewById(R.id.void_auth_tv);
        sale=findViewById(R.id.sale_tv);
        voidsale=findViewById(R.id.void_sale_tv);
        refund=findViewById(R.id.refund_tv);
        saveAll=findViewById(R.id.save_log);
        inputIdEd=findViewById(R.id.input_id_ed);
        showAll=findViewById(R.id.show_all_bn);
        packUp=findViewById(R.id.packUp);
        testScan =findViewById(R.id.test_scan_bn);
        scanTv=findViewById(R.id.scan_tv);
        inputIdEd=findViewById(R.id.input_ed_currecy);

        auth.setOnClickListener(this);
        compAuth.setOnClickListener(this);
        offAuth.setOnClickListener(this);
        voidAuth.setOnClickListener(this);
        sale.setOnClickListener(this);
        voidsale.setOnClickListener(this);
        refund.setOnClickListener(this);
        saveAll.setOnClickListener(this);
        showAll.setOnClickListener(this);
        packUp.setOnClickListener(this);
        testScan.setOnClickListener(this);
    }

    private void start(Payment payment){
        if(payment==null){
            return;
        }
        Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
        collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
        startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST);
    }

    private String getCurrency(){
        String currency = inputIdEd.getText().toString().trim();
        if(StringUtil.isEmpty(currency)){
            currency="GBP";
        }
        return currency;
    }






    private String getTransactonId(){
        Payment payment = mPayment;
        String transactonId = payment.getTransactionId();
        if(StringUtil.isEmpty(transactonId)){
            List<Transaction> transactions = payment.getTransactions();
            if(null==transactions || transactions.isEmpty()){
                return transactonId;
            }
            Transaction transaction = transactions.get(0);
            transactonId = transaction.getProcessorResponse().getTransactionId();
        }
        return transactonId;
    }

    private Payment getAuthPayment(){
        Payment payment = new Payment();
        payment.setAction(TransactionAction.AUTHORIZE);
        payment.setCurrency(getCurrency());
        payment.setDisablePaymentOptions(true);
        payment.setAmount(10);
        payment.setAuthzOnly(true);
        return payment;
    }

    private  Payment getCaupAuth(){
        Payment p = checkPayment();
        if(p==null){
            return null;
        }
        Payment payment = new Payment();
        payment.setAction(TransactionAction.CAPTURE);
        payment.setCurrency(getCurrency());
        payment.setAmount(p.getAmount());
        payment.setOrderId(p.getOrderId());
        payment.setTransactionId(getTransactonId());
        payment.setReferenceId(p.getReferenceId());
        payment.setAuthzOnly(true);
        return payment;
    }


    private  Payment getOfflienAuth(){
        Payment p = checkPayment();
        if(p==null){
            return null;
        }
        Payment payment = new Payment();
        payment.setAction(TransactionAction.OFFLINE_AUTHORIZE);
        payment.setCurrency(getCurrency());
        payment.setAmount(p.getAmount());
        payment.setTransactionId(getTransactonId());
        payment.setReferenceId(p.getReferenceId());
        payment.setOrderId(p.getOrderId());
        payment.setAuthzOnly(true);
        return payment;
    }

    private Payment getVoidAuth(){
        Payment p = checkPayment();
        if(p==null){
            return null;
        }

        Payment payment = new Payment();
        payment.setAction(TransactionAction.VOID);
        payment.setCurrency(getCurrency());
        payment.setAmount(p.getAmount());
        payment.setTransactionId(getTransactonId());
        payment.setReferenceId(p.getReferenceId());
        payment.setOrderId(p.getOrderId());
        return payment;
    }

    private Payment getSalePayment(){
        Payment payment = new Payment();
        payment.setAction(TransactionAction.SALE);
        payment.setCurrency(getCurrency());
        payment.setAmount(20);
        payment.setTipAmount(10);
        payment.setSkipSignatureScreen(true);
        payment.setSkipReceiptScreen(true);
        payment.setDisableCash(true);
        payment.setSkipPaymentConfirmationScreen(true);
        return payment;
    }

    private Payment getVoidSalePayment(){
        Payment p = checkPayment();
        if(p==null){
            return null;
        }

        Payment payment = new Payment();
        payment.setTransactionId(getTransactonId());
        payment.setReferenceId(p.getReferenceId());
        payment.setAction(TransactionAction.VOID);
        payment.setCurrency(getCurrency());
        payment.setOrderId(p.getOrderId());
        return payment;
    }

    private Payment getRefundPayment(){
        Payment p = checkPayment();
        if(p==null){
            return null;
        }

        Payment payment = new Payment();
        payment.setTransactionId(getTransactonId());
        payment.setReferenceId(p.getReferenceId());
        payment.setCurrency(getCurrency());
        payment.setAction(TransactionAction.REFUND);
        payment.setNonReferencedCredit(true);
        payment.setAmount(p.getAmount());
        p.setOrderId(p.getOrderId());
        return payment;
    }

    private Payment checkPayment(){
//        String orderId = inputIdEd.getText().toString().trim();
//        if(StringUtil.isEmpty(orderId)){
//            Toast.makeText(MainActivity.this,"Please input the order ID",Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        Payment p = PaymentUtil.getPaymentUtil().getPayment(orderId);
//        if(p.getStatus()==PaymentStatus.CANCELED){
//            Toast.makeText(MainActivity.this,"The order has been cancelled",Toast.LENGTH_SHORT).show();
//            return null;
//        }
        return mPayment;
    }


    private void packUp(){
        InputMethodManager inputMethodManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(saveAll.getWindowToken(), 0);
    }

    private void saveLog(){
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if(aBoolean){
                            PaymentUtil.getPaymentUtil().saveAll();
                        }
                    }
                });
    }

    private void startScann(){
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request( Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if(aBoolean){
                            Intent intent = new Intent("poynt.intent.action.SCANNER");
                            intent.putExtra("MODE", "SINGLE");
                            startActivityForResult(intent, SCANNER_REQUEST_CODE);
                        }
                    }
                });
    }

    public void logReceivedMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanTv.setText(message+"");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.auth_tv:
                start(getAuthPayment());
                break;
            case R.id.caputer_tv:
                Intent intent = new Intent(MainActivity.this,ShowAllId.class);
                intent.putExtra("paymentType",1);
                startActivityForResult(intent, CHECK_TRACKES_CODE);
//                start(getCaupAuth());
                break;
            case R.id.offline_tv:
//                start(getOfflienAuth());
                Intent offlineIntent = new Intent(MainActivity.this,ShowAllId.class);
                offlineIntent.putExtra("paymentType",2);
                startActivityForResult(offlineIntent, CHECK_TRACKES_CODE);
                break;
            case R.id.void_auth_tv:
                Intent voidAuthIntent = new Intent(MainActivity.this,ShowAllId.class);
                voidAuthIntent.putExtra("paymentType",3);
                startActivityForResult(voidAuthIntent, CHECK_TRACKES_CODE);
//                start(getVoidAuth());
                break;
            case R.id.sale_tv:
                start(getSalePayment());
                break;
            case R.id.void_sale_tv:
                Intent voidSaleIntent = new Intent(MainActivity.this,ShowAllId.class);
                voidSaleIntent.putExtra("paymentType",5);
                startActivityForResult(voidSaleIntent, CHECK_TRACKES_CODE);
//                start(getVoidSalePayment());
                break;
            case R.id.refund_tv:
                Intent refundIntent = new Intent(MainActivity.this,ShowAllId.class);
                refundIntent.putExtra("paymentType",6);
                startActivityForResult(refundIntent, CHECK_TRACKES_CODE);
//                start(getRefundPayment());
                break;
            case R.id.save_log:
                saveLog();
                break;
            case R.id.show_all_bn:
                break;
            case R.id.packUp:
                packUp();
                break;
            case R.id.test_scan_bn:
                startScann();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLLECT_PAYMENT_REFS_REQUEST ){
            if (data != null) {
//                if (resultCode == Activity.RESULT_CANCELED) {
//                    logData("Payment Canceled");
//                }
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                if (payment != null) {
                    if(payment.getStatus().equals(PaymentStatus.FAILED)){
                    }else if(payment.getStatus().equals(PaymentStatus.CANCELED)){
                        return;
                    }
//                    Gson gson = new Gson();
//                    Type paymentType = new TypeToken<Payment>() {
//                    }.getType();
//                    String paymentGson =gson.toJson(payment, paymentType);
                    PaymentUtil.getPaymentUtil().putPayment(payment,StringUtil.isEmpty(payment.getReferenceId())?
                            System.currentTimeMillis()+"":payment.getReferenceId()
                            );
                }else{
                    payment = new Payment();
                    payment.setOrderId(System.currentTimeMillis()+"");
                    payment.setActionLabel("该交易已经失败，返回payment为null");
                    PaymentUtil.getPaymentUtil().putPayment(payment,payment.getOrderId());
                }
            }else{
                Payment  payment = new Payment();
                payment.setOrderId(System.currentTimeMillis()+"");
                payment.setActionLabel("该交易已经失败，返回data为null");
                PaymentUtil.getPaymentUtil().putPayment(payment,payment.getOrderId());
            }
        }


        if (requestCode == SCANNER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String code = data.getStringExtra("CODE");
                String format = data.getStringExtra("FORMAT");
                logReceivedMessage("Scanner request was successful - Code:"
                        + code + " Format:" + format);
            } else if (resultCode == RESULT_CANCELED) {
                logReceivedMessage("Scanner canceled!");
            }

        }

        if(CHECK_TRACKES_CODE==requestCode&& resultCode== Activity.RESULT_OK){
            mPayment = PaymentUtil.getPaymentUtil().getPayment(data.getStringExtra("select"));
            int paymentType = data.getIntExtra("paymentType",0);
            switch (paymentType){
                case 1:
                    start(getCaupAuth());
                    break;
                case 2:
                    start(getOfflienAuth());
                    break;
                case 3:
                    start(getVoidAuth());
                    break;
                case 4:
                    break;
                case 5:
                    start(getVoidSalePayment());
                    break;
                case 6:
                    start(getRefundPayment());
                    break;
                    default:
                        break;
            }
        }
    }
}
