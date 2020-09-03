package comshiji.testpoynt;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.CaptureAllRequest;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntCheckCardListener;
import co.poynt.os.services.v1.IPoyntCheckPaymentListener;
import co.poynt.os.services.v1.IPoyntGetTransactionsListener;
import co.poynt.os.services.v1.IPoyntOrderService;
import co.poynt.os.services.v1.IPoyntTerminalStatusListener;
import co.poynt.os.services.v1.IPoyntTerminalTotalsListener;
import co.poynt.os.services.v1.IPoyntTerminalTotalsV4Listener;
import co.poynt.os.services.v1.IPoyntTransactionBalanceInquiryListener;
import co.poynt.os.services.v1.IPoyntTransactionCaptureAllListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;
import co.poynt.os.util.StringUtil;
import io.reactivex.functions.Consumer;

public class TestSaleActivity2 extends AppCompatActivity {
    private static final int COLLECT_PAYMENT_REFS_REQUEST = 13134;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_1=13181;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_2=13182;
    private static final int COLLECT_PAYMENT_REFS_REQUEST_VOID_3=13183;

    private Payment mPayment;

    private Button sale,voidSale1,voidSale2,voidSale3;
    private Button getAll_log;
    private TextView tvContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_void);

        sale = findViewById(R.id.sale);
        tvContent=findViewById(R.id.tvContent);
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

                Gson gson = new Gson();
                Type transactionType = new TypeToken<Payment>() {
                }.getType();
                String transactionJson = gson.toJson(payment, transactionType);

                PaymentUtil.getPaymentUtil().putPayment(transactionJson,"消费请求"+System.currentTimeMillis());

                Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
                collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
                startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REFS_REQUEST);
            }
        });

        voidSale3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Transaction> transactionList = mPayment.getTransactions();
                for(Transaction transaction:transactionList){
                    Transaction voidTransaction = new Transaction();
                    voidTransaction.setParentId(transaction.getId());
                    voidTransaction.setId(UUID.randomUUID());
                    voidTransaction.setAction(TransactionAction.REFUND);
                    voidTransaction.setActionVoid(true);


                    Gson gson = new Gson();
                    Type transactionType = new TypeToken<Transaction>() {
                    }.getType();
                    String transactionJson = gson.toJson(voidTransaction, transactionType);

                    PaymentUtil.getPaymentUtil().putPayment(transactionJson,"撤销请求3"+System.currentTimeMillis());


                    if(transactionService!=null){
                        try {
                            transactionService.processTransaction(voidTransaction,mPayment.getReferenceId(),mTransactionServiceListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(TestSaleActivity2.this,"service not connection",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        voidSale1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transactionService!=null){
                    List<Transaction> transactionList = mPayment.getTransactions();
                    for(Transaction transaction:transactionList){
                        try {
                            PaymentUtil.getPaymentUtil().putPayment(transaction.getId().toString() +"\n","撤销请求1"+System.currentTimeMillis());
                            transactionService.voidTransaction(transaction.getId().toString(),transaction.getFundingSource().getEmvData(),mPayment.getReferenceId(),mTransactionServiceListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    Toast.makeText(TestSaleActivity2.this,"service not connection",Toast.LENGTH_SHORT).show();
                }
        }});

        voidSale2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Transaction> transactionList = mPayment.getTransactions();
                for(Transaction transaction:transactionList){
                    Transaction voidTransaction = new Transaction();
                    voidTransaction.setParentId(transaction.getId());
                    voidTransaction.setId(UUID.randomUUID());
                    voidTransaction.setAction(TransactionAction.REFUND);
                    voidTransaction.setActionVoid(true);

                    Gson gson = new Gson();
                    Type transactionType = new TypeToken<Transaction>() {
                    }.getType();
                    String transactionJson = gson.toJson(voidTransaction, transactionType);

                    PaymentUtil.getPaymentUtil().putPayment(transactionJson,"撤销请求2"+System.currentTimeMillis());


                    if(transactionService!=null){
                        try {
                            transactionService.processTransaction(voidTransaction,mPayment.getReferenceId(),mTransactionServiceListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(TestSaleActivity2.this,"service not connection",Toast.LENGTH_SHORT).show();
                    }
                }
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
                            PaymentUtil.getPaymentUtil().saveStringAll();
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
                    Gson gson = new Gson();
                    Type transactionType = new TypeToken<Payment>() {
                    }.getType();
                    String transactionJson = gson.toJson(payment, transactionType);


                    PaymentUtil.getPaymentUtil().putPayment(transactionJson,"消费成功返回"+System.currentTimeMillis());


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







    @Override
    protected void onStart() {
        super.onStart();

        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_TRANSACTION_SERVICE),
                transactionServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(transactionServiceConnection);
    }



    private IPoyntTransactionService transactionService;
    private ServiceConnection transactionServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            transactionService =IPoyntTransactionService.Stub.asInterface(service);
            if(transactionService!=null){
                Toast.makeText(TestSaleActivity2.this,"Service Create Sucess",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(TestSaleActivity2.this,"Service Create Fail",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private IPoyntTransactionServiceListener mTransactionServiceListener = new IPoyntTransactionServiceListener.Stub() {
        public void onResponse(Transaction _transaction, String s, PoyntError poyntError) throws RemoteException {
            Gson gson = new Gson();
            Type transactionType = new TypeToken<Transaction>() {
            }.getType();
            String transactionJson = gson.toJson(_transaction, transactionType);


            tvContent.setText(tvContent.getText().toString() + "\n----------------------------------------------------------"+"\n"+transactionJson);

            PaymentUtil.getPaymentUtil().putPayment(transactionJson,"当前时间"+System.currentTimeMillis());
        }

        //@Override
        public void onLaunchActivity(Intent intent, String s) throws RemoteException {
            //do nothing
        }

        public void onLoginRequired() throws RemoteException {

        }

    };

}
