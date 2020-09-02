package comshiji.testpoynt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.util.StringUtil;
import io.reactivex.functions.Consumer;

public class TestSaleActivity extends AppCompatActivity {
    private static final int COLLECT_PAYMENT_REFS_REQUEST = 13134;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_1=13181;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_2=13182;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_3=13183;

    private Payment mPayment;

    private Button sale,voidSale1,voidSale2,voidSale3;
    private Button getAll_log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_void);

        sale = findViewById(R.id.sale);
        voidSale1=findViewById(R.id.tv_void_sale_1);
        voidSale2=findViewById(R.id.tv_void_sale_2);
        voidSale3=findViewById(R.id.tv_void_sale_3);
        getAll_log = findViewById(R.id.getAll_log);

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Payment payment = new Payment();
                payment.setAction(TransactionAction.SALE);
                payment.setCurrency("GBP");
                payment.setAmount(200);
                payment.setSkipSignatureScreen(true);
                payment.setSkipReceiptScreen(true);
                payment.setDisableCash(true);
                payment.setSkipPaymentConfirmationScreen(true);

                PaymentUtil.getPaymentUtil().putPayment(payment,"12324234234");

                Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
                collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST);
            }
        });

        voidSale3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Payment payment = new Payment();
                String referenceId =mPayment.getReferenceId();
                payment.setReferenceId(referenceId);
                payment.setAmount(mPayment.getAmount());
                payment.setCurrency("GBP");

                PaymentUtil.getPaymentUtil().putPayment(payment,referenceId);
                Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
                collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST_VOID_3);
            }
        });

        voidSale1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Payment payment = new Payment();
                String referenceId = mPayment.getReferenceId();
                payment.setAction(TransactionAction.VOID);
                payment.setReferenceId(referenceId);
                payment.setAmount(mPayment.getAmount());
                payment.setCurrency("GBP");
                PaymentUtil.getPaymentUtil().putPayment(payment,referenceId);
                Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
                collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST_VOID_1);
        }});

        voidSale2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Payment payment = new Payment();
                String referenceId =mPayment.getReferenceId();
                payment.setAction(TransactionAction.VOID);
                payment.setReferenceId(referenceId);
                payment.setAmount(mPayment.getAmount());
                payment.setCurrency("GBP");
                List<Transaction> orgTransactionList = mPayment.getTransactions();
                if(!orgTransactionList.isEmpty()){
                    payment.setTransactionId(orgTransactionList.get(0).getId().toString());
                }
                PaymentUtil.getPaymentUtil().putPayment(payment,referenceId);
                Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
                collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST_VOID_2);
            }
        });


        getAll_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLog();
            }
        });
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLLECT_PAYMENT_REFS_REQUEST) {
            if (data != null) {
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                if (payment != null) {
                    mPayment = payment;
                    PaymentUtil.getPaymentUtil().putPayment(payment, StringUtil.isEmpty(payment.getReferenceId()) ?
                            System.currentTimeMillis() + "" : payment.getReferenceId()
                    );
                } else {
                    payment = new Payment();
                    payment.setOrderId(System.currentTimeMillis() + "");
                    payment.setActionLabel("该交易已经失败，返回payment为null");
                    PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
                }
            } else {
                Payment payment = new Payment();
                payment.setOrderId(System.currentTimeMillis() + "");
                payment.setActionLabel("该交易已经失败，返回data为null");
                PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
            }
        } else if (COLLECT_PAYMENT_REFS_REQUEST_VOID_1 == requestCode) {
            if (data != null) {
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                if (payment != null) {
                    payment.setActionLabel("方式一 取消交易");
                    PaymentUtil.getPaymentUtil().putPayment(payment, StringUtil.isEmpty(payment.getReferenceId()) ?
                            System.currentTimeMillis() + "" : payment.getReferenceId()
                    );
                } else {
                    payment = new Payment();
                    payment.setOrderId(System.currentTimeMillis() + "");
                    payment.setActionLabel("方式一 取消交易"+"取消交易失败，返回payment为null");
                    PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
                }
            } else {
                Payment payment = new Payment();
                payment.setOrderId(System.currentTimeMillis() + "");
                payment.setActionLabel("方式二 取消交易"+"取消交易失败，返回data为null");
                PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
            }


        }else if (COLLECT_PAYMENT_REFS_REQUEST_VOID_2 == requestCode) {
            if (data != null) {
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                if (payment != null) {
                    payment.setActionLabel("方式二 取消交易");
                    PaymentUtil.getPaymentUtil().putPayment(payment, StringUtil.isEmpty(payment.getReferenceId()) ?
                            System.currentTimeMillis() + "" : payment.getReferenceId()
                    );
                } else {
                    payment = new Payment();
                    payment.setOrderId(System.currentTimeMillis() + "");
                    payment.setActionLabel("方式二 取消交易"  +"取消交易失败，返回payment为null");
                    PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
                }
            } else {
                Payment payment = new Payment();
                payment.setOrderId(System.currentTimeMillis() + "");
                payment.setActionLabel("方式二 取消交易"+"取消交易失败，返回data为null");
                PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
            }


        }else if (COLLECT_PAYMENT_REFS_REQUEST_VOID_2 == requestCode) {
            if (data != null) {
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                if (payment != null) {
                    payment.setActionLabel("方式三 取消交易");
                    PaymentUtil.getPaymentUtil().putPayment(payment, StringUtil.isEmpty(payment.getReferenceId()) ?
                            System.currentTimeMillis() + "" : payment.getReferenceId()
                    );
                } else {
                    payment = new Payment();
                    payment.setOrderId(System.currentTimeMillis() + "");
                    payment.setActionLabel("方式三 取消交易"  +"取消交易失败，返回payment为null");
                    PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
                }
            } else {
                Payment payment = new Payment();
                payment.setOrderId(System.currentTimeMillis() + "");
                payment.setActionLabel("方式三 取消交易"+"取消交易失败，返回data为null");
                PaymentUtil.getPaymentUtil().putPayment(payment, payment.getOrderId());
            }


        }
    }
}
